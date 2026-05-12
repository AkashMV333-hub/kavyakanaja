package com.example.kavyakanaja.ui.screens

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.kavyakanaja.data.Repository
import androidx.compose.ui.platform.LocalContext

@Composable
fun PoetsScreen(navController: NavController) {
    val context = LocalContext.current
    val repo = remember { Repository(context) }
    val poems = remember { repo.loadPoems() }

    // group by poet
    val poets = poems.groupBy { it.poet }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        items(poets.entries.toList()) { entry ->
            val poetName = entry.key
            Card(modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
                .clickable {
                    val encoded = Uri.encode(poetName)
                    navController.navigate("poet/$encoded")
                }) {
                Text(text = poetName, modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
fun PoetDetailScreen(navController: NavController, poetName: String?) {
    val context = LocalContext.current
    val repo = remember { Repository(context) }
    val poems = remember { repo.loadPoems().filter { it.poet == poetName } }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = poetName ?: "Poet", style = MaterialTheme.typography.titleLarge)
        poems.forEach { poem ->
            Card(modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .clickable { navController.navigate("poem/${Uri.encode(poem.id)}") }) {
                Text(text = poem.title, modifier = Modifier.padding(12.dp))
            }
        }
    }
}

