package com.example.atrox.ui.home.notes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AddCircleOutline
import androidx.compose.material.icons.rounded.CheckCircleOutline
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Redo
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Undo
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNotesScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddNotesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showMenu by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    val colors = MaterialTheme.colorScheme

    Scaffold(
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = colors.onBackground
                        )
                    }
                },
                title = {},
                actions = {
                    // Undo
                    IconButton(
                        onClick = { viewModel.undo() },
                        enabled = uiState.undoStack.isNotEmpty()
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Undo,
                            contentDescription = "Undo",
                            tint = if (uiState.undoStack.isNotEmpty()) colors.onBackground else colors.onSurfaceVariant
                        )
                    }
                    // Redo
                    IconButton(
                        onClick = { viewModel.redo() },
                        enabled = uiState.redoStack.isNotEmpty()
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Redo,
                            contentDescription = "Redo",
                            tint = if (uiState.redoStack.isNotEmpty()) colors.onBackground else colors.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Share
                    IconButton(onClick = { /* share action */ }) {
                        Icon(
                            imageVector = Icons.Rounded.Share,
                            contentDescription = "Share",
                            tint = colors.onBackground
                        )
                    }

                    // 3-dot menu with red dot indicator
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Box {
                                Icon(
                                    imageVector = Icons.Rounded.MoreVert,
                                    contentDescription = "More Options",
                                    tint = colors.onBackground
                                )
                                // Red dot indicator
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .align(Alignment.TopEnd)
                                        .background(colors.error, shape = androidx.compose.foundation.shape.CircleShape)
                                )
                            }
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            modifier = Modifier.background(colors.surfaceVariant)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Delete", color = colors.error) },
                                onClick = { showMenu = false }
                            )
                            DropdownMenuItem(
                                text = { Text("Pin note", color = colors.onBackground) },
                                onClick = { showMenu = false }
                            )
                            DropdownMenuItem(
                                text = { Text("Labels", color = colors.onBackground) },
                                onClick = { showMenu = false }
                            )
                        }
                    }
                },
                windowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.background
                )
            )
        },
        bottomBar = {
            // Bottom Toolbar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.surface)
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Checklist / Todo
                IconButton(onClick = { /* toggle checklist */ }) {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircleOutline,
                        contentDescription = "Checklist",
                        tint = colors.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }
                // Voice to text
                IconButton(onClick = { /* voice input */ }) {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircleOutline, // placeholder for mic+text icon
                        contentDescription = "Voice Input",
                        tint = colors.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }
                // Add attachment
                IconButton(onClick = { /* add attachment */ }) {
                    Icon(
                        imageVector = Icons.Rounded.AddCircleOutline,
                        contentDescription = "Add",
                        tint = colors.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .verticalScroll(scrollState)
        ) {
            // Title Field
            BasicTextField(
                value = uiState.title,
                onValueChange = { viewModel.updateTitle(it) },
                textStyle = TextStyle(
                    color = colors.onBackground,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                ),
                cursorBrush = SolidColor(colors.primary),
                decorationBox = { innerTextField ->
                    if (uiState.title.isEmpty()) {
                        Text(
                            text = "Title",
                            color = colors.onSurfaceVariant,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    innerTextField()
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Body Field
            BasicTextField(
                value = uiState.body,
                onValueChange = { viewModel.updateBody(it) },
                textStyle = TextStyle(
                    color = colors.onBackground,
                    fontSize = 16.sp,
                    lineHeight = 24.sp
                ),
                cursorBrush = SolidColor(colors.primary),
                decorationBox = { innerTextField ->
                    if (uiState.body.isEmpty()) {
                        Text(
                            text = "Start typing...",
                            color = colors.onSurfaceVariant,
                            fontSize = 16.sp
                        )
                    }
                    innerTextField()
                },
                modifier = Modifier
                    .fillMaxWidth()
            )
        }
    }
}
