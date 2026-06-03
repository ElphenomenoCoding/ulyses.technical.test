package com.septeo.ulyses.technical.test.service;

import com.septeo.ulyses.technical.test.cache.ExpiringCache;
import com.septeo.ulyses.technical.test.entity.Brand;
import com.septeo.ulyses.technical.test.repository.BrandRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Implementation of the BrandService interface.
 * This class provides the implementation for all brand-related operations.
 */
@Service
@Transactional(readOnly = false)
public class BrandServiceImpl implements BrandService {

    private static final long CACHE_TTL_MILLIS = 30_000L;
    private static final String ALL_BRANDS_KEY = "ALL";

    private final ExpiringCache<String, List<Brand>> allBrandsCache = new ExpiringCache<>(CACHE_TTL_MILLIS);
    private final ExpiringCache<Long, Optional<Brand>> brandByIdCache = new ExpiringCache<>(CACHE_TTL_MILLIS);

    @Autowired
    private BrandRepository brandRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Brand> getAllBrands() {
        return allBrandsCache.get(ALL_BRANDS_KEY, brandRepository::findAll);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Brand> getBrandById(Long id) {
        return brandByIdCache.get(id, () -> brandRepository.findById(id));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Brand saveBrand(Brand brand) {
        Brand savedBrand = brandRepository.save(brand);
        invalidateCache();
        return savedBrand;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteBrand(Long id) {
        brandRepository.deleteById(id);
        invalidateCache();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void invalidateCache() {
        allBrandsCache.invalidateAll();
        brandByIdCache.invalidateAll();
    }
}
