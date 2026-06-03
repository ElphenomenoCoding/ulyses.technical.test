package com.septeo.ulyses.technical.test.dto;

import com.septeo.ulyses.technical.test.entity.Vehicle;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * Aggregated sales figures for a single vehicle.
 * Used both as the accumulator during aggregation and as the response payload
 * for the best-selling vehicles endpoint.
 */
@Getter
public class VehicleSalesSummary {

    private final Vehicle vehicle;
    private long salesCount;
    private BigDecimal totalRevenue;

    public VehicleSalesSummary(Vehicle vehicle) {
        this.vehicle = vehicle;
        this.totalRevenue = BigDecimal.ZERO;
    }

    /**
     * Add a single sale to this vehicle's running totals.
     *
     * @param price the price of the sale to add
     */
    public void addSale(BigDecimal price) {
        this.salesCount++;
        this.totalRevenue = this.totalRevenue.add(price);
    }
}
