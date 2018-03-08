package eu.javaland.tracing.backend.rest.boundary;

import io.prometheus.client.exporter.MetricsServlet;

import javax.servlet.annotation.WebServlet;

@WebServlet(urlPatterns = "/metrics")
public class JavalandSwarmMetricsServlet extends MetricsServlet {
}
