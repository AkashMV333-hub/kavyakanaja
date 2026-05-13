package com.example.kavyakanaja.ui.screens

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

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

