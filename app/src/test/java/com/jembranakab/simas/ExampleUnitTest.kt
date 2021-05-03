package com.jembranakab.simas

import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        val asalDraft = "1111".toCharArray()
        var bawahan = ""
        for (i in 0 until asalDraft.size-2) {
            for (j in 0 until asalDraft.size - i) {
                bawahan += asalDraft[j].toString()
            }
            bawahan += " "
        }
        assertEquals(bawahan, "1111 111 ")
    }

    @Test
    fun addition_isCorrect1() {
        val asalDraft = "11111".toCharArray()
        var bawahan = ""
        for (i in 0 until asalDraft.size-2) {
            for (j in 0 until asalDraft.size - i) {
                bawahan += asalDraft[j].toString()
            }
            bawahan += " "
        }
        assertEquals(bawahan, "11111 1111 111 ")
    }

    @Test
    fun addition_isCorrect2() {
        val asalDraft = "11112".toCharArray()
        var bawahan = ""
        for (i in 0 until asalDraft.size-2) {
            for (j in 0 until asalDraft.size - i) {
                bawahan += asalDraft[j].toString()
            }
            bawahan += " "
        }
        assertEquals(bawahan, "11112 1111 111 ")
    }

    @Test
    fun addition_isCorrect3() {
        val asalDraft = "111".toCharArray()
        var bawahan = ""
        for (i in 0 until asalDraft.size-2) {
            for (j in 0 until asalDraft.size - i) {
                bawahan += asalDraft[j].toString()
            }
            bawahan += " "
        }
        assertEquals(bawahan, "111 ")
    }

    @Test
    fun addition_isCorrect4() {
        val asalDraft = "1113".toCharArray()
        var bawahan = ""
        for (i in 0 until asalDraft.size-2) {
            for (j in 0 until asalDraft.size - i) {
                bawahan += asalDraft[j].toString()
            }
            bawahan += " "
        }
        assertEquals(bawahan, "1113 111 ")
    }

    @Test
    fun addition_isCorrect5() {
        val asalDraft = "11131".toCharArray()
        var bawahan = ""
        for (i in 0 until asalDraft.size-2) {
            for (j in 0 until asalDraft.size - i) {
                bawahan += asalDraft[j].toString()
            }
            bawahan += " "
        }
        assertEquals(bawahan, "11131 1113 111 ")
    }

    @Test
    fun addition_isCorrect6() {
        val asalDraft = "1121".toCharArray()
        var bawahan = ""
        for (i in 0 until asalDraft.size-2) {
            for (j in 0 until asalDraft.size - i) {
                bawahan += asalDraft[j].toString()
            }
            bawahan += " "
        }
        assertEquals(bawahan, "1121 112 ")
    }

    @Test
    fun addition_isCorrect7() {
        var atasan = ""
        val thisUnitCharArray = "111".toCharArray()
        for (i in 0 until thisUnitCharArray.size - 1) {
            atasan += thisUnitCharArray[i]
        }
        assertEquals(atasan, "11")
    }

    @Test
    fun addition_isCorrect8() {
        var atasan = ""
        val thisUnitCharArray = "1111".toCharArray()
        for (i in 0 until thisUnitCharArray.size - 1) {
            atasan += thisUnitCharArray[i]
        }
        assertEquals(atasan, "111")
    }

    @Test
    fun addition_isCorrect9() {
        var atasan = ""
        val thisUnitCharArray = "11111".toCharArray()
        for (i in 0 until thisUnitCharArray.size - 1) {
            atasan += thisUnitCharArray[i]
        }
        assertEquals(atasan, "1111")
    }

    @Test
    fun test01() {
        var result = ""
        var asalDraftCharArray = "11111".toCharArray()
        var penerimaCharArray = "11".toCharArray()

        for (i in 0 until asalDraftCharArray.size - penerimaCharArray.size) {
            var atasan = ""
            for (j in 0 until asalDraftCharArray.size - i) {
                atasan += asalDraftCharArray[j]
            }
            result += " $atasan"
//            asalDraftCharArray = atasan.toCharArray()
        }

        assertEquals(result, " 11111 1111 111")
    }

}