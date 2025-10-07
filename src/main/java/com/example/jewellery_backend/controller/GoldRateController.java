package com.example.jewellery_backend.controller;

import com.example.jewellery_backend.model.GoldRate;
import com.example.jewellery_backend.service.GoldRateService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/gold-rates")
public class GoldRateController {

    private final GoldRateService goldRateService;

    public GoldRateController(GoldRateService goldRateService) {
        this.goldRateService = goldRateService;
    }

    @GetMapping
    public List<GoldRate> getAllGoldRates() {
        return goldRateService.getAllGoldRates();
    }

    @GetMapping("/latest")
    public GoldRate getLatestGoldRate() {
        return goldRateService.getLatestGoldRate()
                .orElseThrow(() -> new RuntimeException("No gold rate found"));
    }

    @PostMapping
    public GoldRate createGoldRate(@RequestBody GoldRate goldRate) {
        return goldRateService.saveGoldRate(goldRate);
    }
}
