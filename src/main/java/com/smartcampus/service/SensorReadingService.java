package com.smartcampus.service;

import com.smartcampus.model.SensorReading;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * In-memory data store for SensorReading sub-resources.
 * Readings are stored in a map keyed by the parent sensor's ID.
 */
public class SensorReadingService {

    // Map<sensorId, List<SensorReading>>
    private static Map<String, List<SensorReading>> readings = new LinkedHashMap<>();

    /**
     * Returns all readings for a given sensor.
     * Returns an empty list if no readings exist yet.
     */
    public List<SensorReading> getReadings(String sensorId) {
        return readings.getOrDefault(sensorId, new ArrayList<>());
    }

    /**
     * Adds a new reading for a given sensor.
     * Automatically generates a UUID for the reading ID and sets the timestamp
     * to the current epoch time if not already set.
     */
    public SensorReading addReading(String sensorId, SensorReading reading) {
        // Auto-generate ID if not provided
        if (reading.getId() == null || reading.getId().isEmpty()) {
            reading.setId(UUID.randomUUID().toString());
        }

        // Auto-set timestamp if not provided
        if (reading.getTimestamp() <= 0) {
            reading.setTimestamp(System.currentTimeMillis());
        }

        readings.computeIfAbsent(sensorId, k -> new ArrayList<>()).add(reading);
        return reading;
    }
}
