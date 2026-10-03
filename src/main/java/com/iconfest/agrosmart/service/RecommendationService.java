package com.iconfest.agrosmart.service;

import com.iconfest.agrosmart.dto.PredictResponse.Rekomendasi;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class RecommendationService {

    private static final int TOP_N = 3;
    /** Kandidat non-prediksi tidak pernah dinilai lebih tinggi dari 90% hanya karena dekat. */
    private static final double BOBOT_KEMIRIPAN = 0.9;

    /**
     * Tanaman pertama selalu hasil prediksi J48. Dua berikutnya diurutkan menurut skor
     * = p + (1 - p) * 0.9 * kemiripan, dengan p = probabilitas J48 dan kemiripan = kedekatan input
     * ke wilayah keputusan tanaman tersebut di pohon. Tanpa file .tree, hanya tanaman berprobabilitas
     * positif dari J48 yang ditampilkan.
     */
    public List<Rekomendasi> rekomendasi(WekaService.Hasil hasil) {
        int n = hasil.urutanKelas().length;
        double[] skor = new double[n];
        for (int i = 0; i < n; i++) {
            double p = hasil.probabilitas()[i];
            double sim = hasil.kemiripan() != null ? hasil.kemiripan()[i] : 0.0;
            skor[i] = p + (1 - p) * BOBOT_KEMIRIPAN * sim;
        }
        List<Integer> urut = new ArrayList<>();
        for (int i = 0; i < n; i++) urut.add(i);
        urut.sort(Comparator.comparingDouble((Integer i) -> skor[i]).reversed());

        int prediksi = java.util.Arrays.asList(hasil.urutanKelas()).indexOf(hasil.kelas());
        urut.remove(Integer.valueOf(prediksi));
        urut.add(0, prediksi);

        List<Rekomendasi> out = new ArrayList<>();
        for (int idx : urut) {
            if (out.size() >= TOP_N) break;
            if (skor[idx] <= 0 && idx != prediksi) continue;
            CropCatalog.Info t = CropCatalog.cari(hasil.urutanKelas()[idx]);
            out.add(new Rekomendasi(t.nama(), t.ikon(), t.deskripsi(), t.jenisTanah(), t.kebutuhan(),
                    (int) Math.round(skor[idx] * 100)));
        }
        return out;
    }
}
