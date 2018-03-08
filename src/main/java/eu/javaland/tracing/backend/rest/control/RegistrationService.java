package eu.javaland.tracing.backend.rest.control;

import eu.javaland.tracing.backend.rest.entity.Drone;
import eu.javaland.tracing.backend.rest.entity.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Singleton;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Singleton
public class RegistrationService {
    private static final Logger logger = LoggerFactory.getLogger(RegistrationService.class);
    private Map<String, Drone> registry = new HashMap<>();

    public void register(Drone drone) {
        logger.info("Registering drone {}", drone.toString());
        registry.put(drone.getId(), drone);
    }

    public List<Drone> getAvailableDrones(Location location) {
        return registry.values().stream().filter(c -> (c.getLocation().equals(location))).collect(Collectors.toList());
    }

    public void dispose(String id) {
        logger.info("Disposing {}", id);
        Optional.of(registry.get(id)).ifPresent(v -> logger.info("Drone got destroyed. {}", v));
    }
}
