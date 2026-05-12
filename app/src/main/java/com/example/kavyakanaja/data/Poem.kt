package com.example.kavyakanaja.data

data class Poem(
    val id: String,
    val title: String,
    val poet: String,
    val poetId: String? = null,
    val date: String? = null,
    val text: String,
    val lines: List<String> = emptyList(),
    val meanings: Map<String, String> = emptyMap(),
    val explanation: String? = null,
    val audio: String? = null,
    val poetBio: String? = null
)

