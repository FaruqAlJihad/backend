package com.iconfest.agrosmart.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Salinan pohon keputusan J48 dalam bentuk teks (file .tree, dibuat oleh tools/export_tree.py
 * dari file .model). Dipakai untuk dua hal:
 *  1. memeriksa bahwa pohon ini identik dengan model Weka yang dimuat (self-check saat start),
 *  2. menghitung kemiripan input terhadap wilayah keputusan tiap tanaman, untuk kandidat alternatif.
 * Klasifikasi utama tetap dilakukan oleh Weka.
 */
public final class J48Tree {

    /** Skala normalisasi jarak per fitur (kira-kira rentang data pelatihan Crop Recommendation). */
    private static double skala(String atribut) {
        return switch (atribut.toLowerCase(Locale.ROOT)) {
            case "n", "p" -> 140;
            case "k" -> 200;
            case "temperature", "suhu" -> 35;
            case "humidity", "kelembaban", "kelembapan" -> 85;
            case "ph" -> 6.5;
            case "rainfall", "curah_hujan" -> 280;
            default -> 100;
        };
    }

    /** Lebar toleransi: jarak ternormalisasi 0.03 menurunkan kemiripan menjadi 50%. */
    private static final double TOLERANSI = 0.03;

    private static final class Node {
        int attr = -1;
        double split;
        Node kiri, kanan;
        double[] dist;
    }

    private record Daun(int kelas, double kemurnian, double[] lo, double[] hi) {}

    private final String[] atribut;
    private final String[] kelas;
    private final double[] skalaFitur;
    private final Node root;
    private final List<Daun> daun = new ArrayList<>();

    private J48Tree(String[] atribut, String[] kelas, List<String> baris) {
        this.atribut = atribut;
        this.kelas = kelas;
        this.skalaFitur = new double[atribut.length];
        for (int i = 0; i < atribut.length; i++) skalaFitur[i] = skala(atribut[i]);
        int[] pos = {0};
        double[] lo = new double[atribut.length], hi = new double[atribut.length];
        java.util.Arrays.fill(lo, Double.NEGATIVE_INFINITY);
        java.util.Arrays.fill(hi, Double.POSITIVE_INFINITY);
        this.root = bangun(baris, pos, lo, hi);
    }

    public static J48Tree baca(InputStream in) throws IOException {
        List<String> baris = new ArrayList<>();
        try (BufferedReader r = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String s;
            while ((s = r.readLine()) != null) if (!s.isBlank()) baris.add(s.trim());
        }
        if (baris.size() < 3 || !baris.get(0).startsWith("atribut ") || !baris.get(1).startsWith("kelas ")) {
            throw new IOException("Format file .tree tidak dikenal. Buat ulang dengan tools/export_tree.py");
        }
        String[] atribut = baris.get(0).substring(8).split(",");
        String[] kelas = baris.get(1).substring(6).split(",");
        return new J48Tree(atribut, kelas, baris.subList(2, baris.size()));
    }

    private Node bangun(List<String> baris, int[] pos, double[] lo, double[] hi) {
        String[] t = baris.get(pos[0]++).split(" ");
        Node n = new Node();
        if (t[0].equals("L")) {
            n.dist = new double[t.length - 1];
            double total = 0, maks = -1;
            int best = 0;
            for (int i = 1; i < t.length; i++) {
                n.dist[i - 1] = Double.parseDouble(t[i]);
                total += n.dist[i - 1];
                if (n.dist[i - 1] > maks) { maks = n.dist[i - 1]; best = i - 1; }
            }
            if (total > 0) daun.add(new Daun(best, maks / total, lo.clone(), hi.clone()));
            return n;
        }
        n.attr = Integer.parseInt(t[1]);
        n.split = Double.parseDouble(t[2]);
        double simpanHi = hi[n.attr], simpanLo = lo[n.attr];
        hi[n.attr] = Math.min(hi[n.attr], n.split);          // cabang kiri: nilai <= split
        n.kiri = bangun(baris, pos, lo, hi);
        hi[n.attr] = simpanHi;
        lo[n.attr] = Math.max(lo[n.attr], n.split);          // cabang kanan: nilai > split
        n.kanan = bangun(baris, pos, lo, hi);
        lo[n.attr] = simpanLo;
        return n;
    }

    public String[] atribut() { return atribut; }

    public String[] kelas() { return kelas; }

    /** Distribusi probabilitas kelas (jumlah = 1) untuk vektor fitur x (urutan sesuai atribut()). */
    public double[] distribusi(double[] x) {
        Node n = root;
        while (n.attr >= 0) n = x[n.attr] <= n.split ? n.kiri : n.kanan;
        double total = 0;
        for (double v : n.dist) total += v;
        double[] p = new double[n.dist.length];
        if (total > 0) for (int i = 0; i < p.length; i++) p[i] = n.dist[i] / total;
        return p;
    }

    /** Kemiripan 0..1 input x terhadap wilayah keputusan terdekat tiap kelas (1 = di dalam wilayah). */
    public double[] kemiripan(double[] x) {
        double[] sim = new double[kelas.length];
        for (Daun d : daun) {
            double jumlah = 0;
            for (int f = 0; f < x.length; f++) {
                double lebih = Math.max(Math.max(d.lo()[f] - x[f], x[f] - d.hi()[f]), 0);
                double z = lebih / skalaFitur[f];
                jumlah += z * z;
            }
            double jarak = Math.sqrt(jumlah / x.length);
            double r = jarak / TOLERANSI;
            double nilai = d.kemurnian() / (1 + r * r);
            if (nilai > sim[d.kelas()]) sim[d.kelas()] = nilai;
        }
        return sim;
    }

    /** Cari indeks atribut berdasarkan alias nama (tidak case-sensitive); -1 jika tidak ada. */
    public static int cari(String[] daftar, String... alias) {
        for (String a : alias)
            for (int i = 0; i < daftar.length; i++)
                if (daftar[i].equalsIgnoreCase(a)) return i;
        return -1;
    }
}
