package com.jembranakab.simas.utilities

class App {
    companion object {
        const val TAG = "SimasLogger"
        const val SURAT_DB_NAME = "surat_db"

        const val OPERATOR = 0
        const val ORG_TOP_LAYER = 1
        const val ORG_MID_LAYER = 2
        const val ORG_BOTTOM_LAYER = 3

        const val LOGOUT = 11
        const val EXIT = 12
        const val NAVIGATE_UP = 13

        const val KIRIM_ARSIP_KELUAR = 21
        const val DISPOSISI = 22
        const val JAWABAN = 23
        const val PENYELESAIAN = 24
        const val SURAT_MASUK = 25

        object DraftSurat {
            const val BELUM_PROSES = 0
            const val DIAJUKAN = 1
            const val DISETUJUI_DINAS = 2
            const val DIKOREKSI = 3
            const val DIAJUKAN_KEMBALI = 4
            const val KOREKSI = 5
            const val DISETUJUI_BIDANG = 6
            const val DILANJUTKAN = 7
            const val SETUJU = 8
            const val TELAH_DIKOREKSI = 9
        }

    }
}