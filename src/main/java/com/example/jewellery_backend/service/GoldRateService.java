package com.example.jewellery_backend.service;

import com.example.jewellery_backend.model.GoldRate;
import com.example.jewellery_backend.repository.GoldRateRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class GoldRateService {

    private final GoldRateRepository goldRateRepository;

    public GoldRateService(GoldRateRepository goldRateRepository) {
        this.goldRateRepository = goldRateRepository;
    }

    public List<GoldRate> getAllGoldRates() {
        return goldRateRepository.findAll();
    }

    public Optional<GoldRate> getLatestGoldRate() {
        return goldRateRepository.findTopByOrderByDateDesc();
    }

    public GoldRate saveGoldRate(GoldRate goldRate) {
        return goldRateRepository.save(goldRate);
    }
}
