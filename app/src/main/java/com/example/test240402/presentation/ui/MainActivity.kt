package com.example.test240402.presentation.ui

import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.setValue
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
import com.example.test240402.domain.model.TodoItem
import com.example.test240402.presentation.viewmodel.InputViewModel
import com.example.test240402.presentation.viewmodel.MainViewModel
import com.example.test240402.ui.theme.Test240402Theme
import com.example.test240402.ui.theme.Todo
import com.example.test240402.ui.theme.Icon_button_pastel
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

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun MainView(navController: NavController) {
    val scope = rememberCoroutineScope()
    val viewModel: MainViewModel = hiltViewModel()

    val contentList by viewModel.todoList.collectAsState()
    val showContent = remember { mutableStateOf(false) } // 데이터 표시 여부 상태

    val showDeleteDialog = remember { mutableStateOf(false) }
    val showEditDialog = remember { mutableStateOf(false) }
    val itemToDelete = remember { mutableStateOf<TodoItem?>(null) }
    val itemToEdit = remember { mutableStateOf<TodoItem?>(null) }

    var editTextContent by remember(itemToEdit.value) {
        mutableStateOf(itemToEdit.value?.content ?: "")
    }
    var editTextMemo: String? by remember(itemToEdit.value) {
        mutableStateOf(itemToEdit.value?.memo ?: "")
    }
    LaunchedEffect(contentList) {
        // contentList가 업데이트될 때마다
        showContent.value = false // 데이터 숨김
        delay(300) // 짧은 딜레이
        showContent.value = true  // 데이터 표시
    }
    Log.d("데이터", "${contentList}")
    val snackbarHostState = remember { SnackbarHostState() }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Todo List",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
                ,colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer, // 예시 M3 색상
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
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
                .background(color = MaterialTheme.colorScheme.background)
        ) {
//            Button(onClick = { navController.navigate("InputView") }) {
//                Text(text = "go to Input")
//            }
            LazyColumn() {
//                Log.d("데이터확인", "${viewModel.contentList.value}\n,${viewModel}")
                if (contentList.isNotEmpty()) {
                    items(contentList.size) { index->
                        val currentItem  =contentList[index]
                        Row(
                            Modifier
                                .background(color = Todo)
                                .fillMaxWidth()
                                .combinedClickable(
                                    onClick = {},
                                    onLongClick = {
                                        scope.launch {
//                                            val result = snackbarHostState.showSnackbar(
//                                                "해당 todo 리스트를 삭제 하시겠습니까?",
//                                                "삭제",
//                                                duration = SnackbarDuration.Indefinite
//                                            )
//                                            viewModel.deleteItem(contentList[it])
//                                            when (result) {
//                                                SnackbarResult.Dismissed -> {}
//                                                SnackbarResult.ActionPerformed -> {}
//                                            }
                                            itemToDelete.value=currentItem
                                            showDeleteDialog.value = true

                                        }
                                    }).padding(8.dp),verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = contentList[index].isDone,
                                onCheckedChange = { checked ->
                                    viewModel.updateItem(currentItem.copy(isDone = checked))
                                })
                            Spacer(modifier = Modifier.height(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "할일: " + currentItem.content,
                                    color = Color.Red,
                                    fontStyle = FontStyle.Italic,
                                    fontWeight = FontWeight(1),
                                    fontSize = 16.sp
                                )
                                currentItem.memo?.let { memo->
                                    Text(
                                        text = "메모: " + currentItem.memo,
                                        color = Color.Blue,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                            IconButton(onClick = {
                                itemToEdit.value = currentItem
                                editTextContent = currentItem.content
                                editTextMemo =currentItem.memo
                                showEditDialog.value = true
                            }, Modifier.background(color = Icon_button_pastel),
                            ) { Icon(Icons.Filled.Edit, contentDescription = "") }

                        }

                    }
                } else if (showContent.value && contentList.isEmpty()) {
                    item {
                        Text(text = "데이터가 없습니다.${contentList.size}")
                    }
                } else {
                    //
                }
            }
            if (showDeleteDialog.value) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog.value = false
                        itemToDelete.value=null},
                    title = { Text(text = "삭제 확인") },
                    text = { Text(text = "${itemToDelete.value?.content ?: ""}를 삭제 하시겠습니까?") },
                    confirmButton = {Button(
                        onClick = {
                            itemToDelete.value?.let { viewModel.deleteItem(it) }
                            showDeleteDialog.value = false
                            itemToDelete.value = null
                        }
                    ) {
                        Text("삭제")
                    }},
                    dismissButton = {
                        Button(onClick = {
                        showDeleteDialog.value = false
                        itemToDelete.value = null
                    }) {
                        Text("취소")
                    }}
                )
            }
            if (showEditDialog.value && itemToEdit.value != null) {
                AlertDialog(
                    onDismissRequest = {
                        showEditDialog.value = false
                        itemToEdit.value = null
                    },
                    title = { Text("Todo 수정") },
                    text = {
                        Column {
                            OutlinedTextField(
                                value = editTextContent,
                                onValueChange = { editTextContent = it },
                                label = { Text("할 일") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            editTextMemo?.let { it1 ->
                                OutlinedTextField(
                                    value = it1,
                                    onValueChange = { editTextMemo = it },
                                    label = { Text("메모 (선택 사항)") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                itemToEdit.value?.let { currentTodo ->
                                    val updatedTodo = currentTodo.copy(
                                        content = editTextContent,
                                        memo = editTextMemo?.ifBlank { null } // 비어있으면 null로 저장
                                    )
                                    viewModel.updateItem(updatedTodo)
                                }
                                showEditDialog.value = false
                                itemToEdit.value = null
                            },
                            // 할 일이 비어있지 않을 때만 활성화
                            enabled = editTextContent.isNotBlank()
                        ) {
                            Text("수정")
                        }
                    },
                    dismissButton = {
                        Button(onClick = {
                            showEditDialog.value = false
                            itemToEdit.value = null
                        }) {
                            Text("취소")
                        }
                    }
                )
            }



        }


    }
}

//@HiltViewModel
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputView(navController: NavController) {

    val viewModel: InputViewModel = hiltViewModel()
//    viewModel.initData(item = viewModel.item ?: ContentEntity(content = "", memo = ""))
    var content: MutableState<String> =
        remember { mutableStateOf(viewModel.content.value.toString() ) }
    var memo: MutableState<String> =
        remember { mutableStateOf(viewModel.memo.value.toString() ) }

    Scaffold(topBar = {
        TopAppBar(
            title = {
                Text(
                    text = "Todo List",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
            ,colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer, // 예시 M3 색상
                titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
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