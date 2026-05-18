# 📚 StudyHub

> Aplikasi Manajemen Tugas Mahasiswa dengan integrasi AI

---

## 👥 Anggota Tim

| Nama | NIM |
|------|-----|
| Maxavier Girvanus Manurung | 123140191 |
| Muhammad Rafiq Ridho | 123140197 |

---

## 📖 Tentang StudyHub

**StudyHub** adalah aplikasi manajemen tugas yang dirancang khusus untuk meningkatkan produktivitas mahasiswa. StudyHub mengadopsi arsitektur **Clean Architecture + MVVM** yang memisahkan logika bisnis, data, dan tampilan secara jelas, serta mengintegrasikan **Groq AI** untuk menghadirkan fitur-fitur cerdas yang membantu mahasiswa mengelola waktu dan prioritas belajar mereka.

### Tujuan Aplikasi

- Membantu mahasiswa melacak tugas dan deadline secara terorganisir
- Mengurangi keterlambatan pengumpulan tugas dengan sistem reminder otomatis
- Memberikan rekomendasi prioritas tugas yang cerdas berbasis AI
- Menyediakan tampilan kalender terintegrasi untuk perencanaan akademik

---

## ✨ Fitur Aplikasi

### 🔐 Autentikasi

- **Login** — Masuk ke akun menggunakan email dan password via Firebase Authentication.
- **Register** — Pendaftaran akun baru dengan validasi data pengguna. Sesi pengguna tersimpan otomatis sehingga tidak perlu login ulang setiap saat.

### ✅ Manajemen Tugas

- **Tambah Tugas** — Menambahkan tugas baru lengkap dengan judul, mata kuliah, deskripsi, dan tingkat kesulitan.
- **Edit & Hapus Tugas** — Mengubah detail tugas atau menghapus tugas yang sudah tidak relevan.
- **Status Selesai / Belum** — Menandai tugas sebagai selesai dengan satu klik. Tugas yang sudah selesai akan diarsipkan secara otomatis.

### 📅 Deadline & Kalender

- **Deadline Tugas** — Setiap tugas memiliki tanggal dan waktu deadline yang wajib diisi sebagai acuan pengerjaan.
- **Kalender Tugas** — Tampilan kalender bulanan yang menampilkan semua tugas berdasarkan tanggal deadline, memudahkan mahasiswa dalam merencanakan jadwal belajar mingguan maupun bulanan.

### 🔔 Reminder Notifikasi

Notifikasi push otomatis dikirimkan sebelum deadline tugas tiba. Pengingat dapat dikustomisasi sesuai preferensi pengguna, misalnya H-1 hari atau H-3 jam sebelum deadline. Fitur ini bekerja di Android maupun iOS.

---

## 🤖 Fitur AI — Powered by Gemini

### 🎯 Smart Priority

Fitur **Smart Priority** memanfaatkan groq AI untuk menganalisis seluruh daftar tugas mahasiswa dan menghasilkan urutan prioritas pengerjaan yang optimal.

groq menganalisis deadline, estimasi waktu pengerjaan, dan distribusi tugas per mata kuliah agar tidak menumpuk di hari yang sama. Hasilnya berupa rekomendasi urutan tugas beserta alasan singkat mengapa tugas tersebut perlu didahulukan, sehingga mahasiswa tidak perlu lagi bingung harus mulai dari mana.

### ⏰ Smart Reminder

Fitur **Smart Reminder** menggunakan groq AI untuk menentukan waktu pengingat yang adaptif dan dipersonalisasi, bukan sekadar interval waktu tetap.

AI menganalisis riwayat penyelesaian tugas mahasiswa — apakah cenderung mengerjakan jauh-jauh hari atau mendekati deadline — lalu mempertimbangkan kompleksitas tugas untuk menghasilkan jadwal reminder yang paling efektif bagi masing-masing pengguna. Dengan cara ini, reminder yang diterima terasa lebih relevan dan tepat waktu.

---
## 📁 Struktur Proyek
```
composeApp/src/commonMain/kotlin/com/studyhub/
├── core/ 
│   ├── di/                        
│   │   ├── KoinSetup.kt            
│   │   └── AppModule.kt            
│   ├── network/
│   │   ├── ApiConfig.kt            
│   │   └── HttpClientFactory.kt    
│   └── util/                       
├── data/
│   ├── local/                      
│   ├── remote/                     
│   └── repository/                 
├── domain/
│   ├── model/                      
│   │   ├── Task.kt                
│   │   ├── User.kt                 
│   │   ├── Subject.kt              
│   │   └── UserPreferences.kt      
│   ├── repository/                
│   └── usecase/                    
└── presentation/
├── navigation/                
├── screens/                   
├── components/            
└── theme/                     

---

## 🚧 Sprint Progress

| Sprint | Status | Keterangan |
|--------|--------|------------|
| Sprint 1 | ✅ 100% | Foundation, Clean Architecture, DI, CI |
| Sprint 2 | 🔄 In Progress | Domain models selesai, lanjut use cases |
| Sprint 3 | 🔄 25% | Network layer (Groq API) sudah disiapkan |
| Sprint 4 | ❌ Belum | Polish, tests, coverage |
```
---

## 🗂️ Domain Models

### Task
Representasi tugas mahasiswa di domain layer.

| Field | Tipe | Keterangan |
|-------|------|------------|
| id | String | UUID unik |
| userId | String | ID pemilik tugas |
| title | String | Judul tugas |
| description | String | Deskripsi tugas |
| subject | String | Nama mata kuliah |
| priority | Priority | HIGH / MEDIUM / LOW |
| status | TaskStatus | TODO / IN_PROGRESS / DONE |
| dueDate | Long | Deadline dalam epoch millis |
| dueTime | String? | Waktu deadline format HH:mm |
| tags | List<String> | Label tambahan |
| estimatedMinutes | Int | Estimasi waktu pengerjaan |
| isDeleted | Boolean | Soft delete flag |
| completedAt | Long? | Waktu selesai epoch millis |
| createdAt | Long | Waktu dibuat epoch millis |
| updatedAt | Long | Waktu diubah epoch millis |

**Enums:**
- `Priority` → HIGH, MEDIUM, LOW
- `TaskStatus` → TODO, IN_PROGRESS, DONE
- `SortBy` → DUE_DATE, PRIORITY, SUBJECT, TITLE

---
