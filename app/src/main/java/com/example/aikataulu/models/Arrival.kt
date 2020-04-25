package com.example.aikataulu.models

class Arrival {
    var bus: Bus? = null
    private var timeH: Int? = null
    private var timeM: Int? = null

    fun setTime(h: Int, m: Int): Arrival {
        timeH = h
        timeM = m
        return this
    }

    override fun toString(): String {
        val hh: String = if (timeH!! >= 10) timeH.toString() else "0${timeH}"
        val mm: String = if (timeM!! >= 10) timeM.toString() else "0${timeM}"
        return "${bus?.name}\t${bus?.destination}\t${hh}:${mm}"
    }
}