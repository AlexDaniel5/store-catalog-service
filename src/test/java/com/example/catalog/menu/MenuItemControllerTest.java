package com.example.catalog.menu;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.catalog.menu.dto.MenuItemResponse;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest({MenuItemController.class, MenuItemSearchController.class})
class MenuItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MenuItemService menuItemService;

    private static MenuItemResponse sampleItem() {
        return new MenuItemResponse(10L, 1L, "Margherita", "Classic",
                new BigDecimal("9.50"), "Pizza", true,
                Instant.parse("2026-01-01T00:00:00Z"), Instant.parse("2026-01-01T00:00:00Z"));
    }

    @Test
    void createReturns201() throws Exception {
        when(menuItemService.create(eq(1L), any())).thenReturn(sampleItem());

        String body = """
                {"name":"Margherita","price":9.50,"category":"Pizza"}
                """;

        mockMvc.perform(post("/api/v1/stores/1/menu-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.storeId").value(1))
                .andExpect(jsonPath("$.price").value(9.50));
    }

    @Test
    void createWithNegativePriceReturns400() throws Exception {
        String body = """
                {"name":"Margherita","price":-1.00}
                """;

        mockMvc.perform(post("/api/v1/stores/1/menu-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[?(@.field=='price')]").exists());
    }

    @Test
    void createWithMissingPriceReturns400() throws Exception {
        String body = """
                {"name":"Margherita"}
                """;

        mockMvc.perform(post("/api/v1/stores/1/menu-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[?(@.field=='price')]").exists());
    }

    @Test
    void searchWithNegativeMinPriceReturns400() throws Exception {
        mockMvc.perform(get("/api/v1/menu-items").param("minPrice", "-5"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors[?(@.field=='minPrice')]").exists());
    }
}
