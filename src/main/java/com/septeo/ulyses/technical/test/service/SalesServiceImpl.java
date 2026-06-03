package com.septeo.ulyses.technical.test.service;

import com.septeo.ulyses.technical.test.dto.VehicleSalesSummary;
import com.septeo.ulyses.technical.test.entity.Sales;
import com.septeo.ulyses.technical.test.entity.Vehicle;
import com.septeo.ulyses.technical.test.repository.SalesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Implementation of the SalesService interface.
 * This class provides the implementation for all sales-related operations.
 */
@Service
@Transactional(readOnly = false)
public class SalesServiceImpl implements SalesService {

    private static final int PAGE_SIZE = 10;
    private static final int FIRST_PAGE = 1;
    private static final int TOP_VEHICLES_LIMIT = 5;

    @Autowired
    private SalesRepository salesRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Sales> getAllSales() {
        return salesRepository.findAll();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Sales> getSalesById(Long id) {
        return salesRepository.findById(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Sales> getSalesByPage(Integer page) {
        int offset = (resolvePage(page) - FIRST_PAGE) * PAGE_SIZE;
        return salesRepository.findAll(offset, PAGE_SIZE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Sales> getSalesByBrandId(Long brandId) {
        return salesRepository.findByBrandId(brandId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Sales> getSalesByVehicleId(Long vehicleId) {
        return salesRepository.findByVehicleId(vehicleId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<VehicleSalesSummary> getBestSellingVehicles(LocalDate startDate, LocalDate endDate) {
        Map<Long, VehicleSalesSummary> summariesByVehicle = aggregateSales(startDate, endDate);
        return selectTop(summariesByVehicle.values(), TOP_VEHICLES_LIMIT);
    }

    /**
     * Aggregate sales into per-vehicle summaries, keeping only sales within the given period.
     *
     * @param startDate the inclusive start of the period, or {@code null} for no lower bound
     * @param endDate   the inclusive end of the period, or {@code null} for no upper bound
     * @return the sales summaries keyed by vehicle id
     */
    private Map<Long, VehicleSalesSummary> aggregateSales(LocalDate startDate, LocalDate endDate) {
        Map<Long, VehicleSalesSummary> summariesByVehicle = new HashMap<>();
        for (Sales sale : salesRepository.findAll()) {
            if (!withinPeriod(sale, startDate, endDate)) {
                continue;
            }
            Vehicle vehicle = sale.getVehicle();
            summariesByVehicle
                    .computeIfAbsent(vehicle.getId(), id -> new VehicleSalesSummary(vehicle))
                    .addSale(sale.getPrice());
        }
        return summariesByVehicle;
    }

    /**
     * Check whether a sale falls within the optional date bounds.
     *
     * @param sale      the sale to test
     * @param startDate the inclusive lower bound, or {@code null} for none
     * @param endDate   the inclusive upper bound, or {@code null} for none
     * @return {@code true} if the sale date is within the bounds
     */
    private boolean withinPeriod(Sales sale, LocalDate startDate, LocalDate endDate) {
        LocalDate saleDate = sale.getSaleDate();
        if (startDate != null && saleDate.isBefore(startDate)) {
            return false;
        }
        return endDate == null || !saleDate.isAfter(endDate);
    }

    /**
     * Select the best summaries without fully sorting the input. A single pass keeps only the K best
     * in a fixed-size array (O(n*k); with k constant this is effectively O(n) time and O(k) memory).
     * Sorting every aggregated vehicle would be O(n log n) and wasteful when only a handful are
     * returned, which matters once the data reaches millions of records.
     *
     * @param summaries the aggregated vehicle summaries
     * @param limit     the maximum number of summaries to return
     * @return the top summaries, best first
     */
    private List<VehicleSalesSummary> selectTop(Collection<VehicleSalesSummary> summaries, int limit) {
        VehicleSalesSummary[] top = new VehicleSalesSummary[limit];
        int size = 0;
        for (VehicleSalesSummary candidate : summaries) {
            size = insertIfAmongTop(top, size, limit, candidate);
        }
        return toList(top, size);
    }

    /**
     * Insert a candidate into the fixed-size top array if it ranks among the current best.
     *
     * @param top       the current best summaries, ordered best first
     * @param size      the number of slots currently filled
     * @param limit     the capacity of the array
     * @param candidate the summary to consider
     * @return the number of filled slots after the candidate is considered
     */
    private int insertIfAmongTop(VehicleSalesSummary[] top, int size, int limit, VehicleSalesSummary candidate) {
        if (size == limit && !ranksHigher(candidate, top[size - 1])) {
            return size;
        }
        int position = size < limit ? size : limit - 1;
        while (position > 0 && ranksHigher(candidate, top[position - 1])) {
            top[position] = top[position - 1];
            position--;
        }
        top[position] = candidate;
        return size < limit ? size + 1 : size;
    }

    /**
     * Compare two summaries by sales count, then total revenue, then vehicle id.
     *
     * @param a the first summary
     * @param b the second summary
     * @return {@code true} if {@code a} ranks strictly higher than {@code b}
     */
    private boolean ranksHigher(VehicleSalesSummary a, VehicleSalesSummary b) {
        if (a.getSalesCount() != b.getSalesCount()) {
            return a.getSalesCount() > b.getSalesCount();
        }
        int revenueComparison = a.getTotalRevenue().compareTo(b.getTotalRevenue());
        if (revenueComparison != 0) {
            return revenueComparison > 0;
        }
        return a.getVehicle().getId() < b.getVehicle().getId();
    }

    /**
     * Copy the filled slots of the top array into a list.
     *
     * @param top  the array of best summaries
     * @param size the number of filled slots
     * @return a list containing the first {@code size} summaries
     */
    private List<VehicleSalesSummary> toList(VehicleSalesSummary[] top, int size) {
        List<VehicleSalesSummary> result = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            result.add(top[i]);
        }
        return result;
    }

    /**
     * Resolve the effective page number, defaulting to the first page when absent or invalid.
     *
     * @param page the requested page number, possibly {@code null}
     * @return the page number to use, never lower than the first page
     */
    private int resolvePage(Integer page) {
        if (page == null || page < FIRST_PAGE) {
            return FIRST_PAGE;
        }
        return page;
    }

}
