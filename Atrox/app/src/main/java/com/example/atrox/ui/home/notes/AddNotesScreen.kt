package com.example.atrox.ui.home.notes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AddCircleOutline
import androidx.compose.material.icons.rounded.CheckCircleOutline
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Redo
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Undo
import androidx.compose.material3.*
import androidx.compose.material3.BottomAppBarDefaults.windowInsets
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.atrox.R

// Theme colors consistent with the app
private val ColorBackground = Color(0xFF0A0A0F)
private val ColorTextPrimary = Color(0xFFFFFFFF)
private val ColorTextSecondary = Color(0xFF8888A0)
private val ColorSurface = Color(0xFF14141E)
private val ColorAccent = Color(0xFF6C63FF)
private val ColorRedDot = Color(0xFFE53935)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNotesScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddNotesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showMenu by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = ColorBackground,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = ColorTextPrimary
                        )
                    }
                },
                title = {},
                actions = {
                    // Undo
                    IconButton(
                        onClick = { viewModel.undo() },
                        enabled = viewModel.canUndo
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Undo,
                            contentDescription = "Undo",
                            tint = if (viewModel.canUndo) ColorTextPrimary else ColorTextSecondary
                        )
                    }
                    // Redo
                    IconButton(
                        onClick = { viewModel.redo() },
                        enabled = viewModel.canRedo
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Redo,
                            contentDescription = "Redo",
                            tint = if (viewModel.canRedo) ColorTextPrimary else ColorTextSecondary
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Share
                    IconButton(onClick = { /* share action */ }) {
                        Icon(
                            imageVector = Icons.Rounded.Share,
                            contentDescription = "Share",
                            tint = ColorTextPrimary
                        )
                    }

                    // 3-dot menu with red dot indicator
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Box {
                                Icon(
                                    imageVector = Icons.Rounded.MoreVert,
                                    contentDescription = "More Options",
                                    tint = ColorTextPrimary
                                )
                                // Red dot indicator
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .align(Alignment.TopEnd)
                                        .background(ColorRedDot, shape = androidx.compose.foundation.shape.CircleShape)
                                )
                            }
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            modifier = Modifier.background(ColorSurface)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Delete", color = ColorRedDot) },
                                onClick = { showMenu = false }
                            )
                            DropdownMenuItem(
                                text = { Text("Pin note", color = ColorTextPrimary) },
                                onClick = { showMenu = false }
                            )
                            DropdownMenuItem(
                                text = { Text("Labels", color = ColorTextPrimary) },
                                onClick = { showMenu = false }
                            )
                        }
                    }
                },
                windowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ColorBackground
                )
            )
        },
        bottomBar = {
            // Bottom Toolbar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ColorSurface)
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
                        tint = ColorTextSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                // Voice to text
                IconButton(onClick = { /* voice input */ }) {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircleOutline, // placeholder for mic+text icon
                        contentDescription = "Voice Input",
                        tint = ColorTextSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                // Add attachment
                IconButton(onClick = { /* add attachment */ }) {
                    Icon(
                        imageVector = Icons.Rounded.AddCircleOutline,
                        contentDescription = "Add",
                        tint = ColorTextSecondary,
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
        ) {
            // Title Field
            BasicTextField(
                value = uiState.title,
                onValueChange = { viewModel.updateTitle(it) },
                textStyle = TextStyle(
                    color = ColorTextPrimary,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                ),
                cursorBrush = SolidColor(ColorAccent),
                decorationBox = { innerTextField ->
                    if (uiState.title.isEmpty()) {
                        Text(
                            text = "Title",
                            color = ColorTextSecondary,
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
                    color = ColorTextPrimary,
                    fontSize = 16.sp,
                    lineHeight = 24.sp
                ),
                cursorBrush = SolidColor(ColorAccent),
                decorationBox = { innerTextField ->
                    if (uiState.body.isEmpty()) {
                        Text(
                            text = "Start typing...",
                            color = ColorTextSecondary,
                            fontSize = 16.sp
                        )
                    }
                    innerTextField()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
        }
    }
}
