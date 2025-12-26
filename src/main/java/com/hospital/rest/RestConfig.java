package com.hospital.rest;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

@ApplicationPath("/api")
public class RestConfig extends Application {
    // Ensure this exists:
    public RestConfig() {

    }
}