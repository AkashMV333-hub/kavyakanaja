package com.example.kavyakanaja.ui.screens

import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.kavyakanaja.data.Repository
import com.example.kavyakanaja.data.FavoritesManager
import com.example.kavyakanaja.media.TtsHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.kavyakanaja.ui.screens.MeaningDialog

@Composable
fun PoemDetailScreen(navController: NavController, poemId: String?) {
    val context = LocalContext.current
    val repo = remember { Repository(context) }
    val poems = remember { mutableStateOf(repo.loadPoems()) }
    val poem = poems.value.firstOrNull { it.id == poemId }
    val tts = remember { TtsHelper(context) }

    DisposableEffect(Unit) { onDispose { tts.shutdown() } }

    val selectedWord = remember { mutableStateOf<Pair<String, String?>>("" to null) }
    val showDialog = remember { mutableStateOf(false) }

    if (poem == null) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Poem not found", style = MaterialTheme.typography.titleLarge)
        }
        return
    }

    val isFav by FavoritesManager.isFavoriteFlow(context, poem.id).collectAsState(initial = false)

    Column(modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(16.dp)) {
        Text(text = poem.title, style = MaterialTheme.typography.titleLarge)
        Text(text = "by ${poem.poet}", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(bottom = 8.dp))

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
        }, modifier = Modifier.padding(vertical = 8.dp))

        Row(modifier = Modifier.fillMaxWidth(),) {
            Button(onClick = { tts.speak(poem.text) }) { Text("Play (TTS)") }
            Button(onClick = {
                // toggle favorite
                CoroutineScope(Dispatchers.IO).launch {
                    FavoritesManager.toggleFavorite(context, poem.id)
                }
            }, modifier = Modifier.padding(start = 8.dp)) {
                Text(if (isFav) "Unfavorite" else "Favorite")
            }
        }

        poem.explanation?.let {
            Card(modifier = Modifier.padding(top = 16.dp)) {
                Text(text = it, modifier = Modifier.padding(12.dp))
            }
        }
    }
    if (showDialog.value) {
        MeaningDialog(word = selectedWord.value.first, meaning = selectedWord.value.second, onDismiss = { showDialog.value = false })
    }
}

