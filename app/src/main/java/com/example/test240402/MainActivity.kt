package com.example.test240402

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.test240402.ui.theme.Test240402Theme
import kotlinx.coroutines.launch

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

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    val navController = rememberNavController()
    Test240402Theme {
        SecondView(navController = navController)
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview2() {
    val navController = rememberNavController()
    Test240402Theme {
        FirstView(navController = navController)
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
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text(text = "add") },
                icon = { Icon(Icons.Filled.Add, contentDescription = "") },
                onClick = {
                    scope.launch {
                        val result = snackbarHostState.showSnackbar(
                            "test",
                            "Action",
                            duration = SnackbarDuration.Indefinite
                        )
                        when (result) {
                            SnackbarResult.ActionPerformed -> {}
                            SnackbarResult.Dismissed -> {}
                        }

                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(color = Color.White)
        ) {
            Button(onClick = { navController.navigate("SecondView") }) {
                Text(text = "go to second")
            }
            LazyColumn(modifier = Modifier.align(Alignment.Center)) {
                for (i in 1..5) {
                    item { Text(text = "test $i") }
                }
            }
        }
    }
//        FloatingActionButton(modifier = Modifier
//            .align(Alignment.BottomEnd)
//            .padding(10.dp), onClick = {}) {
//            Icon(Icons.Filled.Add, "fab")
//        }

}

@Composable
fun SecondView(navController: NavController) {
    var text by remember { mutableStateOf("") }
    Box(
        modifier = Modifier
            .padding(10.dp)
            .fillMaxSize()
            .background(color = Color.White)
    )
    {
        Column(
            Modifier
                .fillMaxSize()
        ) {
            Button(onClick = { navController.popBackStack() }) {
                Text(text = "go to first")
            }
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(value = text,
                onValueChange = { text = it },
                label = { Text(text = "할일", color = Color.Red) },
                modifier = Modifier.fillMaxWidth()
                    .align(alignment = Alignment.CenterHorizontally),
                placeholder = { Text(text = "입력해주세요", color = Color.LightGray) })
            Spacer(modifier = Modifier.height(10.dp))
            TextField(
                value = "",
                onValueChange = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .align(alignment = Alignment.CenterHorizontally),
                placeholder = { Text(text = "메모", color = Color.LightGray) }
            )

        }
        Button(modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.BottomCenter), onClick = { navController.popBackStack() }) {
            Text(text = "입력완료")
        }

    }
}