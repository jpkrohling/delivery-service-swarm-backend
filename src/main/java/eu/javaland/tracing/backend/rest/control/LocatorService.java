package eu.javaland.tracing.backend.rest.control;

import eu.javaland.tracing.backend.rest.entity.Drone;
import eu.javaland.tracing.backend.rest.entity.Location;
import io.opentracing.util.GlobalTracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

@Stateless
public class LocatorService {
    private static final Logger logger = LoggerFactory.getLogger(LocatorService.class);

    @Inject
    RegistrationService registrationService;

    public Drone locate() {
        logger.info("Locating drone");
        long wait = (long)(Math.random() * 1000);
        Optional.ofNullable(GlobalTracer.get().activeSpan()).ifPresent(as -> as.setTag("wait", wait));

        try {
            Thread.sleep(wait);
        } catch (InterruptedException ignored) {
        }

        List<Drone> availableDrones = registrationService.getAvailableDrones(Location.MUNICH);
        if (availableDrones.size() > 0) {
            return availableDrones.get(0);
        } else {
            logger.info("Sorry, no drones available.");
            return null;
        }
    }

}
