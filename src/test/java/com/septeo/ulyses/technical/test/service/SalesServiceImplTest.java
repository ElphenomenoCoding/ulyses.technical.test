package com.septeo.ulyses.technical.test.service;

import com.septeo.ulyses.technical.test.dto.VehicleSalesSummary;
import com.septeo.ulyses.technical.test.entity.Sales;
import com.septeo.ulyses.technical.test.entity.Vehicle;
import com.septeo.ulyses.technical.test.repository.SalesRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SalesServiceImpl")
class SalesServiceImplTest {

    private static final LocalDate ANY_DATE = LocalDate.of(2025, 1, 1);

    @Mock
    private SalesRepository salesRepository;

    @InjectMocks
    private SalesServiceImpl salesService;

    @Nested
    @DisplayName("getAllSales")
    class GetAllSales {

        @Test
        @DisplayName("when called then returns all sales from the repository")
        void whenCalled_thenReturnsAllSales() {
            // Given
            List<Sales> all = List.of(new Sales());
            when(salesRepository.findAll()).thenReturn(all);

            // When
            List<Sales> result = salesService.getAllSales();

            // Then
            assertThat(result).isSameAs(all);
        }
    }

    @Nested
    @DisplayName("getSalesById")
    class GetSalesById {

        @Test
        @DisplayName("when the sale exists then returns it")
        void whenSaleExists_thenReturnsIt() {
            // Given
            Sales sale = new Sales();
            when(salesRepository.findById(1L)).thenReturn(Optional.of(sale));

            // When
            Optional<Sales> result = salesService.getSalesById(1L);

            // Then
            assertThat(result).contains(sale);
        }
    }

    @Nested
    @DisplayName("getSalesByPage")
    class GetSalesByPage {

        @Test
        @DisplayName("when page is null then returns the first page")
        void whenPageIsNull_thenReturnsFirstPage() {
            // Given
            List<Sales> firstPage = List.of(new Sales());
            when(salesRepository.findAll(0, 10)).thenReturn(firstPage);

            // When
            List<Sales> result = salesService.getSalesByPage(null);

            // Then
            assertThat(result).isSameAs(firstPage);
            verify(salesRepository).findAll(0, 10);
        }

        @Test
        @DisplayName("when page is below the first page then falls back to the first page")
        void whenPageBelowFirst_thenReturnsFirstPage() {
            // Given
            List<Sales> firstPage = List.of(new Sales());
            when(salesRepository.findAll(0, 10)).thenReturn(firstPage);

            // When
            List<Sales> result = salesService.getSalesByPage(0);

            // Then
            assertThat(result).isSameAs(firstPage);
            verify(salesRepository).findAll(0, 10);
        }

        @Test
        @DisplayName("when page is valid then applies the matching offset")
        void whenPageIsValid_thenAppliesOffset() {
            // Given
            List<Sales> thirdPage = List.of(new Sales());
            when(salesRepository.findAll(20, 10)).thenReturn(thirdPage);

            // When
            List<Sales> result = salesService.getSalesByPage(3);

            // Then
            assertThat(result).isSameAs(thirdPage);
            verify(salesRepository).findAll(20, 10);
        }
    }

    @Nested
    @DisplayName("getSalesByBrandId")
    class GetSalesByBrandId {

        @Test
        @DisplayName("when called then returns the sales of the brand")
        void whenCalled_thenReturnsBrandSales() {
            // Given
            List<Sales> brandSales = List.of(new Sales());
            when(salesRepository.findByBrandId(2L)).thenReturn(brandSales);

            // When
            List<Sales> result = salesService.getSalesByBrandId(2L);

            // Then
            assertThat(result).isSameAs(brandSales);
        }
    }

    @Nested
    @DisplayName("getSalesByVehicleId")
    class GetSalesByVehicleId {

        @Test
        @DisplayName("when called then returns the sales of the vehicle")
        void whenCalled_thenReturnsVehicleSales() {
            // Given
            List<Sales> vehicleSales = List.of(new Sales());
            when(salesRepository.findByVehicleId(7L)).thenReturn(vehicleSales);

            // When
            List<Sales> result = salesService.getSalesByVehicleId(7L);

            // Then
            assertThat(result).isSameAs(vehicleSales);
        }
    }

    @Nested
    @DisplayName("getBestSellingVehicles")
    class GetBestSellingVehicles {

        @Test
        @DisplayName("when there are no sales then returns an empty list")
        void whenNoSales_thenReturnsEmptyList() {
            // Given
            when(salesRepository.findAll()).thenReturn(List.of());

            // When
            List<VehicleSalesSummary> result = salesService.getBestSellingVehicles(null, null);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("when fewer vehicles than the limit then ranks them by sales count")
        void whenFewerThanLimit_thenRanksByCount() {
            // Given
            List<Sales> sales = new ArrayList<>();
            addSales(sales, vehicle(1), 3, "100", ANY_DATE);
            addSales(sales, vehicle(2), 1, "100", ANY_DATE);
            addSales(sales, vehicle(3), 2, "100", ANY_DATE);
            when(salesRepository.findAll()).thenReturn(sales);

            // When
            List<VehicleSalesSummary> result = salesService.getBestSellingVehicles(null, null);

            // Then
            assertThat(result).extracting(s -> s.getVehicle().getId()).containsExactly(1L, 3L, 2L);
            assertThat(result).extracting(VehicleSalesSummary::getSalesCount).containsExactly(3L, 2L, 1L);
        }

        @Test
        @DisplayName("when sales counts are equal then breaks ties by total revenue")
        void whenEqualCounts_thenBreaksTieByRevenue() {
            // Given
            List<Sales> sales = new ArrayList<>();
            addSales(sales, vehicle(1), 1, "300", ANY_DATE);
            addSales(sales, vehicle(2), 1, "100", ANY_DATE);
            addSales(sales, vehicle(3), 1, "200", ANY_DATE);
            when(salesRepository.findAll()).thenReturn(sales);

            // When
            List<VehicleSalesSummary> result = salesService.getBestSellingVehicles(null, null);

            // Then
            assertThat(result).extracting(s -> s.getVehicle().getId()).containsExactly(1L, 3L, 2L);
        }

        @Test
        @DisplayName("when count and revenue are equal then breaks ties by vehicle id")
        void whenEqualCountAndRevenue_thenBreaksTieByVehicleId() {
            // Given
            List<Sales> sales = new ArrayList<>();
            addSales(sales, vehicle(5), 1, "100", ANY_DATE);
            addSales(sales, vehicle(2), 1, "100", ANY_DATE);
            addSales(sales, vehicle(9), 1, "100", ANY_DATE);
            when(salesRepository.findAll()).thenReturn(sales);

            // When
            List<VehicleSalesSummary> result = salesService.getBestSellingVehicles(null, null);

            // Then
            assertThat(result).extracting(s -> s.getVehicle().getId()).containsExactly(2L, 5L, 9L);
        }

        @Test
        @DisplayName("when more vehicles than the limit then returns only the top five")
        void whenMoreThanLimit_thenReturnsTopFive() {
            // Given
            List<Sales> sales = new ArrayList<>();
            addSales(sales, vehicle(1), 6, "100", ANY_DATE);
            addSales(sales, vehicle(2), 5, "100", ANY_DATE);
            addSales(sales, vehicle(3), 4, "100", ANY_DATE);
            addSales(sales, vehicle(4), 3, "100", ANY_DATE);
            addSales(sales, vehicle(5), 2, "100", ANY_DATE);
            addSales(sales, vehicle(6), 1, "100", ANY_DATE);
            addSales(sales, vehicle(7), 7, "100", ANY_DATE);
            when(salesRepository.findAll()).thenReturn(sales);

            // When
            List<VehicleSalesSummary> result = salesService.getBestSellingVehicles(null, null);

            // Then
            assertThat(result).hasSize(5);
            assertThat(result).extracting(s -> s.getVehicle().getId()).containsExactly(7L, 1L, 2L, 3L, 4L);
        }

        @Test
        @DisplayName("when a date range is given then excludes sales outside the period")
        void whenDateRangeGiven_thenExcludesSalesOutsidePeriod() {
            // Given
            LocalDate start = LocalDate.of(2025, 1, 10);
            LocalDate end = LocalDate.of(2025, 1, 20);
            Vehicle inRange = vehicle(1);
            Vehicle outOfRange = vehicle(2);
            List<Sales> sales = new ArrayList<>();
            sales.add(sale(inRange, LocalDate.of(2025, 1, 5), "100"));
            sales.add(sale(inRange, LocalDate.of(2025, 1, 15), "100"));
            sales.add(sale(outOfRange, LocalDate.of(2025, 1, 25), "100"));
            when(salesRepository.findAll()).thenReturn(sales);

            // When
            List<VehicleSalesSummary> result = salesService.getBestSellingVehicles(start, end);

            // Then
            assertThat(result).extracting(s -> s.getVehicle().getId()).containsExactly(1L);
            assertThat(result).extracting(VehicleSalesSummary::getSalesCount).containsExactly(1L);
        }
    }

    private Vehicle vehicle(long id) {
        Vehicle vehicle = new Vehicle();
        vehicle.setId(id);
        vehicle.setModel("model-" + id);
        return vehicle;
    }

    private Sales sale(Vehicle vehicle, LocalDate date, String price) {
        Sales sale = new Sales();
        sale.setVehicle(vehicle);
        sale.setSaleDate(date);
        sale.setPrice(new BigDecimal(price));
        return sale;
    }

    private void addSales(List<Sales> target, Vehicle vehicle, int count, String price, LocalDate date) {
        for (int i = 0; i < count; i++) {
            target.add(sale(vehicle, date, price));
        }
    }
}
