package com.example.kavyakanaja.ui.screens

import com.example.kavyakanaja.data.Poem
import kotlin.math.absoluteValue

// small helper; simple deterministic selection based on epoch day
fun poemOfTheDay(poems: List<Poem>, nowMillis: Long = System.currentTimeMillis()): Poem {
    if (poems.isEmpty()) throw IllegalArgumentException("No poems available")
    val days = nowMillis / 86_400_000L
    val dayIndex = (days % poems.size).toInt().absoluteValue
    return poems[dayIndex]
}

