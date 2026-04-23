package com.smartcampus.service;

import com.smartcampus.model.Sensor;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * In-memory data store and business logic for Sensor resources.
 */
public class SensorService {

    private static Map<String, Sensor> sensors = new LinkedHashMap<>();

    // Pre-populate with sample data linked to existing rooms
    static {
        Sensor s1 = new Sensor("TEMP-001", "Temperature", "ACTIVE", 22.5, "LIB-301");
        Sensor s2 = new Sensor("OCC-001", "Occupancy", "ACTIVE", 35.0, "LEC-201");
        Sensor s3 = new Sensor("CO2-001", "CO2", "MAINTENANCE", 410.0, "ENG-101");
        sensors.put(s1.getId(), s1);
        sensors.put(s2.getId(), s2);
        sensors.put(s3.getId(), s3);

        // Also register these sensor IDs on their rooms
        RoomService roomService = new RoomService();
        roomService.addSensorToRoom("LIB-301", "TEMP-001");
        roomService.addSensorToRoom("LEC-201", "OCC-001");
        roomService.addSensorToRoom("ENG-101", "CO2-001");
    }

    /**
     * Returns a list of all sensors.
     */
    public List<Sensor> getAllSensors() {
        return new ArrayList<>(sensors.values());
    }

    /**
     * Returns sensors filtered by type (case-insensitive match).
     */
    public List<Sensor> getSensorsByType(String type) {
        return sensors.values().stream()
                .filter(s -> s.getType().equalsIgnoreCase(type))
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a sensor by its unique identifier.
     * @return the Sensor, or null if not found
     */
    public Sensor getSensorById(String id) {
        return sensors.get(id);
    }

    /**
     * Adds a new sensor to the in-memory store.
     */
    public void addSensor(Sensor sensor) {
        sensors.put(sensor.getId(), sensor);
    }

    /**
     * Removes a sensor by its identifier.
     * @return the removed Sensor, or null if it did not exist
     */
    public Sensor deleteSensor(String id) {
        return sensors.remove(id);
    }

    /**
     * Returns all sensors assigned to a particular room.
     */
    public List<Sensor> getSensorsByRoomId(String roomId) {
        return sensors.values().stream()
                .filter(s -> roomId.equals(s.getRoomId()))
                .collect(Collectors.toList());
    }

    /**
     * Checks whether a room has any sensors with status "ACTIVE" assigned to it.
     * Used as a safety check before room deletion.
     */
    public boolean hasActiveSensorsInRoom(String roomId) {
        return sensors.values().stream()
                .anyMatch(s -> roomId.equals(s.getRoomId()));
    }
}
