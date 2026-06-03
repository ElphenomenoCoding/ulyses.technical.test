package com.septeo.ulyses.technical.test.controller;

import com.septeo.ulyses.technical.test.dto.VehicleSalesSummary;
import com.septeo.ulyses.technical.test.entity.Sales;
import com.septeo.ulyses.technical.test.service.SalesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/sales")
public class SalesController {

    @Autowired
    private SalesService salesService;

    @GetMapping
    public ResponseEntity<List<Sales>> getAllSales(@RequestParam(required = false) Integer page) {
        return ResponseEntity.ok(salesService.getSalesByPage(page));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Sales> getSalesById(@PathVariable Long id) {
        return salesService.getSalesById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/brands/{brandId}")
    public ResponseEntity<List<Sales>> getSalesByBrand(@PathVariable Long brandId) {
        return ResponseEntity.ok(salesService.getSalesByBrandId(brandId));
    }

    @GetMapping("/vehicles/{vehicleId}")
    public ResponseEntity<List<Sales>> getSalesByVehicle(@PathVariable Long vehicleId) {
        return ResponseEntity.ok(salesService.getSalesByVehicleId(vehicleId));
    }

    @GetMapping("/vehicles/bestSelling")
    public ResponseEntity<List<VehicleSalesSummary>> getBestSellingVehicles(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "startDate must not be after endDate");
        }
        return ResponseEntity.ok(salesService.getBestSellingVehicles(startDate, endDate));
    }

}
