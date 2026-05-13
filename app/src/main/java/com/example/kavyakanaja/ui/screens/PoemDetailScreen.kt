package com.example.kavyakanaja.ui.screens

import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.material3.TabRow
import androidx.compose.material3.Tab
import androidx.navigation.NavController
import com.example.kavyakanaja.data.Repository
import com.example.kavyakanaja.data.FavoritesManager
import com.example.kavyakanaja.media.TtsHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.kavyakanaja.ui.screens.MeaningDialog
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.ui.graphics.Color as UiColor

@OptIn(ExperimentalMaterial3Api::class)
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
    // Tabs: 0 = Translation, 1 = Explanation
    var selectedTab by remember { mutableStateOf(0) }

    if (poem == null) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Poem not found", style = MaterialTheme.typography.titleLarge)
        }
        return
    }

    val isFav by FavoritesManager.isFavoriteFlow(context, poem.id).collectAsState(initial = false)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        CenterAlignedTopAppBar(
            title = { Text(text = poem.title) },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            navigationIcon = {
                TextButton(onClick = { navController.navigateUp() }) { Text("Back") }
            })
        Spacer(modifier = Modifier.height(8.dp))
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = poem.title, style = MaterialTheme.typography.titleLarge)
            Text(
                text = "by ${poem.poet}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            val annotated = buildAnnotatedString {
                val words = poem.text.split(Regex("\\s+"))
                words.forEachIndexed { idx, token ->
                    val clean = token.trim().trim(',', '.', '!', '?', '"', '“', '”')
                    val start = length
                    append(token)
                    val end = length
                    if (poem.meanings.containsKey(clean)) {
                        addStringAnnotation(
                            tag = "MEANING",
                            annotation = clean,
                            start = start,
                            end = end
                        )
                    }
                    if (idx != words.lastIndex) append(" ")
                }
            }

            ClickableText(text = annotated, onClick = { offset ->
                val list =
                    annotated.getStringAnnotations(tag = "MEANING", start = offset, end = offset)
                if (list.isNotEmpty()) {
                    val ann = list.first()
                    // ensure the annotation item is treated as String
                    val wordStr = ann.item.toString()
                    val meaning = poem.meanings[wordStr]
                    selectedWord.value = wordStr to meaning?.toString()
                    showDialog.value = true
                }
            }, modifier = Modifier.padding(vertical = 8.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
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

            // Tab row for Translation / Explanation
            val tabs = listOf("Translation", "Explanation")
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(selected = selectedTab == index, onClick = { selectedTab = index }) {
                        Text(title, modifier = Modifier.padding(12.dp))
                    }
                }
            }

            when (selectedTab) {
                0 -> {
                    // Translation tab
                    Text(
                        text = poem.translation ?: "English translation not available.",
                        modifier = Modifier.padding(top = 12.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                1 -> {
                    // Explanation tab (English preferred)
                    Text(
                        text = poem.explanationEn ?: poem.explanation ?: "Explanation not available.",
                        modifier = Modifier.padding(top = 12.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
        if (showDialog.value) {
            MeaningDialog(
                word = selectedWord.value.first,
                meaning = selectedWord.value.second,
                onDismiss = { showDialog.value = false })
        }
    }
}

