# AgroSmart Backend (Spring Boot + Weka J48)

Backend SPK rekomendasi tanaman - bagian Faruq.

## Menjalankan
Model Fazlee sudah ada di `src/main/resources/models/` (default: `rekomendasi_id.model`).

    mvn spring-boot:run

Tes:

    curl -X POST http://localhost:8080/api/predict \
      -H "Content-Type: application/json" \
      -d '{"n":90,"p":42,"k":43,"temperature":20.9,"humidity":82,"ph":6.5,"rainfall":203}'

## Model
- J48 dengan 7 atribut (N, P, K, temperature, humidity, ph, rainfall) dan kelas `label` berisi 22 tanaman.
- `rekomendasi_id.model` berlabel tanaman Indonesia, `rekomendasi_en.model` berlabel Inggris. Ganti lewat `agrosmart.model-path`.
- Setiap `.model` punya pendamping `.tree` (salinan pohon) yang dipakai untuk 2 kandidat alternatif dan self-check saat start.
  Jika model diganti: `python3 ../tools/export_tree.py src/main/resources/models/<nama>.model`.
- Simpan model lewat Weka Explorer (klik kanan hasil > Save model) agar header atribut ikut tersimpan.
- Versi Weka sebaiknya 3.8.x (`pom.xml` memakai 3.8.6).
- Teks deskripsi tanaman ada di `service/CropCatalog.java`.
# backend-iconfest
# backend-iconfest
