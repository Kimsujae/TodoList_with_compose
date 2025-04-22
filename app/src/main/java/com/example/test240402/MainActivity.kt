package com.example.test240402

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import com.example.test240402.data.AppDatabase
import com.example.test240402.ui.theme.Test240402Theme
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch


lateinit var db: AppDatabase

@AndroidEntryPoint
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
                    db = Room.databaseBuilder(
                        applicationContext,
                        AppDatabase::class.java,
                        "DB"
                    ).build()
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
        InputView(navController = navController, viewModel = hiltViewModel())
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview2() {
    val navController = rememberNavController()
    Test240402Theme {
        MainView(navController = navController)
    }
}

@Composable
fun MainView() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "MainView") {
        composable("MainView") { MainView(navController = navController) }
        composable("InputView") {
            InputView(
                navController = navController,
                viewModel = hiltViewModel()
            )
        }
    }
}

@Composable
fun MainView(navController: NavController) {
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
            Button(onClick = { navController.navigate("InputView") }) {
                Text(text = "go to second")
            }
            LazyColumn(modifier = Modifier.align(Alignment.Center)) {
                for (i in 1..5) {
                    item { Text(text = "test $i") }
                }
            }
        }
    }


}

//@HiltViewModel
@Composable
fun InputView(navController: NavController, viewModel: InputViewModel = hiltViewModel()) {

    var content: MutableState<String> =
        remember { mutableStateOf(viewModel.content.value?.toString() ?: "") }
    var memo: MutableState<String> =
        remember { mutableStateOf(viewModel.memo.value?.toString() ?: "") }
    Box(
        modifier = Modifier
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
            OutlinedTextField(
                value = content.value,
//                value = viewModel.content.value?.let { if (it.isEmpty()) "" else it.toString() },
                onValueChange = {
                    content.value = it
                    viewModel.content.value = it
                },
                label = { Text(text = "할일", color = Color.Red) },
                modifier = Modifier
                    .fillMaxWidth()
                    .align(alignment = Alignment.CenterHorizontally),
                placeholder = { Text(text = "내용", color = Color.LightGray) })

            Spacer(modifier = Modifier.height(10.dp))
            TextField(
                value = memo.value,
                onValueChange = {
                    memo.value = it
                    viewModel.memo.value = it
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .align(alignment = Alignment.CenterHorizontally),
                placeholder = { Text(text = "메모", color = Color.LightGray) }
            )

        }
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter), onClick = {
                Log.d("라이브데이터확인", "제목: ${viewModel.content.value}, 메모: ${viewModel.memo.value}")
                navController.popBackStack()
            }) {
            Text(text = "입력완료")
        }

    }
}