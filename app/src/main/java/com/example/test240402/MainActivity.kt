package com.example.test240402

import android.R
import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.test240402.ui.theme.Test240402Theme
import com.example.test240402.ui.theme.Todo
import com.example.test240402.ui.theme.TopAppbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContent {
            Test240402Theme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) {

                    val navController = rememberNavController()
                    MainAndInputScreen(navController)

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
        InputView(navController = navController)
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
fun MainAndInputScreen(navController: NavController) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "MainView") {
        composable("MainView") {
            MainView(
                navController = navController
            )
        }
        composable("InputView") {
            InputView(
                navController = navController
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun MainView(navController: NavController) {
    val scope = rememberCoroutineScope()
    val viewModel: MainViewModel = hiltViewModel()
    val contentList by viewModel.contentList.collectAsState()
    val showContent = remember { mutableStateOf(false) } // 데이터 표시 여부 상태
    LaunchedEffect(contentList) {
        // contentList가 업데이트될 때마다
        showContent.value = false // 데이터 숨김
        delay(300) // 짧은 딜레이
        showContent.value = true  // 데이터 표시
    }
    Log.d("데이터", "${viewModel.contentList.value}")
    val snackbarHostState = remember { SnackbarHostState() }
    Scaffold(
        topBar = {
            Text(
                text = "Todo List",
                color = Color.Black,
                fontSize = 20.sp,
                modifier = Modifier
                    .background(color = TopAppbar)
                    .fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text(text = "add") },
                icon = { Icon(Icons.Filled.Add, contentDescription = "") },
                onClick = {
                    scope.launch {
                        val result = snackbarHostState.showSnackbar(
                            "todo리스트를 작성하시겠습니까?", "이동", duration = SnackbarDuration.Indefinite
                        )
                        navController.navigate("InputView")
                        when (result) {
                            SnackbarResult.ActionPerformed -> {}
                            SnackbarResult.Dismissed -> {}
                        }

                    }
                })
        }) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(color = Color.White)
        ) {
//            Button(onClick = { navController.navigate("InputView") }) {
//                Text(text = "go to Input")
//            }
            LazyColumn() {
//                Log.d("데이터확인", "${viewModel.contentList.value}\n,${viewModel}")
                if (viewModel.contentList.value.isNotEmpty()) {
                    items(contentList.size) {
                        Row(
                            Modifier
                                .background(color = Todo)
                                .fillMaxWidth()
                                .combinedClickable(
                                    onClick = {},
                                    onLongClick = {
                                        scope.launch {
                                            val result = snackbarHostState.showSnackbar(
                                                "해당 todo 리스트를 삭제 하시겠습니까?",
                                                "삭제",
                                                duration = SnackbarDuration.Indefinite
                                            )
                                            viewModel.deleteItem(contentList[it])
                                            when (result) {
                                                SnackbarResult.Dismissed -> {}
                                                SnackbarResult.ActionPerformed -> {}
                                            }


                                        }
                                    })


                        ) {
                            Checkbox(
                                checked = contentList[it].isDone,
                                onCheckedChange = { checked ->
                                    it
                                    viewModel.updateItem(contentList[it].copy(isDone = !contentList[it].isDone))
                                })
                            Spacer(modifier = Modifier.height(10.dp))
                            Column {
                                Text(
                                    text = "할일: " + contentList[it].content,
                                    color = Color.Red,
                                    fontStyle = FontStyle.Italic,
                                    fontWeight = FontWeight(1),
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = "메모: " + contentList[it].memo,
                                    color = Color.Blue,
                                    fontSize = 12.sp
                                )
                            }

                        }

                    }
                } else if (showContent.value && contentList.isEmpty()) {
                    item {
                        Text(text = "데이터가 없습니다.${viewModel.contentList.value.size}")
                    }
                } else {
                    //
                }
            }
        }


    }
}

//@HiltViewModel
@Composable
fun InputView(navController: NavController) {

    val viewModel: InputViewModel = hiltViewModel()
//    viewModel.initData(item = viewModel.item ?: ContentEntity(content = "", memo = ""))
    var content: MutableState<String> =
        remember { mutableStateOf(viewModel.content.value?.toString() ?: "") }
    var memo: MutableState<String> =
        remember { mutableStateOf(viewModel.memo.value?.toString() ?: "") }

    Scaffold(topBar = {
        Text(
            text = "Todo List",
            color = Color.Black,
            fontSize = 20.sp,
            modifier = Modifier
                .background(color = TopAppbar)
                .fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(color = Color.White)
        ) {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
//            Button(onClick = { navController.popBackStack() }) {
//                Text(text = "go to first")
//            }
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
                        .clip(RoundedCornerShape(6.dp))
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
                        .clip(RoundedCornerShape(16.dp))
                        .align(alignment = Alignment.CenterHorizontally),
                    placeholder = { Text(text = "메모", color = Color.LightGray) })

            }
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter), onClick = {
                    Log.d("라이브데이터확인", "제목: ${viewModel.content.value}, 메모: ${viewModel.memo.value}")
                    viewModel.insertData()
                    navController.popBackStack()
                }) {
                Text(text = "입력완료")
            }

        }
    }

}