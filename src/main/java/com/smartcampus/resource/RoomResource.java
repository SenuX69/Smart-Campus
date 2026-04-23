package com.smartcampus.resource;

import com.smartcampus.exception.RoomNotEmptyException;
import com.smartcampus.model.Room;
import com.smartcampus.service.RoomService;
import com.smartcampus.service.SensorService;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * Resource class for managing Rooms at /api/v1/rooms.
 * Provides full CRUD operations with business logic constraints.
 */
@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
public class RoomResource {

    private RoomService roomService = new RoomService();
    private SensorService sensorService = new SensorService();

    /**
     * GET /api/v1/rooms
     * Returns a list of all rooms.
     */
    @GET
    public List<Room> getAllRooms() {
        return roomService.getAllRooms();
    }

    /**
     * GET /api/v1/rooms/{roomId}
     * Returns detailed metadata for a specific room.
     */
    @GET
    @Path("/{roomId}")
    public Response getRoomById(@PathParam("roomId") String roomId) {
        Room room = roomService.getRoomById(roomId);
        if (room == null) {
            return Response
                    .status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"Not Found\", \"message\": \"Room with ID '" + roomId + "' not found\", \"status\": 404}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }
        return Response.ok(room).build();
    }

    /**
     * POST /api/v1/rooms
     * Creates a new room. Returns 201 Created with location header.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createRoom(Room room) {
        // Validate required fields
        if (room.getId() == null || room.getId().isEmpty()) {
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"Bad Request\", \"message\": \"Room ID is required\", \"status\": 400}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }
        if (room.getName() == null || room.getName().isEmpty()) {
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"Bad Request\", \"message\": \"Room name is required\", \"status\": 400}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        // Check for duplicate ID
        if (roomService.roomExists(room.getId())) {
            return Response
                    .status(Response.Status.CONFLICT)
                    .entity("{\"error\": \"Conflict\", \"message\": \"Room with ID '" + room.getId() + "' already exists\", \"status\": 409}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        roomService.addRoom(room);

        return Response
                .status(Response.Status.CREATED)
                .entity(room)
                .header("Location", "/api/v1/rooms/" + room.getId())
                .build();
    }

    /**
     * PUT /api/v1/rooms/{roomId}
     * Updates an existing room.
     */
    @PUT
    @Path("/{roomId}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateRoom(@PathParam("roomId") String roomId, Room room) {
        Room existing = roomService.getRoomById(roomId);
        if (existing == null) {
            return Response
                    .status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"Not Found\", \"message\": \"Room with ID '" + roomId + "' not found\", \"status\": 404}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        // Update fields
        if (room.getName() != null) existing.setName(room.getName());
        if (room.getCapacity() > 0) existing.setCapacity(room.getCapacity());

        return Response.ok(existing).build();
    }

    /**
     * DELETE /api/v1/rooms/{roomId}
     * Deletes a room. Throws RoomNotEmptyException (409) if the room still has sensors.
     * This operation is idempotent — deleting a non-existent room returns 204 No Content.
     */
    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        Room room = roomService.getRoomById(roomId);

        // Idempotent: if room doesn't exist, return 204 (already deleted)
        if (room == null) {
            return Response.noContent().build();
        }

        // Business Logic Constraint: cannot delete room with assigned sensors
        if (sensorService.hasActiveSensorsInRoom(roomId)) {
            throw new RoomNotEmptyException(
                    "Cannot delete room '" + roomId + "' because it still has sensors assigned to it. " +
                    "Please reassign or remove all sensors before deleting the room."
            );
        }

        roomService.deleteRoom(roomId);
        return Response.noContent().build();
    }
}
