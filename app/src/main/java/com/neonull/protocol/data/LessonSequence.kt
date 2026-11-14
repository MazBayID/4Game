package com.neonull.protocol.data

import com.neonull.protocol.localization.LocalizedText

enum class LessonPhase {
    LEARN,
    OBSERVE,
    PRACTICE,
    CHALLENGE,
    OPERATION,
    MASTERY
}

data class LessonStep(
    val phase: LessonPhase,
    val title: LocalizedText,
    val instruction: LocalizedText,
    val code: String = "",
    val acceptanceCriteria: LocalizedText = LocalizedText("", "", "")
)

private data class ExerciseSpec(
    val practice: LessonStep,
    val challenge: LessonStep,
    val operation: LessonStep,
    val mastery: LessonStep
)

/**
 * Curated learning sequence for the seven original story missions.
 *
 * This is deliberately content-first: only the original seven module objectives
 * currently have executable grader contracts. The phase exercises below remain
 * instructional content until each receives its own contract and acceptance tests.
 */
object LessonSequenceRepository {
    private fun text(en: String, id: String, ph: String = en) =
        LocalizedText(en = en, id = id, ph = ph)

    private fun step(
        phase: LessonPhase,
        enTitle: String,
        idTitle: String,
        enInstruction: String,
        idInstruction: String,
        code: String,
        enCriteria: String,
        idCriteria: String
    ) = LessonStep(
        phase = phase,
        title = text(enTitle, idTitle),
        instruction = text(enInstruction, idInstruction),
        code = code,
        acceptanceCriteria = text(enCriteria, idCriteria)
    )

    private val exercises = mapOf(
        1 to ExerciseSpec(
            practice = step(LessonPhase.PRACTICE, "Identity drill", "Latihan identitas",
                "Create agent and clearance variables, then print both values on separate lines.",
                "Buat variabel agent dan clearance, lalu cetak keduanya pada baris terpisah.",
                "agent = \"Null-Agent\"\nclearance = 1\n# print both values",
                "Both variables exist with the requested values and both are printed.",
                "Kedua variabel memiliki nilai yang diminta dan keduanya dicetak."),
            challenge = step(LessonPhase.CHALLENGE, "Signal formatting", "Format sinyal",
                "Build a status string from a sector variable and an integer channel without hard-coding the final sentence.",
                "Buat string status dari variabel sector dan angka channel tanpa menulis kalimat akhir secara langsung.",
                "sector = \"Sector-09\"\nchannel = 3\n# build and print the status",
                "The printed status uses the current variable values.",
                "Status yang dicetak menggunakan nilai variabel saat ini."),
            operation = step(LessonPhase.OPERATION, "Restore the access beacon", "Pulihkan suar akses",
                "GLITCH needs a two-line beacon report. Store the agent handle and signal state, then print a readable report.",
                "GLITCH membutuhkan laporan suar dua baris. Simpan identitas agen dan status sinyal, lalu cetak laporan yang mudah dibaca.",
                "agent = \"\"\nsignal = \"\"\n# complete the beacon report",
                "The report is built from variables and contains both identity and signal state.",
                "Laporan dibuat dari variabel dan memuat identitas serta status sinyal."),
            mastery = step(LessonPhase.MASTERY, "Identity checksum", "Checksum identitas",
                "From a blank script, define the requested agent identity and clearance, then print a single exact confirmation line.",
                "Dari skrip kosong, definisikan identitas agen dan clearance yang diminta, lalu cetak satu baris konfirmasi.",
                "# define agent and clearance\n# print the confirmation",
                "Correct assignment, string/integer values, and exact confirmation output.",
                "Penugasan, nilai string/integer, dan keluaran konfirmasi harus tepat.")
        ),
        2 to ExerciseSpec(
            practice = step(LessonPhase.PRACTICE, "Threat gate", "Gerbang ancaman",
                "Use if/else to print DENIED for threat values of 50 or more and ALLOW otherwise.",
                "Gunakan if/else untuk mencetak DENIED jika threat minimal 50 dan ALLOW jika kurang.",
                "threat = 25\nif threat >= 50:\n    print(\"DENIED\")\nelse:\n    print(\"ALLOW\")",
                "Both branches are present, correctly indented, and use the threshold comparison.",
                "Kedua cabang tersedia, indentasinya benar, dan memakai perbandingan ambang."),
            challenge = step(LessonPhase.CHALLENGE, "Three-level routing", "Routing tiga tingkat",
                "Classify a packet as SAFE below 30, REVIEW from 30 through 69, and BLOCK at 70 or above.",
                "Klasifikasikan paket sebagai SAFE di bawah 30, REVIEW dari 30 sampai 69, dan BLOCK mulai 70.",
                "risk = 42\n# write an if / elif / else chain",
                "All three ranges are mutually exclusive and cover every integer risk value.",
                "Ketiga rentang tidak tumpang tindih dan mencakup semua nilai risiko integer."),
            operation = step(LessonPhase.OPERATION, "Bypass the checkpoint", "Lewati pos pemeriksaan",
                "The checkpoint receives a clearance level. Allow levels 3 and above; reject all lower levels.",
                "Pos pemeriksaan menerima tingkat clearance. Izinkan level 3 ke atas dan tolak sisanya.",
                "clearance = 2\n# decide whether the checkpoint opens",
                "The decision is made from the variable and handles the boundary value 3 correctly.",
                "Keputusan berasal dari variabel dan menangani batas nilai 3 dengan benar."),
            mastery = step(LessonPhase.MASTERY, "Policy engine", "Mesin kebijakan",
                "Write a complete decision chain for a numeric threat score: LOW below 20, MEDIUM below 60, otherwise HIGH.",
                "Tulis rantai keputusan lengkap untuk skor ancaman: LOW di bawah 20, MEDIUM di bawah 60, selain itu HIGH.",
                "score = 0\n# implement the three policy outcomes",
                "The conditions are ordered correctly and every score receives exactly one label.",
                "Kondisi berurutan benar dan setiap skor mendapat tepat satu label.")
        ),
        3 to ExerciseSpec(
            practice = step(LessonPhase.PRACTICE, "Collect the shards", "Kumpulkan shard",
                "Create a list of three shard IDs, append a fourth, and print the list length.",
                "Buat list berisi tiga ID shard, tambahkan yang keempat, lalu cetak panjang list.",
                "shards = [\"A1\", \"B2\", \"C3\"]\n# append D4 and print the length",
                "The list contains four items after append and len() reports four.",
                "List berisi empat item setelah append dan len() menghasilkan empat."),
            challenge = step(LessonPhase.CHALLENGE, "Index the vault", "Indeks brankas",
                "Create a dictionary mapping sector to 4 and key to X99. Read and print the key by name.",
                "Buat dictionary yang memetakan sector ke 4 dan key ke X99. Ambil dan cetak key berdasarkan namanya.",
                "vault = {}\n# add both entries and print the key",
                "The dictionary stores both mappings and retrieves X99 using its key.",
                "Dictionary menyimpan kedua pasangan dan mengambil X99 menggunakan key-nya."),
            operation = step(LessonPhase.OPERATION, "Deduplicate access tokens", "Hilangkan token duplikat",
                "Turn the supplied repeated token IDs into a set and report how many unique IDs remain.",
                "Ubah ID token berulang berikut menjadi set dan laporkan jumlah ID unik yang tersisa.",
                "tokens = [\"K1\", \"K2\", \"K1\", \"K3\", \"K2\"]\n# create a set and print its size",
                "Duplicates are removed by the set and the unique count is correct.",
                "Duplikat dihapus oleh set dan jumlah uniknya benar."),
            mastery = step(LessonPhase.MASTERY, "Rebuild the data vault", "Bangun ulang brankas data",
                "Create a list, tuple, set, and dictionary for one small vault inventory. Print a value retrieved from each structure.",
                "Buat list, tuple, set, dan dictionary untuk inventaris brankas kecil. Cetak satu nilai yang diambil dari tiap struktur.",
                "# define four different collection types\n# retrieve and print one value from each",
                "All four collection types are used correctly and values are accessed using their proper operations.",
                "Keempat tipe koleksi digunakan benar dan nilainya diakses dengan operasi yang sesuai.")
        ),
        4 to ExerciseSpec(
            practice = step(LessonPhase.PRACTICE, "Count the ports", "Hitung port",
                "Use for and range() to print port numbers 1 through 4.",
                "Gunakan for dan range() untuk mencetak nomor port 1 sampai 4.",
                "for port in range(1, 5):\n    print(port)",
                "The loop prints four numbers in ascending order, including 4.",
                "Loop mencetak empat angka berurutan naik, termasuk 4."),
            challenge = step(LessonPhase.CHALLENGE, "Sum the packet weights", "Jumlahkan bobot paket",
                "Use a loop to calculate the sum of all integers from 1 through 10.",
                "Gunakan loop untuk menghitung jumlah semua integer dari 1 sampai 10.",
                "total = 0\n# loop through the range and update total\nprint(total)",
                "The accumulator is updated inside the loop and the final total is 55.",
                "Accumulator diperbarui di dalam loop dan total akhirnya 55."),
            operation = step(LessonPhase.OPERATION, "Scan the subnet", "Pindai subnet",
                "Scan host IDs 1 through 5. Print only IDs that are even.",
                "Pindai ID host 1 sampai 5. Cetak hanya ID yang genap.",
                "for host in range(1, 6):\n    # check whether host is even",
                "The loop visits the full range and prints only even host IDs.",
                "Loop mengunjungi seluruh rentang dan hanya mencetak ID host genap."),
            mastery = step(LessonPhase.MASTERY, "Automate the sweep", "Otomatiskan penyisiran",
                "Use a loop and an accumulator to count how many values in the supplied list exceed 10.",
                "Gunakan loop dan accumulator untuk menghitung berapa nilai pada list berikut yang lebih besar dari 10.",
                "latencies = [4, 12, 18, 7, 21, 9]\ncount = 0\n# count values above 10",
                "The result is computed by iteration rather than a hard-coded answer.",
                "Hasil dihitung melalui iterasi, bukan angka jawaban yang ditulis langsung.")
        ),
        5 to ExerciseSpec(
            practice = step(LessonPhase.PRACTICE, "Forge a function", "Tempa fungsi",
                "Define double(value) so it returns twice the input, then print the result for 8.",
                "Definisikan double(value) agar mengembalikan dua kali input, lalu cetak hasil untuk 8.",
                "def double(value):\n    # return the doubled value\n\nprint(double(8))",
                "The function accepts a parameter, returns a value, and produces 16 for input 8.",
                "Fungsi menerima parameter, mengembalikan nilai, dan menghasilkan 16 untuk input 8."),
            challenge = step(LessonPhase.CHALLENGE, "Separate local state", "Pisahkan state lokal",
                "Write a function that creates a local variable and returns it without relying on a global variable.",
                "Tulis fungsi yang membuat variabel lokal dan mengembalikannya tanpa bergantung pada variabel global.",
                "def build_key(seed):\n    # create and return a local result\n    pass",
                "The returned result is computed from the parameter and remains usable by the caller.",
                "Hasil yang dikembalikan dihitung dari parameter dan dapat digunakan pemanggil."),
            operation = step(LessonPhase.OPERATION, "Deploy the patch function", "Terapkan fungsi patch",
                "Create deploy(target) that returns PATCHED followed by the target name. Call it for Sector-09.",
                "Buat deploy(target) yang mengembalikan PATCHED diikuti nama target. Panggil untuk Sector-09.",
                "def deploy(target):\n    # return the patch status\n    pass\n\nprint(deploy(\"Sector-09\"))",
                "The function is reusable and its return value is printed by the caller.",
                "Fungsi dapat digunakan ulang dan nilai return dicetak oleh pemanggil."),
            mastery = step(LessonPhase.MASTERY, "Build a reusable checksum", "Buat checksum yang dapat digunakan ulang",
                "Write a function that accepts a list of numbers and returns their sum. Do not print inside the function.",
                "Tulis fungsi yang menerima list angka dan mengembalikan jumlahnya. Jangan mencetak dari dalam fungsi.",
                "def checksum(values):\n    # calculate and return the sum\n    pass",
                "The function returns the correct sum for different lists and keeps output decisions outside the function.",
                "Fungsi mengembalikan jumlah yang benar untuk list berbeda dan keputusan output berada di luar fungsi.")
        ),
        6 to ExerciseSpec(
            practice = step(LessonPhase.PRACTICE, "Catch a missing file", "Tangani file yang hilang",
                "Attempt to open a missing file and catch FileNotFoundError to print a fallback message.",
                "Coba buka file yang tidak ada dan tangkap FileNotFoundError untuk mencetak pesan cadangan.",
                "try:\n    with open(\"missing.log\", \"r\") as file:\n        data = file.read()\nexcept FileNotFoundError:\n    print(\"FALLBACK\")",
                "The specific exception is handled and the program reaches the fallback path.",
                "Exception spesifik ditangani dan program menjalankan jalur cadangan."),
            challenge = step(LessonPhase.CHALLENGE, "Validate parsed data", "Validasi data hasil parsing",
                "Convert a text value to an integer and handle ValueError without crashing.",
                "Ubah teks menjadi integer dan tangani ValueError tanpa membuat program crash.",
                "raw_value = \"not-a-number\"\n# convert inside try and handle ValueError",
                "Valid numeric text converts successfully; invalid text follows the exception handler.",
                "Teks angka valid berhasil dikonversi; teks tidak valid masuk ke exception handler."),
            operation = step(LessonPhase.OPERATION, "Recover the damaged log", "Pulihkan log rusak",
                "Read a log file safely. If it is missing, provide a fallback report. Keep file access inside a with block.",
                "Baca file log dengan aman. Jika tidak ada, berikan laporan cadangan. Letakkan akses file di dalam blok with.",
                "log_name = \"mission.log\"\n# use try / with open / except",
                "The file is closed by the context manager and missing-file recovery is explicit.",
                "File ditutup oleh context manager dan pemulihan file hilang ditangani secara eksplisit."),
            mastery = step(LessonPhase.MASTERY, "Resilient parser", "Parser tangguh",
                "Build a small parser that converts a supplied value, handles invalid input, and reports success or failure.",
                "Bangun parser kecil yang mengonversi nilai, menangani input tidak valid, dan melaporkan berhasil atau gagal.",
                "raw = \"42\"\n# parse safely and report the outcome",
                "Both valid and invalid input paths are handled without an uncaught exception.",
                "Jalur input valid dan tidak valid ditangani tanpa exception yang tidak tertangkap.")
        ),
        7 to ExerciseSpec(
            practice = step(LessonPhase.PRACTICE, "Instantiate a patch", "Buat objek patch",
                "Create a Patch class that stores a target in __init__, then create one instance.",
                "Buat class Patch yang menyimpan target di __init__, lalu buat satu instance.",
                "class Patch:\n    def __init__(self, target):\n        self.target = target\n\npatch = Patch(\"Sector-09\")",
                "The instance stores its own target through self.target.",
                "Instance menyimpan target miliknya melalui self.target."),
            challenge = step(LessonPhase.CHALLENGE, "Add a method", "Tambahkan method",
                "Add a deploy() method that returns a status string containing the object's target.",
                "Tambahkan method deploy() yang mengembalikan status berisi target milik objek.",
                "class Patch:\n    def __init__(self, target):\n        self.target = target\n    # add deploy() here",
                "The method reads instance state through self and returns a value.",
                "Method membaca state instance melalui self dan mengembalikan nilai."),
            operation = step(LessonPhase.OPERATION, "Deploy three patches", "Terapkan tiga patch",
                "Create three Patch objects for three sectors and call deploy() on each object.",
                "Buat tiga objek Patch untuk tiga sektor dan panggil deploy() pada setiap objek.",
                "# define Patch with __init__ and deploy\n# instantiate three targets",
                "Each object keeps its own target and produces its own deployment result.",
                "Setiap objek menyimpan target sendiri dan menghasilkan hasil deployment masing-masing."),
            mastery = step(LessonPhase.MASTERY, "Design a secure node", "Rancang node aman",
                "Design a Node class with a name and clearance. Add a method that returns whether a requested level is permitted.",
                "Rancang class Node dengan name dan clearance. Tambahkan method yang mengembalikan apakah level yang diminta diizinkan.",
                "class Node:\n    def __init__(self, name, clearance):\n        # store instance state\n        pass\n\n    def permits(self, level):\n        # return a boolean decision\n        pass",
                "State is initialized per instance and permits() returns a boolean based on the instance clearance.",
                "State diinisialisasi per instance dan permits() mengembalikan boolean berdasarkan clearance instance.")
        )
    )

    fun stepsFor(module: ProtocolModule): List<LessonStep> {
        val exercise = exercises[module.id] ?: return emptyList()
        return listOf(
            LessonStep(
                phase = LessonPhase.LEARN,
                title = text("Learn the concept", "Pelajari konsep"),
                instruction = module.pythonConceptExplanation,
                acceptanceCriteria = text("Explain the concept in your own words.", "Jelaskan konsep dengan kata-katamu sendiri.")
            ),
            LessonStep(
                phase = LessonPhase.OBSERVE,
                title = text("Observe the code", "Amati kode"),
                instruction = text("Read the example and predict its output before running it.", "Baca contoh dan prediksi output sebelum menjalankannya."),
                code = module.codeExample,
                acceptanceCriteria = text("Your prediction matches the actual Python output.", "Prediksimu sesuai dengan output Python sebenarnya.")
            ),
            exercise.practice,
            exercise.challenge,
            exercise.operation,
            exercise.mastery
        )
    }

    fun allSteps(modules: List<ProtocolModule> = CurriculumRepository.modules): Map<Int, List<LessonStep>> =
        modules.associate { it.id to stepsFor(it) }
}
