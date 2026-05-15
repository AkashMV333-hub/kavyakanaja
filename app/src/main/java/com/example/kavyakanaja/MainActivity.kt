package com.example.kavyakanaja

import android.os.Bundle
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.kavyakanaja.ui.screens.HomeScreen
import com.example.kavyakanaja.ui.screens.PoemDetailScreen
import com.example.kavyakanaja.ui.screens.PoetsScreen
import com.example.kavyakanaja.ui.screens.PoetDetailScreen
import com.example.kavyakanaja.ui.screens.FavoritesScreen
import com.example.kavyakanaja.ui.theme.KavyaKanajaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KavyaKanajaTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = MaterialTheme.colorScheme.background
                ) { innerPadding ->
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = "home", modifier = Modifier.padding(innerPadding)) {
                        composable("home") { HomeScreen(navController) }
                        composable("poem/{poemId}") { backStackEntry ->
                            val id = backStackEntry.arguments?.getString("poemId")?.let { Uri.decode(it) }
                            PoemDetailScreen(navController, id)
                        }
                        composable("poets") { PoetsScreen(navController) }
                        composable("poet/{poetName}") { backStackEntry ->
                            val poet = backStackEntry.arguments?.getString("poetName")?.let { Uri.decode(it) }
                            PoetDetailScreen(navController, poet)
                        }
                        composable("favorites") { FavoritesScreen(navController) }
                    }
                }
            }
        }
    }
}

