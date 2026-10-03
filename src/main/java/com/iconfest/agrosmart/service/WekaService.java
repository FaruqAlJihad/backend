package com.iconfest.agrosmart.service;

import com.iconfest.agrosmart.dto.PredictRequest;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import weka.classifiers.Classifier;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SerializationHelper;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

@Service
public class WekaService {

    @Value("${agrosmart.model-path}")
    private String modelPath;

    private Classifier classifier;
    private Instances header;
    private int[] kolomRequest;     // indeks atribut model untuk n, p, k, temperature, humidity, ph, rainfall
    private J48Tree pohon;          // null jika file .tree tidak ada / tidak cocok dengan model

    /** Hasil klasifikasi: label tanaman + distribusi probabilitas (0..1) + kemiripan wilayah (boleh null). */
    public record Hasil(String kelas, Map<String, Double> distribusi, String[] urutanKelas,
                        double[] probabilitas, double[] kemiripan) {
        public double confidence() {
            return distribusi.getOrDefault(kelas, 0.0);
        }
    }

    @PostConstruct
    public void loadModel() throws Exception {
        try (InputStream in = new ClassPathResource(modelPath).getInputStream()) {
            // Model dari Weka Explorer (Save model) berisi: [classifier, header Instances]
            Object[] objs = SerializationHelper.readAll(in);
            classifier = (Classifier) objs[0];
            if (objs.length > 1 && objs[1] instanceof Instances h) {
                header = new Instances(h, 0);
            } else {
                throw new IllegalStateException(
                        "Model tidak memiliki header. Simpan ulang lewat Weka Explorer "
                      + "(klik kanan hasil > Save model).");
            }
            if (header.classIndex() < 0) {
                header.setClassIndex(header.numAttributes() - 1);
            }
        }
        // Urutan masukan sesuai atribut model: nama kolom tidak case-sensitive, urutan bebas.
        kolomRequest = new int[]{
                cari("n"), cari("p"), cari("k"),
                cari("temperature", "suhu"),
                cari("humidity", "kelembaban", "kelembapan"),
                cari("ph"),
                cari("rainfall", "curah_hujan")};
        System.out.println("[Weka] Model dimuat: " + classifier.getClass().getSimpleName()
                + " | kelas = " + header.classAttribute().name()
                + " (" + header.classAttribute().numValues() + " nilai)");
        muatPohon();
    }

    /** Muat file .tree di samping .model dan pastikan hasilnya sama dengan Weka. Jika tidak, nonaktifkan. */
    private void muatPohon() {
        String treePath = modelPath.replaceAll("\\.model$", "") + ".tree";
        ClassPathResource res = new ClassPathResource(treePath);
        if (!res.exists()) {
            System.out.println("[Weka] " + treePath + " tidak ada -> kandidat alternatif dimatikan "
                    + "(jalankan tools/export_tree.py).");
            return;
        }
        try (InputStream in = res.getInputStream()) {
            J48Tree t = J48Tree.baca(in);
            if (!cocokDenganModel(t)) {
                System.out.println("[Weka] PERINGATAN: " + treePath + " tidak sama dengan model -> kandidat "
                        + "alternatif dimatikan. Buat ulang dengan tools/export_tree.py.");
                return;
            }
            pohon = t;
            System.out.println("[Weka] Pohon pendamping cocok dengan model (300 uji acak): " + treePath);
        } catch (Exception e) {
            System.out.println("[Weka] Gagal membaca " + treePath + ": " + e.getMessage());
        }
    }

    private boolean cocokDenganModel(J48Tree t) throws Exception {
        int nf = header.numAttributes() - 1;
        String[] namaModel = new String[nf];
        for (int i = 0; i < nf; i++) namaModel[i] = header.attribute(i).name();
        if (!java.util.Arrays.equals(namaModel, t.atribut())) return false;
        String[] kelasModel = new String[header.classAttribute().numValues()];
        for (int i = 0; i < kelasModel.length; i++) kelasModel[i] = header.classAttribute().value(i);
        if (!java.util.Arrays.equals(kelasModel, t.kelas())) return false;

        Random r = new Random(7);
        for (int i = 0; i < 300; i++) {
            double[] x = new double[nf];
            for (int f = 0; f < nf; f++) x[f] = r.nextDouble() * 300;
            double[] a = distribusiWeka(x);
            double[] b = t.distribusi(x);
            if (terbesar(a) != terbesar(b)) return false;
        }
        return true;
    }

    public Hasil klasifikasi(PredictRequest req) throws Exception {
        double[] nilai = {req.n(), req.p(), req.k(), req.temperature(), req.humidity(), req.ph(), req.rainfall()};
        double[] x = new double[header.numAttributes() - 1];
        for (int i = 0; i < nilai.length; i++) x[kolomRequest[i]] = nilai[i];

        double[] dist = distribusiWeka(x);
        int best = terbesar(dist);

        String[] kelas = new String[dist.length];
        Map<String, Double> map = new LinkedHashMap<>();
        for (int i = 0; i < dist.length; i++) {
            kelas[i] = header.classAttribute().value(i);
            map.put(kelas[i], dist[i]);
        }
        double[] sim = pohon != null ? pohon.kemiripan(x) : null;
        return new Hasil(kelas[best], map, kelas, dist, sim);
    }

    private double[] distribusiWeka(double[] x) throws Exception {
        Instance inst = new DenseInstance(header.numAttributes()); // semua nilai awal = missing
        inst.setDataset(header);
        for (int i = 0; i < x.length; i++) inst.setValue(i, x[i]);
        return classifier.distributionForInstance(inst);
    }

    private static int terbesar(double[] d) {
        int best = 0;
        for (int i = 1; i < d.length; i++) if (d[i] > d[best]) best = i;
        return best;
    }

    private int cari(String... alias) {
        for (String a : alias) {
            for (int i = 0; i < header.numAttributes(); i++) {
                if (i != header.classIndex() && header.attribute(i).name().equalsIgnoreCase(a)) return i;
            }
        }
        throw new IllegalStateException("Atribut tidak ada di model: " + String.join("/", alias)
                + ". Atribut model: " + header.toString().lines().filter(l -> l.startsWith("@attribute"))
                        .reduce((a, b) -> a + ", " + b).orElse("?"));
    }
}
