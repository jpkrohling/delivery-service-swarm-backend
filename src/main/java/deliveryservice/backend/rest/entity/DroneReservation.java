package deliveryservice.backend.rest.entity;

import java.time.Instant;

public class DroneReservation {
    private Drone drone;
    private Instant expiration;

    public DroneReservation(Drone drone) {
        this.drone = drone;
    }

    public DroneReservation(Drone drone, Instant expiration) {
        this.drone = drone;
        this.expiration = expiration;
    }

    public Drone getDrone() {
        return drone;
    }

    public Instant getExpiration() {
        return expiration;
    }
}
