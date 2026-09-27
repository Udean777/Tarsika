<div align="center">

# Tarsika

Galeri foto Android yang bekerja langsung di perangkat.

Foto tetap berada di penyimpanan perangkat. Tarsika tidak mengunggah foto atau data pengguna ke server developer, cloud, atau layanan analitik.

[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-7F52FF?style=flat-square&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-Material_3-4285F4?style=flat-square&logo=android&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Android](https://img.shields.io/badge/Android-API_26%2B-3DDC84?style=flat-square&logo=android&logoColor=white)](https://developer.android.com/)

</div>

---

## Tentang Tarsika

Tarsika adalah galeri foto lokal. Aplikasi membaca foto di perangkat melalui Android MediaStore, lalu menampilkannya berdasarkan tanggal dan album. Tidak perlu akun, koneksi internet, atau konfigurasi server.

## Fitur

- Jelajahi foto dalam tampilan galeri dan album.
- Cari foto berdasarkan nama file, album, tanggal, atau favorit.
- Lihat foto dan detail metadata, lalu bagikan melalui menu berbagi Android.
- Tandai foto favorit dan buat album lokal.
- Edit foto dengan menyimpan hasilnya sebagai salinan.
- Simpan foto pilihan ke album tersembunyi yang dienkripsi di perangkat.
- Kelola foto terpilih, termasuk menghapusnya melalui alur izin Android.

## Privasi dan penyimpanan

**Tarsika tidak mengirim foto, metadata, atau data aplikasi secara diam-diam ke server developer.** Versi aplikasi ini tidak memiliki backend, sinkronisasi cloud, akun pengguna, iklan, atau SDK analitik. Manifest Android juga tidak meminta izin `INTERNET`.

Foto galeri dibaca langsung dari MediaStore dan tidak diunggah. Favorit, album buatan pengguna, serta preferensi tema disimpan secara lokal. Jika pengguna memasukkan foto ke album tersembunyi, Tarsika menyimpan salinan terenkripsi di penyimpanan aplikasi dan mengelola kuncinya melalui Android Keystore.

Tarsika hanya membagikan foto ketika pengguna memilih tindakan **Bagikan** lalu memilih aplikasi tujuan dari menu Android. Setelah itu, aplikasi tujuan mengikuti kebijakan privasinya sendiri.

Android meminta izin sebelum Tarsika membaca galeri. Pada versi Android yang mendukungnya, pengguna dapat memberi akses ke semua foto atau hanya foto tertentu. Tarsika mengikuti pilihan akses tersebut.

## Teknologi

- Kotlin 2.2.10
- Jetpack Compose dan Material 3
- Android MediaStore untuk membaca galeri perangkat
- Room dan DataStore untuk data lokal
- Android Keystore dan enkripsi AES-GCM untuk album tersembunyi
- Coroutines, StateFlow, dan Navigation Compose

## Menjalankan proyek

### Persyaratan

- Android Studio
- JDK 17
- Android SDK compile/target 37
- Emulator atau perangkat Android 8.0 (API 26) atau lebih baru

Tidak diperlukan kredensial backend atau konfigurasi server.

### Build dan pemeriksaan lokal

```bash
./gradlew test ktlintSourceCheck lintDebug compileDebugAndroidTestKotlin assembleDebug
```

Untuk memasang versi debug ke perangkat yang terhubung:

```bash
./gradlew installDebug
```

APK debug tersedia di:

```text
app/build/outputs/apk/debug/app-debug.apk
```

Untuk membuat laporan cakupan unit test:

```bash
./gradlew jacocoTestReport
```

Laporan HTML tersedia di `app/build/reports/jacoco/jacocoTestReport/html/index.html`.

Untuk kompilasi release:

```bash
./gradlew assembleRelease
```

Konfigurasi penandatanganan release harus disediakan di luar repositori.

## Struktur proyek

```text
app/src/
├── main/
│   ├── java/com/ssajudn/tarsika/
│   │   ├── app/di/                  # composition root dan ViewModel factory
│   │   ├── core/ui/                 # komponen UI bersama
│   │   ├── data/local/              # database dan preferensi lokal
│   │   ├── feature/gallery/          # galeri, album, pencarian, vault, dan sampah
│   │   ├── navigation/              # navigasi aplikasi
│   │   └── ui/theme/                # warna, tipografi, dan tema
│   ├── res/                         # string, tema, dan aset launcher
│   └── AndroidManifest.xml
├── test/                            # unit test
└── androidTest/                     # instrumentation test
```

## Lisensi

Repositori ini belum menyertakan lisensi open-source. Kode tetap bersifat proprietari sampai pemilik proyek menambahkan berkas lisensi.
