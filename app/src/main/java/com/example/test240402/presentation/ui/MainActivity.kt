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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
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
import com.example.test240402.presentation.ui.TodoItemRow

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

    // --- 삭제 관련 상태 ---
    var itemToDelete: TodoItem? by remember { mutableStateOf(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // --- 수정 관련 상태 ---
    var itemToEdit: TodoItem? by remember { mutableStateOf(null) }
    var showEditDialog by remember { mutableStateOf(false) }

    // 수정 다이얼로그용 텍스트 필드 상태 (itemToEdit이 변경될 때 업데이트)
    var editTextContent by remember(itemToEdit) {
        mutableStateOf(itemToEdit?.content ?: "")
    }
    // editTextMemo는 nullable String이므로, String? 타입으로 유지
    var editTextMemo: String by remember(itemToEdit) { // null일 경우 빈 문자열로 초기화
        mutableStateOf(itemToEdit?.memo ?: "")
    }

    LaunchedEffect(contentList) {
        showContent.value = false
        delay(300)
        showContent.value = true
    }
    Log.d("데이터", "ContentList in MainView: $contentList") // 로그 메시지 명확화
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
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text(text = "add") },
                icon = { Icon(Icons.Filled.Add, contentDescription = "새로운 Todo 추가") }, // contentDescription 추가
                onClick = {
                    // 스낵바를 통한 네비게이션은 UX적으로 고려 필요, 여기서는 직접 네비게이션으로 변경 가능
                    // scope.launch {
                    //     val result = snackbarHostState.showSnackbar(
                    //         "todo리스트를 작성하시겠습니까?", "이동", duration = SnackbarDuration.Indefinite
                    //     )
                    //     if (result == SnackbarResult.ActionPerformed) {
                    //         navController.navigate("InputView")
                    //     }
                    // }
                    navController.navigate("InputView") // 직접 네비게이션
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.background)
        ) {
            if (contentList.isEmpty() && showContent.value) { // 조건 명확화
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "데이터가 없습니다. 추가해주세요!") // 사용자 안내 메시지 개선
                }
            } else if (contentList.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(), // LazyColumn이 전체 공간 차지하도록
                    contentPadding = PaddingValues(vertical = 8.dp) // 리스트 상하 패딩
                ) {
                    items(
                        items = contentList, // items 파라미터 명시
                        key = { todoItem -> todoItem.id } // 고유 ID가 있다면 key로 사용 (성능 향상)
                    ) { currentItem ->
                        TodoItemRow(
                            currentItem = currentItem,
                            onUpdateItem = { updatedItem ->
                                Log.d("MainView", "onUpdateItem for ${updatedItem.content}, isDone: ${updatedItem.isDone}")
                                viewModel.updateItem(updatedItem)
                            },
                            // --- 콜백 함수 전달 방식으로 변경 ---
                            onRequestDeleteItem = { todoItemFromRow ->
                                Log.d("MainView", "onRequestDeleteItem CALLED from Row for: ${todoItemFromRow.content}")
                                itemToDelete = todoItemFromRow      // MainView의 상태 업데이트
                                showDeleteDialog = true             // MainView의 상태 업데이트
                                Log.d("MainView", "itemToDelete set to: ${itemToDelete?.content}, showDeleteDialog set to: $showDeleteDialog")
                            },
                            onRequestEditItem = { todoItemFromRow ->
                                Log.d("MainView", "onRequestEditItem CALLED from Row for: ${todoItemFromRow.content}")
                                itemToEdit = todoItemFromRow        // MainView의 상태 업데이트
                                // editTextContent와 editTextMemo는 remember(itemToEdit)에 의해 자동 업데이트됩니다.
                                showEditDialog = true               // MainView의 상태 업데이트
                                Log.d("MainView", "itemToEdit set to: ${itemToEdit?.content}, showEditDialog set to: $showEditDialog")
                            }
                        )
                        // 각 아이템 사이에 Divider를 추가하여 구분 (선택 사항)
                        if (contentList.last() != currentItem) { // 마지막 아이템이 아닐 때만 Divider 표시
                            Divider(modifier = Modifier.padding(horizontal = 16.dp))
                        }
                    }
                }
            } else {
                // 데이터 로딩 중이거나 showContent.value가 false인 경우 (예: 로딩 인디케이터 표시)
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            // --- 다이얼로그 정의 ---
            // 삭제 확인 다이얼로그
            if (showDeleteDialog && itemToDelete != null) {
                Log.d("MainView", "Delete AlertDialog SHOULD BE VISIBLE for: ${itemToDelete?.content}")
                AlertDialog(
                    onDismissRequest = {
                        Log.d("MainView", "Delete Dialog onDismissRequest")
                        showDeleteDialog = false
                        itemToDelete = null // 다이얼로그 닫힐 때 선택된 아이템 초기화
                    },
                    title = { Text("삭제 확인") },
                    text = { Text("정말로 '${itemToDelete?.content}' 항목을 삭제하시겠습니까?") },
                    confirmButton = {
                        Button(
                            onClick = {
                                Log.d("MainView", "Delete Dialog CONFIRM clicked for: ${itemToDelete?.content}")
                                itemToDelete?.let { viewModel.deleteItem(it) }
                                showDeleteDialog = false
                                itemToDelete = null
                                scope.launch { snackbarHostState.showSnackbar("삭제되었습니다.") }
                            }
                        ) { Text("삭제") }
                    },
                    dismissButton = {
                        Button(onClick = {
                            Log.d("MainView", "Delete Dialog DISMISS clicked")
                            showDeleteDialog = false
                            itemToDelete = null
                        }) { Text("취소") }
                    }
                )
            }

            // 수정 다이얼로그
            if (showEditDialog && itemToEdit != null) {
                Log.d("MainView", "Edit AlertDialog SHOULD BE VISIBLE for: ${itemToEdit?.content}")
                AlertDialog(
                    onDismissRequest = {
                        Log.d("MainView", "Edit Dialog onDismissRequest")
                        showEditDialog = false
                        itemToEdit = null // 다이얼로그 닫힐 때 선택된 아이템 초기화
                    },
                    title = { Text("Todo 수정") },
                    text = {
                        Column {
                            OutlinedTextField(
                                value = editTextContent,
                                onValueChange = { editTextContent = it },
                                label = { Text("할 일") },
                                singleLine = true, // 할 일은 보통 한 줄
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = editTextMemo, // nullable String이므로 직접 사용
                                onValueChange = { editTextMemo = it },
                                label = { Text("메모 (선택 사항)") },
                                modifier = Modifier.fillMaxWidth(),
                                maxLines = 3 // 메모는 여러 줄 가능
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                itemToEdit?.let { currentTodo ->
                                    Log.d("MainView", "Edit Dialog CONFIRM clicked for: ${currentTodo.content}")
                                    val updatedTodo = currentTodo.copy(
                                        content = editTextContent,
                                        memo = editTextMemo.ifBlank { null } // 비어있으면 null로 저장
                                    )
                                    viewModel.updateItem(updatedTodo)
                                }
                                showEditDialog = false
                                itemToEdit = null
                                scope.launch { snackbarHostState.showSnackbar("수정되었습니다.") }
                            },
                            enabled = editTextContent.isNotBlank() // 할 일이 비어있지 않을 때만 활성화
                        ) { Text("수정") }
                    },
                    dismissButton = {
                        Button(onClick = {
                            Log.d("MainView", "Edit Dialog DISMISS clicked")
                            showEditDialog = false
                            itemToEdit = null
                        }) { Text("취소") }
                    }
                )
            }
        } // Box 끝
    } // Scaffold의 content 람다 끝
} // MainView 끝

//@HiltViewModel
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputView(navController: NavController) {

    val viewModel: InputViewModel = hiltViewModel()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }



//    viewModel.initData(item = viewModel.item ?: ContentEntity(content = "", memo = ""))
    val content by viewModel.content.collectAsState()
    val memo by viewModel.memo.collectAsState()
    Scaffold(topBar = {
        TopAppBar(
            title = {
                Text(
                    text = "Todo List",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "뒤로가기")
                }
            },
            actions = {
                IconButton(
                    onClick = {
                        if (content.isNotBlank()) {
                            viewModel.insertData()
                            navController.popBackStack()
                        }else{
                            scope.launch { snackbarHostState.showSnackbar("할일을 입력해주세요.") }
                        }

                    },enabled = content.isNotBlank()
                ) {
                    Icon(Icons.Filled.Done, contentDescription = "저장")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )

        )
    }, snackbarHost = {SnackbarHost(hostState = snackbarHostState)}

    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(color = MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = content,
                onValueChange = { viewModel.updateContent(it) }, // ViewModel 함수 호출
                label = { Text("할 일") },
                placeholder = { Text("내용을 입력하세요") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = memo, // memo가 null을 허용하지 않는 String이므로 직접 사용
                onValueChange = { viewModel.updateMemo(it) }, // ViewModel 함수 호출
                label = { Text("메모 (선택 사항)") },
                placeholder = { Text("추가 메모를 입력하세요") },
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 120.dp),
                maxLines = 5
            )
        }



//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(innerPadding)
//                .background(color = Color.White)
//        ) {
//            Column(
//                Modifier
//                    .fillMaxSize()
//                    .padding(12.dp)
//            ) {
////            Button(onClick = { navController.popBackStack() }) {
////                Text(text = "go to first")
////            }
//                Spacer(modifier = Modifier.height(10.dp))
//                OutlinedTextField(
//                    value = content.value,
////                value = viewModel.content.value?.let { if (it.isEmpty()) "" else it.toString() },
//                    onValueChange = {
//                        content.value = it
//                        viewModel.content.value = it
//                    },
//                    label = { Text(text = "할일", color = Color.Red) },
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .clip(RoundedCornerShape(6.dp))
//                        .align(alignment = Alignment.CenterHorizontally),
//                    placeholder = { Text(text = "내용", color = Color.LightGray) })
//
//                Spacer(modifier = Modifier.height(10.dp))
//                TextField(
//                    value = memo.value,
//                    onValueChange = {
//                        memo.value = it
//                        viewModel.memo.value = it
//                    },
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .clip(RoundedCornerShape(16.dp))
//                        .align(alignment = Alignment.CenterHorizontally),
//                    placeholder = { Text(text = "메모", color = Color.LightGray) })
//
//            }
//            Button(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .align(Alignment.BottomCenter), onClick = {
//                    Log.d("라이브데이터확인", "제목: ${viewModel.content.value}, 메모: ${viewModel.memo.value}")
//                    viewModel.insertData()
//                    navController.popBackStack()
//                }) {
//                Text(text = "입력완료")
//            }
//
//        }
    }

}