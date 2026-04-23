package com.smartcampus.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Root "Discovery" endpoint at GET /api/v1.
 * Returns API metadata including version, contact information,
 * and a map of available resource collection URIs (HATEOAS links).
 */
@Path("/")
public class RootResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> root() {
        Map<String, Object> discovery = new LinkedHashMap<>();
        discovery.put("name", "Smart Campus Sensor & Room Management API");
        discovery.put("version", "1.0");
        discovery.put("description", "RESTful API for managing campus rooms, sensors, and sensor readings");
        discovery.put("contact", "admin@smartcampus.westminster.ac.uk");

        // HATEOAS-style links to primary resource collections
        Map<String, String> resources = new LinkedHashMap<>();
        resources.put("rooms", "/api/v1/rooms");
        resources.put("sensors", "/api/v1/sensors");
        discovery.put("resources", resources);

        return discovery;
    }
}