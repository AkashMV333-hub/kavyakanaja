package com.example.kavyakanaja.ui.screens

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.kavyakanaja.data.Repository
import com.example.kavyakanaja.data.Poem
import com.example.kavyakanaja.media.TtsHelper
import kotlin.math.absoluteValue

@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    val repo = remember { Repository(context) }
    val poems = remember { mutableStateOf<List<Poem>>(emptyList()) }
    val selectedWord = remember { mutableStateOf<Pair<String, String?>>("" to null) }
    val showDialog = remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        poems.value = repo.loadPoems()
    }

    val tts = remember { TtsHelper(context) }
    DisposableEffect(Unit) {
        onDispose { tts.shutdown() }
    }

    Surface(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(modifier = Modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState())) {
            Text(text = "Poem of the Day", style = MaterialTheme.typography.titleLarge)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { navController.navigate("poets") }) { Text("Poets") }
                TextButton(onClick = { navController.navigate("favorites") }) { Text("Favorites") }
            }

            if (poems.value.isNotEmpty()) {
                val poem = poemOfTheDay(poems.value)
                Card(modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .clickable { navController.navigate("poem/${Uri.encode(poem.id)}") }, elevation = CardDefaults.cardElevation(4.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = poem.title, style = MaterialTheme.typography.titleMedium)
                        Text(text = "by ${poem.poet}", style = MaterialTheme.typography.bodySmall)

                        val annotated = buildAnnotatedString {
                            val words = poem.text.split(Regex("\\s+"))
                            words.forEachIndexed { idx, token ->
                                val clean = token.trim().trim(',', '.', '!', '?', '"', '“', '”')
                                val start = length
                                append(token)
                                val end = length
                                if (poem.meanings.containsKey(clean)) {
                                    addStringAnnotation(tag = "MEANING", annotation = clean, start = start, end = end)
                                }
                                if (idx != words.lastIndex) append(" ")
                            }
                        }

                        ClickableText(text = annotated, onClick = { offset ->
                            val list = annotated.getStringAnnotations(tag = "MEANING", start = offset, end = offset)
                            if (list.isNotEmpty()) {
                                val word = list.first().item
                                selectedWord.value = word to poem.meanings[word]
                                showDialog.value = true
                            }
                        }, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 8.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Button(onClick = { tts.speak(poem.text) }, modifier = Modifier.padding(top = 12.dp)) {
                                Text("Play (TTS)")
                            }
                            Button(onClick = { navController.navigate("poem/${Uri.encode(poem.id)}") }, modifier = Modifier.padding(top = 12.dp)) {
                                Text("Read")
                            }
                        }
                    }
                }
            } else {
                Text("Loading poems…", modifier = Modifier.padding(top = 12.dp))
            }
        }
    }

    if (showDialog.value) {
        MeaningDialog(word = selectedWord.value.first, meaning = selectedWord.value.second, onDismiss = { showDialog.value = false })
    }
}

// small helper; simple deterministic selection based on epoch day
fun poemOfTheDay(poems: List<Poem>, nowMillis: Long = System.currentTimeMillis()): Poem {
    if (poems.isEmpty()) throw IllegalArgumentException("No poems available")
    val days = nowMillis / 86_400_000L
    val dayIndex = (days % poems.size).toInt().absoluteValue
    return poems[dayIndex]
}

@Composable
fun MeaningDialog(word: String, meaning: String?, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = word) },
        text = { Text(text = meaning ?: "Meaning not available") },
        confirmButton = {
            Button(onClick = onDismiss) { Text("Close") }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun HomePreview() {
    // preview cannot load assets; show placeholder
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Poem of the Day", style = MaterialTheme.typography.titleLarge)
    }
}

