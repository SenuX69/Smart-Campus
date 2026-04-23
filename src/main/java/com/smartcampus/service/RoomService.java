package com.smartcampus.service;

import com.smartcampus.model.Room;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * In-memory data store and business logic for Room resources.
 * Uses a static LinkedHashMap to maintain insertion order and allow efficient lookups by ID.
 */
public class RoomService {

    private static Map<String, Room> rooms = new LinkedHashMap<>();

    // Pre-populate with sample data
    static {
        Room room1 = new Room("LIB-301", "Library Quiet Study", 50);
        Room room2 = new Room("ENG-101", "Engineering Lab A", 30);
        Room room3 = new Room("LEC-201", "Main Lecture Hall", 200);
        rooms.put(room1.getId(), room1);
        rooms.put(room2.getId(), room2);
        rooms.put(room3.getId(), room3);
    }

    /**
     * Returns a list of all rooms.
     */
    public List<Room> getAllRooms() {
        return new ArrayList<>(rooms.values());
    }

    /**
     * Retrieves a room by its unique identifier.
     * @return the Room, or null if not found
     */
    public Room getRoomById(String id) {
        return rooms.get(id);
    }

    /**
     * Adds a new room to the in-memory store.
     */
    public void addRoom(Room room) {
        rooms.put(room.getId(), room);
    }

    /**
     * Removes a room by its identifier.
     * @return the removed Room, or null if it did not exist
     */
    public Room deleteRoom(String id) {
        return rooms.remove(id);
    }

    /**
     * Checks if a room with the given identifier exists.
     */
    public boolean roomExists(String id) {
        return rooms.containsKey(id);
    }

    /**
     * Adds a sensor ID to the room's list of sensor IDs.
     */
    public void addSensorToRoom(String roomId, String sensorId) {
        Room room = rooms.get(roomId);
        if (room != null && !room.getSensorIds().contains(sensorId)) {
            room.getSensorIds().add(sensorId);
        }
    }

    /**
     * Removes a sensor ID from the room's list of sensor IDs.
     */
    public void removeSensorFromRoom(String roomId, String sensorId) {
        Room room = rooms.get(roomId);
        if (room != null) {
            room.getSensorIds().remove(sensorId);
        }
    }
}