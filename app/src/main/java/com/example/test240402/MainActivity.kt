package com.example.test240402

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.test240402.ui.theme.Test240402Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Test240402Theme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainView()
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Test240402Theme {
        Greeting("Android")
    }
}

@Composable
fun MainView() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "firstView") {
        composable("firstView") { FirstView(navController = navController) }
        composable("SecondView") { SecondView(navController = navController) }
    }
}

@Composable
fun FirstView(navController: NavController) {
    Box(modifier = Modifier.fillMaxSize())
    {
        Button(onClick = { navController.navigate("SecondView") }) {
            Text(text = "go to second")
        }
    }
}

@Composable
fun SecondView(navController: NavController) {
    Box(modifier = Modifier.fillMaxSize())
    {
        Button(onClick = { navController.popBackStack() }) {
            Text(text = "go to first")
        }
    }
}