package com.iconfest.agrosmart.controller;

import com.iconfest.agrosmart.dto.PredictRequest;
import com.iconfest.agrosmart.dto.PredictResponse;
import com.iconfest.agrosmart.service.CropCatalog;
import com.iconfest.agrosmart.service.RecommendationService;
import com.iconfest.agrosmart.service.WekaService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class PredictController {

    private final WekaService weka;
    private final RecommendationService rekomendasi;

    public PredictController(WekaService weka, RecommendationService rekomendasi) {
        this.weka = weka;
        this.rekomendasi = rekomendasi;
    }

    @PostMapping("/predict")
    public PredictResponse predict(@Valid @RequestBody PredictRequest req) throws Exception {
        WekaService.Hasil hasil = weka.klasifikasi(req);

        // Probabilitas > 0 saja, diurutkan menurun, memakai nama tampilan tanaman.
        Map<String, Double> persen = new LinkedHashMap<>();
        hasil.distribusi().entrySet().stream()
                .filter(e -> e.getValue() > 0)
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .forEach(e -> persen.put(CropCatalog.cari(e.getKey()).nama(),
                        Math.round(e.getValue() * 1000) / 10.0));

        return new PredictResponse(
                CropCatalog.cari(hasil.kelas()).nama(),
                hasil.kelas(),
                Math.round(hasil.confidence() * 1000) / 10.0,
                persen,
                rekomendasi.rekomendasi(hasil)
        );
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "ok");
    }
}
