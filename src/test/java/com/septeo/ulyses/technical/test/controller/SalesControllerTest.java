package com.septeo.ulyses.technical.test.controller;

import com.septeo.ulyses.technical.test.entity.Sales;
import com.septeo.ulyses.technical.test.service.SalesService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SalesController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("SalesController")
class SalesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SalesService salesService;

    @Nested
    @DisplayName("getAllSales")
    class GetAllSales {

        @Test
        @DisplayName("when no page param then forwards a null page to the service")
        void whenNoPageParam_thenForwardsNullPage() throws Exception {
            // Given
            when(salesService.getSalesByPage(null)).thenReturn(List.of());

            // When
            ResultActions response = mockMvc.perform(get("/api/sales"));

            // Then
            response.andExpect(status().isOk());
            verify(salesService).getSalesByPage(null);
        }

        @Test
        @DisplayName("when page param is provided then forwards it to the service")
        void whenPageParamProvided_thenForwardsPage() throws Exception {
            // Given
            when(salesService.getSalesByPage(2)).thenReturn(List.of());

            // When
            ResultActions response = mockMvc.perform(get("/api/sales").param("page", "2"));

            // Then
            response.andExpect(status().isOk());
            verify(salesService).getSalesByPage(2);
        }
    }

    @Nested
    @DisplayName("getSalesById")
    class GetSalesById {

        @Test
        @DisplayName("when the sale exists then returns 200")
        void whenSaleExists_thenReturnsOk() throws Exception {
            // Given
            Sales sale = new Sales();
            sale.setId(1L);
            when(salesService.getSalesById(1L)).thenReturn(Optional.of(sale));

            // When
            ResultActions response = mockMvc.perform(get("/api/sales/1"));

            // Then
            response.andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1));
        }

        @Test
        @DisplayName("when the sale is missing then returns 404")
        void whenSaleMissing_thenReturnsNotFound() throws Exception {
            // Given
            when(salesService.getSalesById(99L)).thenReturn(Optional.empty());

            // When
            ResultActions response = mockMvc.perform(get("/api/sales/99"));

            // Then
            response.andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("getSalesByBrand")
    class GetSalesByBrand {

        @Test
        @DisplayName("when called then returns the sales of the brand")
        void whenCalled_thenReturnsBrandSales() throws Exception {
            // Given
            when(salesService.getSalesByBrandId(2L)).thenReturn(List.of());

            // When
            ResultActions response = mockMvc.perform(get("/api/sales/brands/2"));

            // Then
            response.andExpect(status().isOk());
            verify(salesService).getSalesByBrandId(2L);
        }
    }

    @Nested
    @DisplayName("getSalesByVehicle")
    class GetSalesByVehicle {

        @Test
        @DisplayName("when called then returns the sales of the vehicle")
        void whenCalled_thenReturnsVehicleSales() throws Exception {
            // Given
            when(salesService.getSalesByVehicleId(7L)).thenReturn(List.of());

            // When
            ResultActions response = mockMvc.perform(get("/api/sales/vehicles/7"));

            // Then
            response.andExpect(status().isOk());
            verify(salesService).getSalesByVehicleId(7L);
        }
    }

    @Nested
    @DisplayName("getBestSellingVehicles")
    class GetBestSellingVehicles {

        @Test
        @DisplayName("when no dates then queries without a period filter")
        void whenNoDates_thenQueriesWithoutFilter() throws Exception {
            // Given
            when(salesService.getBestSellingVehicles(null, null)).thenReturn(List.of());

            // When
            ResultActions response = mockMvc.perform(get("/api/sales/vehicles/bestSelling"));

            // Then
            response.andExpect(status().isOk());
            verify(salesService).getBestSellingVehicles(null, null);
        }

        @Test
        @DisplayName("when only the start date is given then forwards start and null end")
        void whenOnlyStartDate_thenForwardsStart() throws Exception {
            // Given
            LocalDate start = LocalDate.of(2025, 1, 1);
            when(salesService.getBestSellingVehicles(start, null)).thenReturn(List.of());

            // When
            ResultActions response = mockMvc.perform(
                    get("/api/sales/vehicles/bestSelling").param("startDate", "2025-01-01"));

            // Then
            response.andExpect(status().isOk());
            verify(salesService).getBestSellingVehicles(start, null);
        }

        @Test
        @DisplayName("when only the end date is given then forwards null start and end")
        void whenOnlyEndDate_thenForwardsEnd() throws Exception {
            // Given
            LocalDate end = LocalDate.of(2025, 1, 31);
            when(salesService.getBestSellingVehicles(null, end)).thenReturn(List.of());

            // When
            ResultActions response = mockMvc.perform(
                    get("/api/sales/vehicles/bestSelling").param("endDate", "2025-01-31"));

            // Then
            response.andExpect(status().isOk());
            verify(salesService).getBestSellingVehicles(null, end);
        }

        @Test
        @DisplayName("when the range is valid then forwards both dates")
        void whenValidRange_thenForwardsBothDates() throws Exception {
            // Given
            LocalDate start = LocalDate.of(2025, 1, 1);
            LocalDate end = LocalDate.of(2025, 1, 31);
            when(salesService.getBestSellingVehicles(start, end)).thenReturn(List.of());

            // When
            ResultActions response = mockMvc.perform(get("/api/sales/vehicles/bestSelling")
                    .param("startDate", "2025-01-01")
                    .param("endDate", "2025-01-31"));

            // Then
            response.andExpect(status().isOk());
            verify(salesService).getBestSellingVehicles(start, end);
        }

        @Test
        @DisplayName("when the start date is after the end date then returns 400")
        void whenStartAfterEnd_thenReturnsBadRequest() throws Exception {
            // Given
            String start = "2025-01-31";
            String end = "2025-01-01";

            // When
            ResultActions response = mockMvc.perform(get("/api/sales/vehicles/bestSelling")
                    .param("startDate", start)
                    .param("endDate", end));

            // Then
            response.andExpect(status().isBadRequest());
            verifyNoInteractions(salesService);
        }
    }
}
