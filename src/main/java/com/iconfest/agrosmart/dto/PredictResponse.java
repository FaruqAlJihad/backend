package com.iconfest.agrosmart.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

public record PredictResponse(
        @JsonProperty("tanaman_terprediksi") String tanamanTerprediksi,
        @JsonProperty("label_model") String labelModel,
        double confidence,
        Map<String, Double> probabilitas,
        @JsonProperty("rekomendasi_tanaman") List<Rekomendasi> rekomendasiTanaman
) {
    /** Field disesuaikan dengan yang dibaca frontend (App.jsx). */
    public record Rekomendasi(
            String nama,
            String ikon,
            String deskripsi,
            @JsonProperty("jenis_tanah") String jenisTanah,
            String kebutuhan,
            int kecocokan
    ) {}
}
