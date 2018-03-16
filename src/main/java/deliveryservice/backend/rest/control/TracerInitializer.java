package deliveryservice.backend.rest.control;

import com.uber.jaeger.Configuration;
import com.uber.jaeger.samplers.ConstSampler;
import io.opentracing.Tracer;
import io.opentracing.contrib.metrics.Metrics;
import io.opentracing.contrib.metrics.prometheus.PrometheusMetricsReporter;
import io.opentracing.util.GlobalTracer;
import io.prometheus.client.CollectorRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class TracerInitializer implements ServletContextListener {
    private static final Logger logger = LoggerFactory.getLogger(TracerInitializer.class);

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        if (GlobalTracer.isRegistered()) {
            logger.info("A tracer is already registered with the global tracer. Skipping.");
        } else {
            logger.info("Registering a Prometheus Tracer, wrapping a Jaeger Tracer");
            GlobalTracer.register(getPrometheusTracer());
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {

    }

    public Tracer getPrometheusTracer() {
        PrometheusMetricsReporter reporter = PrometheusMetricsReporter.newMetricsReporter()
                .withCollectorRegistry(CollectorRegistry.defaultRegistry)
                .build();

        return Metrics.decorate(getJaegerTracer(), reporter);
    }

    public Tracer getJaegerTracer() {
        return new Configuration("delivery-service-swarm-backend")
                .withReporter(
                        new Configuration.ReporterConfiguration()
                                .withLogSpans(true)
                )
                .withSampler(
                        new Configuration.SamplerConfiguration()
                                .withType(ConstSampler.TYPE)
                                .withParam(1)
                )
                .getTracerBuilder()
                .build();
    }
}
