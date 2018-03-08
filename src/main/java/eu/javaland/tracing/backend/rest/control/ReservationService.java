package eu.javaland.tracing.backend.rest.control;

import eu.javaland.tracing.backend.rest.entity.Drone;
import eu.javaland.tracing.backend.rest.entity.DroneReservation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateless;
import java.time.Instant;

@Stateless
public class ReservationService {
    private static final Logger logger = LoggerFactory.getLogger(RegistrationService.class);

    public DroneReservation reserve(Drone drone) {
        logger.info("Reserving drone {}", drone);
        return new DroneReservation(drone, Instant.now());
    }
}
