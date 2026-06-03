package com.septeo.ulyses.technical.test.service;

import com.septeo.ulyses.technical.test.entity.Vehicle;
import com.septeo.ulyses.technical.test.repository.VehicleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Implementation of the VehicleService interface.
 * This class provides the implementation for all vehicle-related operations.
 */
@Service
@Transactional(readOnly = false)
public class VehicleServiceImpl implements VehicleService {

    @Autowired
    private VehicleRepository vehicleRepository;
    @Autowired
    private BrandService brandService;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Vehicle> getAllVehicles() {
        return vehicleRepository.findAll();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Vehicle> getVehicleById(Long id) {
        return vehicleRepository.findById(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Vehicle saveVehicle(Vehicle vehicle) {
        Vehicle savedVehicle = vehicleRepository.save(vehicle);
        brandService.invalidateCache();
        return savedVehicle;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteVehicle(Long id) {
        vehicleRepository.deleteById(id);
        brandService.invalidateCache();
    }
}
