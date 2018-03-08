package eu.javaland.tracing.backend.rest.entity;

public class DispatchDroneEvent {
    private Drone drone;

    public DispatchDroneEvent(Drone drone) {
        this.drone = drone;
    }

    public Drone getDrone() {
        return drone;
    }
}
