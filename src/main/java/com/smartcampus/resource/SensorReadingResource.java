package com.smartcampus.resource;

import com.smartcampus.exception.SensorUnavailableException;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;
import com.smartcampus.service.SensorReadingService;
import com.smartcampus.service.SensorService;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * Sub-resource class for managing SensorReadings.
 * Accessed via the sub-resource locator in SensorResource at:
 *   /api/v1/sensors/{sensorId}/readings
 * 
 * This class does NOT have a @Path annotation at the class level
 * because it is instantiated and returned by the parent SensorResource.
 */
@Produces(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    private String sensorId;
    private SensorReadingService readingService = new SensorReadingService();
    private SensorService sensorService = new SensorService();

    /**
     * Constructor receives the parent sensor's ID from the sub-resource locator.
     */
    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    /**
     * GET /api/v1/sensors/{sensorId}/readings
     * Returns the full reading history for this sensor.
     */
    @GET
    public List<SensorReading> getReadings() {
        return readingService.getReadings(sensorId);
    }

    /**
     * POST /api/v1/sensors/{sensorId}/readings
     * Appends a new reading to this sensor's history.
     * 
     * Business Logic:
     * - Throws SensorUnavailableException (403) if the sensor is in "MAINTENANCE" status.
     * - On success, updates the parent Sensor's currentValue with the new reading value.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addReading(SensorReading reading) {
        Sensor sensor = sensorService.getSensorById(sensorId);

        // State Constraint: sensor in MAINTENANCE cannot accept readings
        if (sensor != null && "MAINTENANCE".equalsIgnoreCase(sensor.getStatus())) {
            throw new SensorUnavailableException(
                    "Sensor '" + sensorId + "' is currently in MAINTENANCE mode and cannot accept new readings. " +
                    "Please wait until the sensor is back to ACTIVE status."
            );
        }

        // Add the reading (auto-generates UUID and timestamp)
        SensorReading savedReading = readingService.addReading(sensorId, reading);

        // Side Effect: update the parent sensor's currentValue
        if (sensor != null) {
            sensor.setCurrentValue(savedReading.getValue());
        }

        return Response
                .status(Response.Status.CREATED)
                .entity(savedReading)
                .build();
    }
}
