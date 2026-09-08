package com.healthcare.keymanagement;

import com.healthcare.keymanagement.exception.GlobalExceptionHandler;
import com.healthcare.keymanagement.exception.KeyNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {

        handler = new GlobalExceptionHandler();

        request = mock(HttpServletRequest.class);

        when(request.getRequestURI())
                .thenReturn("/api/keys/TEST-001");
    }

    @Test
    void shouldHandleKeyNotFoundException() {

        KeyNotFoundException exception =
                new KeyNotFoundException(
                        "Active key not found: TEST-001"
                );

        var response =
                handler.handleKeyNotFound(
                        exception,
                        request
                );

        assertEquals(
                404,
                response.getStatusCode().value()
        );

        assertNotNull(response.getBody());

        assertEquals(
                404,
                response.getBody().status()
        );

        assertEquals(
                "Not Found",
                response.getBody().error()
        );

        assertEquals(
                "Active key not found: TEST-001",
                response.getBody().message()
        );

        assertEquals(
                "/api/keys/TEST-001",
                response.getBody().path()
        );

        assertNotNull(
                response.getBody().timestamp()
        );
    }
}