package eu.javaland.tracing.backend.rest.entity;

import java.util.UUID;

public class Drone {
    private String id = UUID.randomUUID().toString();
    private Location location;

    public Drone(Location location) {
        this.location = location;
    }

    public Drone(String id, Location location) {
        this.id = id;
        this.location = location;
    }

    public String getId() {
        return id;
    }

    public Location getLocation() {
        return location;
    }

    @Override
    public String toString() {
        return "Drone{" +
                "id='" + id + '\'' +
                ", location=" + location +
                '}';
    }
}
