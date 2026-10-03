package com.iconfest.agrosmart.service;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Basis pengetahuan 22 tanaman pada dataset Crop Recommendation (model Weka).
 * Label kelas dari model (versi Indonesia maupun Inggris) dipetakan ke satu kunci kanonik,
 * sehingga rekomendasi_id.model dan rekomendasi_en.model memberi tampilan yang sama.
 * Isi deskripsi adalah panduan agronomi umum, bukan hasil model.
 */
public final class CropCatalog {

    public record Info(String nama, String ikon, String deskripsi, String jenisTanah, String kebutuhan) {}

    private static final Map<String, Info> INFO = new HashMap<>();
    private static final Map<String, String> ALIAS = new HashMap<>();

    private static void tambah(String kunci, String aliasLain, Info info) {
        INFO.put(kunci, info);
        ALIAS.put(normal(kunci), kunci);
        for (String a : aliasLain.split(",")) if (!a.isBlank()) ALIAS.put(normal(a), kunci);
    }

    static {
        tambah("rice", "nasi,padi", new Info("Padi", "🌾",
            "Tanaman pangan pokok penghasil beras, sumber karbohidrat utama masyarakat Indonesia.",
            "Lempung atau liat berlumpur dan aluvial; lahan sawah yang dapat digenangi.",
            "Air melimpah dan curah hujan tinggi, suhu hangat, sinar matahari cukup, pemupukan N-P-K berimbang."));
        tambah("maize", "jagung", new Info("Jagung", "🌽",
            "Serealia serbaguna untuk pangan, pakan ternak, dan industri.",
            "Gembur dan subur dengan drainase baik; lempung berpasir hingga lempung.",
            "Sinar matahari penuh, pH sekitar 5,5-7, kebutuhan Nitrogen tinggi, tidak tahan tergenang."));
        tambah("chickpea", "kacang_arab", new Info("Kacang Arab", "🫘",
            "Kacang-kacangan sumber protein nabati yang tahan kondisi agak kering.",
            "Lempung berpasir, gembur, drainase sangat baik.",
            "Iklim sejuk-kering, curah hujan rendah, hindari genangan, pH netral hingga agak basa."));
        tambah("kidneybeans", "kacang_merah", new Info("Kacang Merah", "🫘",
            "Kacang berbiji merah yang kaya protein dan serat.",
            "Gembur, kaya bahan organik, drainase baik; dataran sedang hingga tinggi.",
            "Suhu sejuk-sedang, kelembaban cukup, pH sekitar 5,5-6,5, tidak tahan genangan."));
        tambah("pigeonpeas", "kacang_gude", new Info("Kacang Gude", "🌱",
            "Tanaman perdu kacang-kacangan yang tahan kekeringan; polong muda dan bijinya dimakan.",
            "Lempung berpasir hingga lempung dengan drainase baik.",
            "Toleran kering, sinar matahari penuh, curah hujan sedang, mengikat nitrogen dari udara."));
        tambah("mothbeans", "kacang_mat", new Info("Kacang Mat (Moth Bean)", "🫘",
            "Kacang-kacangan yang sangat tahan panas dan kering, cocok untuk lahan marginal.",
            "Berpasir hingga lempung berpasir dengan drainase sangat baik.",
            "Suhu hangat, curah hujan rendah, toleran kekeringan, tidak tahan genangan."));
        tambah("mungbean", "kacang_hijau", new Info("Kacang Hijau", "🫛",
            "Kacang berumur pendek untuk bubur, tauge, dan berbagai olahan.",
            "Lempung berpasir, gembur, drainase baik.",
            "Suhu hangat, sinar matahari penuh, pH sekitar 6-7,5, hindari genangan."));
        tambah("blackgram", "lentil_hitam", new Info("Kacang Hitam (Black Gram)", "🫘",
            "Kacang berbiji hitam kaya protein, umum dipakai dalam masakan Asia Selatan.",
            "Lempung hingga lempung liat dengan drainase baik.",
            "Iklim hangat dan lembab, curah hujan sedang, pH sekitar 6,5-7,5."));
        tambah("lentil", "", new Info("Lentil", "🫘",
            "Kacang kecil berprotein tinggi yang tumbuh di iklim sejuk.",
            "Lempung berpasir hingga lempung dengan drainase baik.",
            "Suhu sejuk, curah hujan rendah-sedang, pH netral, tidak tahan genangan."));
        tambah("pomegranate", "delima", new Info("Delima", "🍒",
            "Pohon buah berbiji merah ranum yang kaya antioksidan.",
            "Lempung berpasir, gembur, drainase baik; cukup toleran tanah kurang subur.",
            "Suhu hangat, sinar matahari penuh, musim kering untuk pembuahan, toleran kekeringan."));
        tambah("banana", "pisang", new Info("Pisang", "🍌",
            "Buah tropis tahunan yang produktif dan mudah dibudidayakan.",
            "Lempung gembur yang kaya bahan organik dengan drainase baik.",
            "Suhu hangat dan lembab, curah hujan merata, kebutuhan Kalium tinggi, lindungi dari angin kencang."));
        tambah("mango", "mangga", new Info("Mangga", "🥭",
            "Pohon buah tropis bernilai ekonomi tinggi.",
            "Lempung berpasir yang dalam dengan drainase baik.",
            "Sinar matahari penuh, suhu hangat, musim kering tegas menjelang berbunga, pH sekitar 5,5-7,5."));
        tambah("grapes", "anggur", new Info("Anggur", "🍇",
            "Tanaman buah merambat yang membutuhkan perawatan intensif.",
            "Lempung berpasir dengan drainase sangat baik.",
            "Sinar matahari melimpah, perlu para-para dan pemangkasan rutin, waspada jamur saat kelembaban tinggi."));
        tambah("watermelon", "semangka", new Info("Semangka", "🍉",
            "Buah merambat berair yang cocok di tanah ringan dan iklim hangat.",
            "Lempung berpasir, gembur, drainase baik.",
            "Suhu hangat, sinar matahari penuh, penyiraman teratur saat buah membesar, pH sekitar 6-7."));
        tambah("muskmelon", "melon", new Info("Melon", "🍈",
            "Buah merambat beraroma manis yang peka terhadap kelembaban berlebih.",
            "Lempung berpasir, gembur, kaya bahan organik.",
            "Suhu hangat, sinar matahari penuh, air cukup tetapi tidak tergenang."));
        tambah("apple", "apel", new Info("Apel", "🍎",
            "Buah beriklim sejuk yang di Indonesia umumnya ditanam di dataran tinggi.",
            "Lempung gembur dan subur dengan drainase baik.",
            "Suhu sejuk (dataran tinggi), kebutuhan Fosfor dan Kalium tinggi, kelembaban cukup, pH sekitar 5,5-6,5."));
        tambah("orange", "jeruk", new Info("Jeruk", "🍊",
            "Tanaman buah sitrus yang kaya vitamin C.",
            "Lempung berpasir hingga lempung dengan drainase baik.",
            "Suhu hangat, sinar matahari cukup, pH sekitar 5,5-7, hindari genangan."));
        tambah("papaya", "pepaya", new Info("Pepaya", "🍑",
            "Tanaman buah cepat berbuah yang tumbuh baik di daerah tropis.",
            "Gembur dan subur dengan drainase sangat baik.",
            "Suhu hangat, sinar matahari penuh, air cukup namun akar sangat peka terhadap genangan."));
        tambah("coconut", "kelapa", new Info("Kelapa", "🥥",
            "Pohon tahunan serbaguna di daerah pesisir tropis.",
            "Berpasir hingga lempung berpasir, termasuk tanah pesisir, dengan drainase baik.",
            "Suhu hangat, kelembaban udara dan curah hujan tinggi, sinar matahari penuh."));
        tambah("cotton", "kapas", new Info("Kapas", "🌼",
            "Tanaman serat penghasil bahan baku tekstil.",
            "Lempung dalam yang subur dengan drainase baik.",
            "Suhu hangat, sinar matahari penuh, musim kering saat panen, pH sekitar 6-8."));
        tambah("jute", "goni", new Info("Goni (Jute)", "🌿",
            "Tanaman serat batang untuk karung dan tekstil kasar.",
            "Aluvial lempung berpasir yang subur dan lembab.",
            "Suhu hangat, kelembaban tinggi, curah hujan melimpah, pH sekitar 6-7,5."));
        tambah("coffee", "kopi", new Info("Kopi", "☕",
            "Tanaman perkebunan penghasil biji kopi, komoditas ekspor penting Indonesia.",
            "Gembur, kaya humus, drainase baik; tanah vulkanik sangat cocok.",
            "Naungan parsial, suhu sejuk-sedang, curah hujan merata, pH sekitar 5,5-6,5."));
    }

    private CropCatalog() {}

    private static String normal(String s) {
        return s.toLowerCase(Locale.ROOT).replaceAll("[^a-z]", "");
    }

    /** Info tanaman untuk label kelas model; label tak dikenal tetap ditampilkan apa adanya. */
    public static Info cari(String labelModel) {
        String kunci = ALIAS.get(normal(labelModel));
        if (kunci != null) return INFO.get(kunci);
        return new Info(labelModel.replace('_', ' '), "🌱",
                "Tanaman hasil prediksi model.", "-", "-");
    }
}
