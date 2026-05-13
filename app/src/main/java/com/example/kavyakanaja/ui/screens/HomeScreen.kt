package com.example.kavyakanaja.ui.screens

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.graphics.Color as UiColor
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.kavyakanaja.data.Repository
import com.example.kavyakanaja.data.Poem
import com.example.kavyakanaja.media.TtsHelper
import kotlin.math.absoluteValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {

    val context = LocalContext.current
    val repo = remember { Repository(context) }

    val poems = remember { mutableStateOf<List<Poem>>(emptyList()) }
    val selectedWord = remember {
        mutableStateOf<Pair<String, String?>>("" to null)
    }
    val showDialog = remember { mutableStateOf(false) }

    // Initialize selected date to today at midnight
    val todayMillis = getTodayMillis()
    val selectedDateMillis = remember { mutableStateOf(todayMillis) }
    val showDatePicker = remember { mutableStateOf(false) }

    // Calculate year range: from 1900 to current year (no future dates allowed)
    val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
    val yearRange = 1900..currentYear

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = todayMillis,
        yearRange = yearRange
    )

    LaunchedEffect(Unit) {
        poems.value = repo.loadPoems()
    }

    val tts = remember { TtsHelper(context) }

    DisposableEffect(Unit) {
        onDispose {
            tts.shutdown()
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {

        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
        ) {

            CenterAlignedTopAppBar(
                title = { Text("Kavya-Kanaja") },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                actions = {
                    TextButton(
                        onClick = { navController.navigate("poets") }
                    ) {
                        Text("Poets")
                    }

                    TextButton(
                        onClick = { navController.navigate("favorites") }
                    ) {
                        Text("Favorites")
                    }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            Column(modifier = Modifier.padding(16.dp)) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Poem for ${formatDateForDisplay(selectedDateMillis.value)}",
                        style = MaterialTheme.typography.titleLarge
                    )

                    Button(
                        onClick = { showDatePicker.value = true },
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text("📅 Pick Date")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (poems.value.isNotEmpty()) {

                    val poem = poemOfTheDay(poems.value, selectedDateMillis.value)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                            .clickable {
                                navController.navigate(
                                    "poem/${Uri.encode(poem.id)}"
                                )
                            },
                        elevation = CardDefaults.cardElevation(6.dp),
                        shape = MaterialTheme.shapes.medium
                    ) {

                        Column(modifier = Modifier.padding(20.dp)) {

                            Text(
                                text = poem.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Text(
                                text = "by ${poem.poet}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            val annotated = buildAnnotatedString {

                                val words = poem.text.split(Regex("\\s+"))

                                words.forEachIndexed { idx, token ->

                                    val clean = token.trim()
                                        .trim(',', '.', '!', '?', '"', '“', '”')

                                    val start = length

                                    append(token)

                                    val end = length

                                    if (poem.meanings.containsKey(clean)) {

                                        addStyle(
                                            style = SpanStyle(
                                                color = UiColor(0xFF2E7D32)
                                            ),
                                            start = start,
                                            end = end
                                        )

                                        addStringAnnotation(
                                            tag = "MEANING",
                                            annotation = clean,
                                            start = start,
                                            end = end
                                        )
                                    }

                                    if (idx != words.lastIndex) {
                                        append(" ")
                                    }
                                }
                            }

                            ClickableText(
                                text = annotated,
                                onClick = { offset ->

                                    val list = annotated.getStringAnnotations(
                                        tag = "MEANING",
                                        start = offset,
                                        end = offset
                                    )

                                    if (list.isNotEmpty()) {

                                        val ann = list.first()

                                        val wordStr = ann.item.toString()

                                        val meaning = poem.meanings[wordStr]

                                        selectedWord.value =
                                            wordStr to meaning?.toString()

                                        showDialog.value = true
                                    }
                                },
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(top = 12.dp)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {

                                Button(
                                    onClick = {
                                        tts.speak(poem.text)
                                    },
                                    modifier = Modifier.padding(top = 12.dp)
                                ) {
                                    Text("Play")
                                }

                                Button(
                                    onClick = {
                                        navController.navigate(
                                            "poem/${Uri.encode(poem.id)}"
                                        )
                                    },
                                    modifier = Modifier.padding(top = 12.dp)
                                ) {
                                    Text("Read")
                                }
                            }
                        }
                    }

                } else {

                    Text(
                        "Loading poems…",
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }
            }
        }
    }

    if (showDialog.value) {
        MeaningDialog(
            word = selectedWord.value.first,
            meaning = selectedWord.value.second,
            onDismiss = {
                showDialog.value = false
            }
        )
    }

    if (showDatePicker.value) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker.value = false },
            confirmButton = {
                Button(
                    onClick = {
                        val selectedMillis = datePickerState.selectedDateMillis ?: todayMillis

                        // Ensure selected date is not in the future (cap to today if it is)
                        val cappedMillis = if (selectedMillis > todayMillis) todayMillis else selectedMillis

                        selectedDateMillis.value = cappedMillis
                        showDatePicker.value = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDatePicker.value = false }
                ) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomePreview() {
    // preview cannot load assets; show placeholder
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Poem of the Day", style = MaterialTheme.typography.titleLarge)
    }
}

// Helper: Get today's date at midnight (UTC) in milliseconds
fun getTodayMillis(): Long {
    val calendar = java.util.Calendar.getInstance()
    calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
    calendar.set(java.util.Calendar.MINUTE, 0)
    calendar.set(java.util.Calendar.SECOND, 0)
    calendar.set(java.util.Calendar.MILLISECOND, 0)
    return calendar.timeInMillis
}

// Helper: Format milliseconds to a readable date string (e.g., "May 13, 2026")
fun formatDateForDisplay(millis: Long): String {
    val calendar = java.util.Calendar.getInstance()
    calendar.timeInMillis = millis
    val format = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
    return format.format(calendar.time)
}
