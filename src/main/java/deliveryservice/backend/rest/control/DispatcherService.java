package deliveryservice.backend.rest.control;

import deliveryservice.backend.rest.entity.DispatchDroneEvent;
import deliveryservice.backend.rest.entity.Drone;
import deliveryservice.backend.rest.entity.DroneReservation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;

@Stateless
public class DispatcherService {
    private static final Logger logger = LoggerFactory.getLogger(DispatcherService.class);

    @Inject
    Event<DispatchDroneEvent> event;

    public void dispatch(DroneReservation reservation) {
        Drone drone = reservation.getDrone();
        logger.info("Dispatching drone {}", drone);
        event.fire(new DispatchDroneEvent(drone));
    }
}
