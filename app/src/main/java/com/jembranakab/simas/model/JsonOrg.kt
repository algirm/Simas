package com.jembranakab.simas.model

import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import com.jembranakab.simas.model.entities.Organisasi

class JsonOrg {

    companion object {

        fun getBawahan(unit: Int): ArrayList<Organisasi> {
            return try {
                val bawahan = arrayListOf<Organisasi>()
                val batasAtas: Int
                val batasBawah: Int

                if (unit == 1) {
                    batasAtas = 2
                    batasBawah = 2
                } else {
                    batasAtas = unit * 10
                    batasBawah = batasAtas + 9
                }

                val jsonList = (parseMyJson() as JsonArray<*>)
                        .filterIsInstance<JsonObject>()
                        .filter {
                            it.int("unit")!! in batasAtas..batasBawah
                        }

                for (data in jsonList) {
                    bawahan.add(Organisasi(data.string("nama"), data.int("unit")))
                }

                bawahan
            } catch (e: Exception) {
                arrayListOf()
            }
        }

        fun getNamaOrg(unit: Int): String {
            return try {
                (parseMyJson() as JsonArray<*>)
                        .filterIsInstance<JsonObject>()
                        .filter { it.int("unit")!! == unit }[0]
                        .string("nama")?: ""
            } catch (e: Exception) {
                ""
            }
        }

        fun getAllTopLayer(): ArrayList<Organisasi> {
            return try {
                val jsonList = (parseMyJson() as JsonArray<*>)
                        .filterIsInstance<JsonObject>()
                        .filter { it.int("unit")!! in 1..98 }
                val result = arrayListOf<Organisasi>()
                for (data in jsonList) {
                    result.add(Organisasi(data.string("nama"), data.int("unit")))
                }
                result
            } catch (e: Exception) {
                arrayListOf()
            }
        }

        fun getAllTopExcept(unit: Int): ArrayList<Organisasi> {
            return try {
                val jsonList = (parseMyJson() as JsonArray<*>)
                        .filterIsInstance<JsonObject>()
                        .filter {
                            it.int("unit")!! in 1..98 && it.int("unit")!! != unit
                        }
                val result = arrayListOf<Organisasi>()
                for (data in jsonList) {
                    result.add(Organisasi(data.string("nama"), data.int("unit")))
                }
                result
            } catch (e: Exception) {
                arrayListOf()
            }
        }

        fun getAllTopExcepts(unit: Int, unit2: Int): ArrayList<Organisasi> {
            return try {
                val jsonList = (parseMyJson() as JsonArray<*>)
                        .filterIsInstance<JsonObject>()
                        .filter {
                            it.int("unit")!! in 1..98 &&
                                    it.int("unit")!! != unit &&
                                    it.int("unit")!! != unit2
                        }
                val result = arrayListOf<Organisasi>()
                for (data in jsonList) {
                    result.add(Organisasi(data.string("nama"), data.int("unit")))
                }
                result
            } catch (e: Exception) {
                arrayListOf()
            }
        }

        fun getAllOrg(): ArrayList<Organisasi> {
            return try {
                val jsonList = (parseMyJson() as JsonArray<*>)
                        .filterIsInstance<JsonObject>()
                val result = arrayListOf<Organisasi>()
                for (data in jsonList) {
                    result.add(Organisasi(data.string("nama"), data.int("unit")))
                }
                result
            } catch (e: Exception) {
                arrayListOf()
            }
        }

        fun getAllTopWithDinasLuar(): ArrayList<Organisasi> {
            return try {
                val jsonList = (parseMyJson() as JsonArray<*>)
                        .filterIsInstance<JsonObject>()
                        .filter {
                            it.int("unit")!! in 1..99
                        }
                val result = arrayListOf<Organisasi>()
                for (data in jsonList) {
                    result.add(Organisasi(data.string("nama"), data.int("unit")))
                }
                result
            } catch (e: Exception) {
                arrayListOf()
            }
        }

        fun getAllTopWithDinasLuarExcept(unit: Int): ArrayList<Organisasi> {
            return try {
                val jsonList = (parseMyJson() as JsonArray<*>)
                        .filterIsInstance<JsonObject>()
                        .filter {
                            it.int("unit")!! in 1..99 && it.int("unit")!! != unit
                        }
                val result = arrayListOf<Organisasi>()
                for (data in jsonList) {
                    result.add(Organisasi(data.string("nama"), data.int("unit")))
                }
                result
            } catch (e: Exception) {
                arrayListOf()
            }
        }

        private fun parseMyJson(): Any? {
            val cls = Parser::class.java
            return cls.getResourceAsStream("/assets/struktur-organisasi.json")?.let { inputStream ->
                return Parser.default().parse(inputStream)
            }
        }

    }

}