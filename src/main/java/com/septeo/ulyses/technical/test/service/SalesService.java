package com.septeo.ulyses.technical.test.service;

import com.septeo.ulyses.technical.test.dto.VehicleSalesSummary;
import com.septeo.ulyses.technical.test.entity.Sales;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service interface for Sales operations.
 */
public interface SalesService {

    /**
     * Get all sales.
     *
     * @return a list of all sales
     */
    List<Sales> getAllSales();

    /**
     * Get a sales by its ID.
     *
     * @param id the ID of the sales to find
     * @return an Optional containing the sales if found, or empty if not found
     */
    Optional<Sales> getSalesById(Long id);

    /**
     * Get a single page of sales, with a fixed page size.
     *
     * @param page the 1-based page number; if {@code null} or lower than the first page,
     *             the first page is returned
     * @return the list of sales for the requested page
     */
    List<Sales> getSalesByPage(Integer page);

    /**
     * Get all sales for a given brand.
     *
     * @param brandId the ID of the brand
     * @return the list of sales associated with the brand
     */
    List<Sales> getSalesByBrandId(Long brandId);

    /**
     * Get all sales for a given vehicle.
     *
     * @param vehicleId the ID of the vehicle
     * @return the list of sales associated with the vehicle
     */
    List<Sales> getSalesByVehicleId(Long vehicleId);

    /**
     * Get the top best-selling vehicles, ranked by sales count then total revenue.
     *
     * @param startDate the inclusive start of the sales period, or {@code null} for no lower bound
     * @param endDate   the inclusive end of the sales period, or {@code null} for no upper bound
     * @return the best-selling vehicle summaries, best first
     */
    List<VehicleSalesSummary> getBestSellingVehicles(LocalDate startDate, LocalDate endDate);

}
