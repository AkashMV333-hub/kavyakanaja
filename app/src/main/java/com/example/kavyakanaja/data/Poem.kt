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
    // Optional full English translation of the poem
    val translation: String? = null,
    // Kannada explanation (original)
    val explanation: String? = null,
    // Optional English explanation to help learners understand the verse
    val explanationEn: String? = null,
    val audio: String? = null,
    val poetBio: String? = null
)

