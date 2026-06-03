package com.septeo.ulyses.technical.test.repository;

import com.septeo.ulyses.technical.test.entity.Sales;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Sales entity.
 */
@Repository
public interface SalesRepository {
    /**
     * Find all sales.
     *
     * @return a list of all sales
     */
    List<Sales> findAll();

    /**
     * Find a sale by its ID.
     *
     * @param id the ID of the sale to find
     * @return an Optional containing the sale if found, or empty if not found
     */
    Optional<Sales> findById(Long id);

    /**
     * Find a page of sales.
     *
     * @param offset the index of the first sale to return
     * @param limit  the maximum number of sales to return
     * @return a list containing at most {@code limit} sales starting at {@code offset}
     */
    List<Sales> findAll(int offset, int limit);

    /**
     * Find all sales for a given brand.
     *
     * @param brandId the ID of the brand
     * @return a list of sales associated with the brand
     */
    List<Sales> findByBrandId(Long brandId);

    /**
     * Find all sales for a given vehicle.
     *
     * @param vehicleId the ID of the vehicle
     * @return a list of sales associated with the vehicle
     */
    List<Sales> findByVehicleId(Long vehicleId);

}
