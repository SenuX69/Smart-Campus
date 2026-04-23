package com.smartcampus.resource;

import com.smartcampus.exception.LinkedResourceNotFoundException;
import com.smartcampus.model.Sensor;
import com.smartcampus.service.RoomService;
import com.smartcampus.service.SensorService;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * Resource class for managing Sensors at /api/v1/sensors.
 * Supports CRUD operations, filtering by type, and sub-resource locator for readings.
 */
@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
public class SensorResource {

    private SensorService sensorService = new SensorService();
    private RoomService roomService = new RoomService();

    /**
     * GET /api/v1/sensors
     * Returns all sensors. Supports optional ?type= query parameter for filtering.
     * Example: GET /api/v1/sensors?type=Temperature
     */
    @GET
    public List<Sensor> getAllSensors(@QueryParam("type") String type) {
        if (type != null && !type.isEmpty()) {
            return sensorService.getSensorsByType(type);
        }
        return sensorService.getAllSensors();
    }

    /**
     * GET /api/v1/sensors/{sensorId}
     * Returns detailed metadata for a specific sensor.
     */
    @GET
    @Path("/{sensorId}")
    public Response getSensorById(@PathParam("sensorId") String sensorId) {
        Sensor sensor = sensorService.getSensorById(sensorId);
        if (sensor == null) {
            return Response
                    .status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"Not Found\", \"message\": \"Sensor with ID '" + sensorId + "' not found\", \"status\": 404}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }
        return Response.ok(sensor).build();
    }

    /**
     * POST /api/v1/sensors
     * Creates a new sensor. Validates that the referenced roomId exists.
     * Throws LinkedResourceNotFoundException (422) if roomId is invalid.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createSensor(Sensor sensor) {
        // Validate required fields
        if (sensor.getId() == null || sensor.getId().isEmpty()) {
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"Bad Request\", \"message\": \"Sensor ID is required\", \"status\": 400}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        // Validate that the referenced roomId exists (Dependency Validation)
        if (sensor.getRoomId() == null || !roomService.roomExists(sensor.getRoomId())) {
            throw new LinkedResourceNotFoundException(
                    "The referenced Room with ID '" + sensor.getRoomId() + "' does not exist. " +
                    "Please create the room first or use a valid roomId."
            );
        }

        // Check for duplicate sensor ID
        if (sensorService.getSensorById(sensor.getId()) != null) {
            return Response
                    .status(Response.Status.CONFLICT)
                    .entity("{\"error\": \"Conflict\", \"message\": \"Sensor with ID '" + sensor.getId() + "' already exists\", \"status\": 409}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        sensorService.addSensor(sensor);

        // Link sensor to room
        roomService.addSensorToRoom(sensor.getRoomId(), sensor.getId());

        return Response
                .status(Response.Status.CREATED)
                .entity(sensor)
                .header("Location", "/api/v1/sensors/" + sensor.getId())
                .build();
    }

    /**
     * PUT /api/v1/sensors/{sensorId}
     * Updates an existing sensor.
     */
    @PUT
    @Path("/{sensorId}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateSensor(@PathParam("sensorId") String sensorId, Sensor sensor) {
        Sensor existing = sensorService.getSensorById(sensorId);
        if (existing == null) {
            return Response
                    .status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"Not Found\", \"message\": \"Sensor with ID '" + sensorId + "' not found\", \"status\": 404}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        // Update fields if provided
        if (sensor.getType() != null) existing.setType(sensor.getType());
        if (sensor.getStatus() != null) existing.setStatus(sensor.getStatus());
        if (sensor.getCurrentValue() != 0) existing.setCurrentValue(sensor.getCurrentValue());

        // Handle room change
        if (sensor.getRoomId() != null && !sensor.getRoomId().equals(existing.getRoomId())) {
            if (!roomService.roomExists(sensor.getRoomId())) {
                throw new LinkedResourceNotFoundException(
                        "The referenced Room with ID '" + sensor.getRoomId() + "' does not exist."
                );
            }
            roomService.removeSensorFromRoom(existing.getRoomId(), sensorId);
            roomService.addSensorToRoom(sensor.getRoomId(), sensorId);
            existing.setRoomId(sensor.getRoomId());
        }

        return Response.ok(existing).build();
    }

    /**
     * DELETE /api/v1/sensors/{sensorId}
     * Deletes a sensor and removes it from its parent room's sensorIds list.
     */
    @DELETE
    @Path("/{sensorId}")
    public Response deleteSensor(@PathParam("sensorId") String sensorId) {
        Sensor sensor = sensorService.getSensorById(sensorId);

        if (sensor == null) {
            return Response.noContent().build();
        }

        // Remove sensor from its room
        roomService.removeSensorFromRoom(sensor.getRoomId(), sensorId);
        sensorService.deleteSensor(sensorId);

        return Response.noContent().build();
    }

    /**
     * Sub-Resource Locator for sensor readings.
     * Delegates requests to /api/v1/sensors/{sensorId}/readings to SensorReadingResource.
     * 
     * This pattern decouples the reading management logic from the sensor resource,
     * allowing for cleaner separation of concerns and independent evolution.
     */
    @Path("/{sensorId}/readings")
    public SensorReadingResource getSensorReadings(@PathParam("sensorId") String sensorId) {
        // Validate sensor exists before delegating to sub-resource
        Sensor sensor = sensorService.getSensorById(sensorId);
        if (sensor == null) {
            throw new NotFoundException("Sensor with ID '" + sensorId + "' not found");
        }
        return new SensorReadingResource(sensorId);
    }
}
