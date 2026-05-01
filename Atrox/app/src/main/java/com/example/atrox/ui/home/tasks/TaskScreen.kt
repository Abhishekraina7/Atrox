package com.example.atrox.ui.home.tasks

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.atrox.data.tasks.TaskItem
import com.example.atrox.ui.theme.atroxColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskScreen(
    viewModel: TaskViewModel = hiltViewModel()
) {
    val tasks by viewModel.tasks.collectAsState()
    
    var showAddOverlay by remember { mutableStateOf(false) }
    var selectedTask by remember { mutableStateOf<TaskItem?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Your Tasks",
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 25.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.5).sp
                    )
                },
                windowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        floatingActionButton = {
            if (tasks.isNotEmpty()) {
                FloatingActionButton(
                    onClick = { showAddOverlay = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = CircleShape
                ) {
                    Icon(imageVector = Icons.Rounded.Add, contentDescription = "Add Task")
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (tasks.isEmpty()) {
                EmptyTaskState(onAddTaskClick = { showAddOverlay = true })
            } else {
                PendingTasksList(
                    tasks = tasks,
                    onTaskClick = { task -> selectedTask = task }
                )
            }
        }
    }

    // Overlays
    if (showAddOverlay) {
        AddTaskOverlay(
            onDismiss = { showAddOverlay = false },
            onCreate = { title, duration ->
                viewModel.addTask(title, duration)
                showAddOverlay = false
            }
        )
    }

    selectedTask?.let { task ->
        TaskActionOverlay(
            task = task,
            onDismiss = { selectedTask = null },
            onStartFocus = {
                // TODO: Start Sprint Timer Navigation
                selectedTask = null
            },
            onDiscard = {
                viewModel.removeTask(task.id)
                selectedTask = null
            }
        )
    }
}

@Composable
fun EmptyTaskState(onAddTaskClick: () -> Unit) {
    val atroxColors = MaterialTheme.atroxColors
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Layered Graphic
        Box(contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .offset(x = 12.dp, y = (-12).dp)
                    .background(atroxColors.cardElevated, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.TopEnd
            ) {
                Icon(
                    imageVector = Icons.Rounded.AccessTime,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(8.dp).size(20.dp)
                )
            }
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(atroxColors.cardDefault, RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.CheckCircleOutline,
                    contentDescription = null,
                    tint = atroxColors.indigoSoft, // Soft purple checkmark
                    modifier = Modifier.size(40.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Add your Tasks",
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Your workspace is currently silent.\nElevate your productivity by defining\nyour next objectives.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 15.sp,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onAddTaskClick,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.height(56.dp).fillMaxWidth()
        ) {
            Text("Add Tasks", color = MaterialTheme.colorScheme.onPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(8.dp))
            Icon(imageVector = Icons.Rounded.ArrowForward, contentDescription = null, modifier = Modifier.size(20.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskOverlay(
    onDismiss: () -> Unit,
    onCreate: (title: String, durationMin: Int) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedDuration by remember { mutableStateOf(25) }
    val durations = listOf(15, 25, 45, 60)
    val atroxColors = MaterialTheme.atroxColors

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = atroxColors.cardDefault,
        dragHandle = { BottomSheetDefaults.DragHandle(color = MaterialTheme.colorScheme.onSurfaceVariant) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 48.dp)
        ) {
            Text(
                text = "Create New Task",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Task Title Input
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                placeholder = { Text("What do you need to focus on?", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.background,
                    unfocusedContainerColor = MaterialTheme.colorScheme.background,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                    cursorColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))
            
            // Duration Selection
            Text(text = "DURATION", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                durations.forEach { duration ->
                    val isSelected = duration == selectedDuration
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = if (duration != 60) 8.dp else 0.dp)
                            .height(48.dp)
                            .background(
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.background,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { selectedDuration = duration },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${duration}m",
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Action Buttons
            Row(modifier = Modifier.fillMaxWidth()) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.height(56.dp).weight(1f)
                ) {
                    Text("Discard", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Button(
                    onClick = { if (title.isNotBlank()) onCreate(title, selectedDuration) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (title.isNotBlank()) MaterialTheme.colorScheme.primary else atroxColors.cardElevated
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.height(56.dp).weight(1.5f)
                ) {
                    Text("Create Task", color = MaterialTheme.colorScheme.onPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun PendingTasksList(tasks: List<TaskItem>, onTaskClick: (TaskItem) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .padding(top = 16.dp)
    ) {
        Text(
            text = "Pending Tasks",
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(24.dp))
        
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            items(tasks) { task ->
                PendingTaskRow(task = task, onClick = { onTaskClick(task) })
            }
        }
    }
}

@Composable
fun PendingTaskRow(task: TaskItem, onClick: () -> Unit) {
    val atroxColors = MaterialTheme.atroxColors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(atroxColors.cardDefault, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Tag/Duration
        Box(
            modifier = Modifier
                .background(atroxColors.cardElevated, RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text = "${task.durationMin}m",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))

        // Title
        Text(
            text = task.title,
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 16.sp,
            modifier = Modifier.weight(1f)
        )

        Icon(
            imageVector = Icons.Rounded.PlayArrow,
            contentDescription = "Start",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskActionOverlay(
    task: TaskItem,
    onDismiss: () -> Unit,
    onStartFocus: () -> Unit,
    onDiscard: () -> Unit
) {
    val atroxColors = MaterialTheme.atroxColors
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = atroxColors.cardDefault,
        dragHandle = { BottomSheetDefaults.DragHandle(color = MaterialTheme.colorScheme.onSurfaceVariant) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Info
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(MaterialTheme.colorScheme.background, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = Icons.Rounded.GpsFixed, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = task.title, color = MaterialTheme.colorScheme.onBackground, fontSize = 24.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "${task.durationMin} minutes reserved", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)

            Spacer(modifier = Modifier.height(48.dp))

            // Action Buttons
            Button(
                onClick = onStartFocus,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Icon(imageVector = Icons.Rounded.PlayArrow, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Start Focus Sprint", color = MaterialTheme.colorScheme.onPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedButton(
                onClick = onDiscard,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Icon(imageVector = Icons.Rounded.Delete, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Discard Task", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
