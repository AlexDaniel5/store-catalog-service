package com.example.catalog.store;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.catalog.common.error.ResourceNotFoundException;
import com.example.catalog.store.dto.StoreResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(StoreController.class)
class StoreControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private StoreService storeService;

    private static StoreResponse sampleStore() {
        return new StoreResponse(1L, "Pizza Palace", "Best pizza", "1 Main St",
                "555-0100", true, Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-01-01T00:00:00Z"));
    }

    @Test
    void createReturns201WithLocation() throws Exception {
        when(storeService.create(any())).thenReturn(sampleStore());

        String body = """
                {"name":"Pizza Palace","address":"1 Main St","phone":"555-0100"}
                """;

        mockMvc.perform(post("/api/v1/stores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/v1/stores/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Pizza Palace"));
    }

    @Test
    void createWithBlankNameReturns400WithFieldErrors() throws Exception {
        String body = """
                {"name":"","address":""}
                """;

        mockMvc.perform(post("/api/v1/stores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed for request body"))
                .andExpect(jsonPath("$.path").value("/api/v1/stores"))
                .andExpect(jsonPath("$.fieldErrors[?(@.field=='name')]").exists())
                .andExpect(jsonPath("$.fieldErrors[?(@.field=='address')]").exists());
    }

    @Test
    void getMissingStoreReturns404() throws Exception {
        when(storeService.get(eq(99L)))
                .thenThrow(new ResourceNotFoundException("Store", 99L));

        mockMvc.perform(get("/api/v1/stores/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Store with id 99 not found"));
    }

    @Test
    void getWithNonNumericIdReturns400() throws Exception {
        mockMvc.perform(get("/api/v1/stores/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void deleteReturns204() throws Exception {
        mockMvc.perform(delete("/api/v1/stores/1"))
                .andExpect(status().isNoContent());
        verify(storeService).delete(1L);
    }
}
