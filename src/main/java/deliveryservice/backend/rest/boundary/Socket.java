package deliveryservice.backend.rest.boundary;

import deliveryservice.backend.rest.control.RegistrationService;
import deliveryservice.backend.rest.entity.DispatchDroneEvent;
import deliveryservice.backend.rest.entity.Drone;
import deliveryservice.backend.rest.entity.Location;
import io.opentracing.*;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMapExtractAdapter;
import io.opentracing.propagation.TextMapInjectAdapter;
import io.opentracing.tag.Tags;
import io.opentracing.util.GlobalTracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateless;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonString;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

@ServerEndpoint("/socket")
@Stateless
public class Socket {
    private static final Logger logger = LoggerFactory.getLogger(Socket.class);
    private static Map<String, Session> sessionByCar = new HashMap<>();

    @Inject
    RegistrationService registrationService;

    @OnOpen
    public void onOpen(Session session) {
        logger.info("New socket session opened");
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        logger.info("Error on socket");
        StringWriter writer = new StringWriter();
        throwable.printStackTrace(new PrintWriter(writer));
        GlobalTracer.get().activeSpan().log(writer.toString());
    }

    @OnClose
    public void onClose(Session session) {
        sessionByCar.entrySet()
                .stream()
                .filter(e -> e.getValue().equals(session))
                .findAny()
                .ifPresent(k -> {
                    registrationService.dispose(k.getKey());
                    sessionByCar.remove(k.getKey());
                });
        logger.info("Session closed");
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        logger.info("Message received on socket: {}", message);
        JsonObject jsonObject = Json.createReader(new StringReader(message)).readObject();

        Map<String, String> messageContent = new HashMap<>();
        jsonObject.forEach((k, v) -> {
            // we are sure it's a <String, String>, otherwise, we'd need a better handling here
            messageContent.put(k, ((JsonString) v).getString());
        });

        Tracer tracer = GlobalTracer.get();
        SpanContext context = tracer.extract(Format.Builtin.TEXT_MAP, new TextMapExtractAdapter(messageContent));

        try (Scope ignored = tracer.buildSpan("drone-registration")
                .withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_SERVER)
                .addReference(References.FOLLOWS_FROM, context)
                .startActive(true)) {
            double lat = Double.parseDouble(jsonObject.getString("lat"));
            double lon = Double.parseDouble(jsonObject.getString("lon"));
            Location location = new Location(lat, lon);
            Drone drone = new Drone(jsonObject.getString("id"), location);

            sessionByCar.put(drone.getId(), session);
            registrationService.register(drone);
        }
    }

    public void dispatchDrone(@Observes DispatchDroneEvent event) {
        Drone drone = event.getDrone();
        Session session = sessionByCar.get(drone.getId());
        if (!session.isOpen()) {
            Span span = GlobalTracer.get().activeSpan();
            // oops, we failed to clean this up somehow
            sessionByCar.remove(drone.getId());
            span.log("The drone session was on the cache, but it vanished already!");
            span.setTag("id", drone.getId());
            return;
        }

        Tracer tracer = GlobalTracer.get();
        Map<String, String> context = new HashMap<>();
        tracer.inject(tracer.activeSpan().context(), Format.Builtin.TEXT_MAP, new TextMapInjectAdapter(context));

        JsonObjectBuilder builder = Json.createObjectBuilder();
        context.forEach(builder::add);
        builder.add("action", "register");
        builder.add("id", drone.getId());
        builder.add("lat", "48.133333"); // TODO: dispatch to the user's requested location
        builder.add("lon", "11.566667"); // TODO: ditto

        try {
            session.getBasicRemote().sendText(builder.build().toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
