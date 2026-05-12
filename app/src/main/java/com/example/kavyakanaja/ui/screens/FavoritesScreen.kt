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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.kavyakanaja.data.FavoritesManager
import com.example.kavyakanaja.data.Repository

@Composable
fun FavoritesScreen(navController: NavController) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val repo = remember { Repository(context) }
    val poems = remember { repo.loadPoems() }

    val favs by FavoritesManager.favoritesFlow(context).collectAsState(initial = emptySet())
    val favPoems = poems.filter { favs.contains(it.id) }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        items(favPoems) { poem ->
            Card(modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
                .clickable { navController.navigate("poem/${Uri.encode(poem.id)}") }) {
                Text(text = poem.title, modifier = Modifier.padding(12.dp))
            }
        }
    }
}

