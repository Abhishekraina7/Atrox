package com.example.atrox.ui.home.notes

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AddCircleOutline
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.CheckCircleOutline
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.Redo
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Undo
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNotesScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddNotesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showMenu by remember { mutableStateOf(false) }
    var showAttachmentSheet by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    val colors = MaterialTheme.colorScheme

    // ── Photo Picker launcher (no permission needed on API 33+ with PickVisualMedia) ──
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris ->
        if (uris.isNotEmpty()) {
            viewModel.addImagesFromUris(uris)
            showAttachmentSheet = false
        }
    }

    // ── Permission launcher for legacy devices (API < 33) ──
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            photoPickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }
    }

    // Helper to open gallery with appropriate permission flow
    val onPhotosClick: () -> Unit = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // API 33+ — PickVisualMedia handles its own permission
            photoPickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        } else {
            // API < 33 — need READ_EXTERNAL_STORAGE
            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

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
                                        .background(colors.error, shape = CircleShape)
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
                IconButton(onClick = { showAttachmentSheet = true }) {
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

            // ── Attached Images ──
            if (uiState.attachedImages.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))

                uiState.attachedImages.forEach { imagePath ->
                    NoteAttachedImage(
                        imagePath = imagePath,
                        onRemove = { viewModel.removeImage(imagePath) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }

    // ── Attachment Bottom Sheet ──
    if (showAttachmentSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAttachmentSheet = false },
            containerColor = colors.surfaceContainerHigh,
            tonalElevation = 0.dp,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            dragHandle = null
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 32.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Add",
                        color = colors.onSurface,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    IconButton(onClick = { showAttachmentSheet = false }) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Close",
                            tint = colors.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Attachment options grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    AttachmentOption(
                        icon = Icons.Rounded.PhotoLibrary,
                        label = "Photos",
                        containerColor = colors.surfaceContainerHighest,
                        iconTint = colors.onSurfaceVariant,
                        onClick = onPhotosClick
                    )
                    AttachmentOption(
                        icon = Icons.Rounded.CameraAlt,
                        label = "Camera",
                        containerColor = colors.surfaceContainerHighest,
                        iconTint = colors.onSurfaceVariant,
                        onClick = { /* TODO: open camera */ }
                    )
                }
            }
        }
    }
}

// ── Attached image with remove button ────────────────────────────────

@Composable
private fun NoteAttachedImage(
    imagePath: String,
    onRemove: () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(File(imagePath))
                .crossfade(300)
                .build(),
            contentDescription = "Attached image",
            contentScale = ContentScale.FillWidth,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
        )

        // Remove button
        IconButton(
            onClick = onRemove,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
                .size(28.dp)
                .background(
                    color = colors.surface.copy(alpha = 0.7f),
                    shape = CircleShape
                )
        ) {
            Icon(
                imageVector = Icons.Rounded.Close,
                contentDescription = "Remove image",
                tint = colors.onSurface,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

// ── Attachment option tile ───────────────────────────────────────────

/**
 * A single attachment option tile — rounded square icon with a label below.
 */
@Composable
private fun AttachmentOption(
    icon: ImageVector,
    label: String,
    containerColor: androidx.compose.ui.graphics.Color,
    iconTint: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(88.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(containerColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconTint,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            color = iconTint,
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )
    }
}
