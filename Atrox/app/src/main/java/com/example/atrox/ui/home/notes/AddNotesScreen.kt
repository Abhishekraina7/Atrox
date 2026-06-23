package com.example.atrox.ui.home.notes

import android.Manifest
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.material.icons.automirrored.rounded.Redo
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.rounded.AddCircleOutline
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.CheckCircleOutline
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MicNone
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.atrox.utils.SpeechState
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
    var showExitDialog by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    val colors = MaterialTheme.colorScheme

    // ── Speech recognition state ──
    val isSpeechActive = uiState.speechState is SpeechState.Listening
            || uiState.speechState is SpeechState.Processing
            || uiState.speechState is SpeechState.Result

    // ── Mic permission launcher ──
    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startSpeechRecognition()
        }
    }

    val context = LocalContext.current

    val onMicClick: () -> Unit = {
        if (uiState.speechState is SpeechState.Listening) {
            viewModel.finishSpeechRecognition()
        } else if (isSpeechActive) {
            viewModel.acceptSpeechResult()
        } else {
            // Check internet connection first
            if (!viewModel.isNetworkAvailable()) {
                android.widget.Toast.makeText(context, "No Internet Connection", android.widget.Toast.LENGTH_SHORT).show()
            } else {
                // Request permission first, then start
                micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }

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

    // ══════════════════════════════════════════════════════════════════
    // ── Camera capture flow ──
    // ══════════════════════════════════════════════════════════════════

    // Holds the file path of the image the camera is about to write to
    var pendingCameraPath by remember { mutableStateOf<String?>(null) }

    // Holds the file path of the successfully captured photo (shown in preview)
    var capturedPreviewPath by remember { mutableStateOf<String?>(null) }

    // TakePicture launcher — camera writes directly to a pre-created file via FileProvider URI
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && pendingCameraPath != null) {
            // Photo was taken — show preview dialog
            capturedPreviewPath = pendingCameraPath
        } else {
            // User canceled or capture failed — clean up the empty file
            pendingCameraPath?.let { File(it).delete() }
        }
        pendingCameraPath = null
        showAttachmentSheet = false
    }

    // Camera permission launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val (path, uri) = viewModel.createCameraImageFile()
            pendingCameraPath = path
            takePictureLauncher.launch(uri)
        }
    }

    // Helper to open camera with permission check
    val onCameraClick: () -> Unit = {
        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

    // ── Exit and Save logic ──
    val handleExit = {
        if (uiState.title.isNotBlank() || uiState.body.isNotBlank()) {
            showExitDialog = true
        } else {
            onNavigateBack()
        }
    }

    val shareNote = {
        val title = uiState.title.ifBlank { "Untitled Note" }
        val body = uiState.body
        val shareContent = if (body.isNotBlank()) "$title\n\n$body" else title
        
        if (uiState.title.isNotBlank() || uiState.body.isNotBlank()) {
            val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(android.content.Intent.EXTRA_SUBJECT, title)
                putExtra(android.content.Intent.EXTRA_TEXT, shareContent)
            }
            context.startActivity(android.content.Intent.createChooser(intent, "Share Note"))
        } else {
            android.widget.Toast.makeText(context, "Nothing to share", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    BackHandler(onBack = handleExit)

    Scaffold(
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = handleExit) {
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
                            imageVector = Icons.AutoMirrored.Rounded.Undo,
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
                            imageVector = Icons.AutoMirrored.Rounded.Redo,
                            contentDescription = "Redo",
                            tint = if (uiState.redoStack.isNotEmpty()) colors.onBackground else colors.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Share
                    IconButton(onClick = shareNote) {
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
                // Voice to text
                IconButton(onClick = onMicClick) {
                    Icon(
                        imageVector = if (isSpeechActive) Icons.Rounded.Mic else Icons.Rounded.MicNone,
                        contentDescription = "Voice Input",
                        tint = if (isSpeechActive) colors.primary else colors.onSurfaceVariant,
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
                        onClick = onCameraClick
                    )
                }
            }
        }
    }

    // ── Camera Preview Dialog ──
    capturedPreviewPath?.let { path ->
        CameraPreviewDialog(
            imagePath = path,
            onInsert = {
                viewModel.addImageByPath(path)
                capturedPreviewPath = null
            },
            onDiscard = {
                // Delete the captured file and re-open camera
                File(path).delete()
                capturedPreviewPath = null
                // Re-launch camera
                val (newPath, newUri) = viewModel.createCameraImageFile()
                pendingCameraPath = newPath
                takePictureLauncher.launch(newUri)
            }
        )
    }

    // ── Speech Recognition Overlay ──
    AnimatedVisibility(
        visible = isSpeechActive || uiState.speechState is SpeechState.Error,
        enter = fadeIn() + scaleIn(initialScale = 0.9f),
        exit = fadeOut() + scaleOut(targetScale = 0.9f)
    ) {
        SpeechRecognitionOverlay(
            speechState = uiState.speechState,
            partialText = uiState.partialSpeechText,
            onAccept = { viewModel.acceptSpeechResult() },
            onDismiss = { viewModel.dismissSpeech() },
            onRetry = { viewModel.startSpeechRecognition() },
            onFinish = { viewModel.finishSpeechRecognition() }
        )
    }

    // ── Exit Dialog ──
    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("Save your note") },
            text = { Text("Do you want to save your note before exiting?") },
            confirmButton = {
                Button(onClick = {
                    viewModel.saveNote()
                    showExitDialog = false
                    onNavigateBack()
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = {
                    showExitDialog = false
                    onNavigateBack()
                }) {
                    Text("Exit")
                }
            }
        )
    }
}

// ══════════════════════════════════════════════════════════════════════
// ── Camera Preview Dialog ─────────────────────────────────────────────
// ══════════════════════════════════════════════════════════════════════

@Composable
private fun CameraPreviewDialog(
    imagePath: String,
    onInsert: () -> Unit,
    onDiscard: () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Dialog(
        onDismissRequest = { /* prevent accidental dismiss — user must pick Insert or Discard */ },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background)
        ) {
            // Full-screen image preview
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(File(imagePath))
                    .crossfade(300)
                    .build(),
                contentDescription = "Captured photo preview",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 100.dp) // leave room for action bar
            )

            // Bottom action bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(
                        color = colors.surfaceContainerHigh,
                        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
                    )
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Discard button
                OutlinedButton(
                    onClick = onDiscard,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = colors.error
                    )
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Retake", fontWeight = FontWeight.SemiBold)
                }

                // Insert button
                Button(
                    onClick = onInsert,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AddCircleOutline,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Insert", fontWeight = FontWeight.SemiBold)
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

// ══════════════════════════════════════════════════════════════════════
// ── Speech Recognition Overlay ───────────────────────────────────────
// ══════════════════════════════════════════════════════════════════════

@Composable
private fun SpeechRecognitionOverlay(
    speechState: SpeechState,
    partialText: String,
    onAccept: () -> Unit,
    onDismiss: () -> Unit,
    onRetry: () -> Unit,
    onFinish: () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background.copy(alpha = 0.95f)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
            ) {
                // ── Pulsing mic indicator ──
                when (speechState) {
                    is SpeechState.Listening -> {
                        PulsingMicIndicator(colors = colors, rmsDb = speechState.rmsDb)
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Listening…",
                            color = colors.primary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    is SpeechState.Processing -> {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .background(colors.primaryContainer, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(36.dp),
                                color = colors.primary,
                                strokeWidth = 3.dp
                            )
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Processing…",
                            color = colors.onSurfaceVariant,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    is SpeechState.Result -> {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .background(colors.primaryContainer, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Mic,
                                contentDescription = null,
                                tint = colors.primary,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Got it!",
                            color = colors.primary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    is SpeechState.Error -> {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .background(colors.errorContainer, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.MicNone,
                                contentDescription = null,
                                tint = colors.error,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = speechState.message,
                            color = colors.error,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    else -> { /* SpeechState.Idle — overlay shouldn't be visible */ }
                }

                // ── Live transcription preview ──
                if (partialText.isNotBlank()) {
                    Spacer(modifier = Modifier.height(32.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = colors.surfaceContainerHigh,
                        tonalElevation = 2.dp
                    ) {
                        Text(
                            text = partialText,
                            color = colors.onSurface,
                            fontSize = 16.sp,
                            lineHeight = 24.sp,
                            modifier = Modifier.padding(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                // ── Action buttons ──
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Dismiss / Cancel
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = colors.onSurfaceVariant
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Cancel", fontWeight = FontWeight.SemiBold)
                    }

                    if (speechState is SpeechState.Error && speechState.isRecoverable) {
                        // Retry
                        Button(
                            onClick = onRetry,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.primary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Mic,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Retry", fontWeight = FontWeight.SemiBold)
                        }
                    } else if (speechState is SpeechState.Listening) {
                        // Done listening
                        Button(
                            onClick = onFinish,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.primary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CheckCircleOutline,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Done", fontWeight = FontWeight.SemiBold)
                        }
                    } else if (speechState is SpeechState.Processing) {
                        // Disabled Done button
                        Button(
                            onClick = { },
                            enabled = false,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("Processing", fontWeight = FontWeight.SemiBold)
                        }
                    } else if (partialText.isNotBlank() || speechState is SpeechState.Result) {
                        // Accept transcribed text
                        Button(
                            onClick = onAccept,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.primary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CheckCircleOutline,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Insert", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

// ── Pulsing mic indicator with concentric rings ──────────────────────

@Composable
private fun PulsingMicIndicator(
    colors: ColorScheme,
    rmsDb: Float = 0f
) {
    val infiniteTransition = rememberInfiniteTransition(label = "mic_pulse")
    
    // Dynamic scale based on voice volume
    val normalizedRms = (rmsDb.coerceIn(-2f, 10f) + 2f) / 12f // 0f to 1f
    val dynamicScale = 1f + (normalizedRms * 0.4f) // 1f to 1.4f
    
    val animatedScale by animateFloatAsState(
        targetValue = dynamicScale,
        animationSpec = tween(100),
        label = "dynamic_scale"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    val ringAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ring_alpha"
    )

    Box(contentAlignment = Alignment.Center) {
        // Outer pulsing ring
        Box(
            modifier = Modifier
                .size(120.dp)
                .scale(pulseScale)
                .background(
                    color = colors.primary.copy(alpha = ringAlpha),
                    shape = CircleShape
                )
        )
        // Inner ring responsive to voice
        Box(
            modifier = Modifier
                .size(96.dp)
                .scale(animatedScale)
                .background(
                    color = colors.primary.copy(alpha = 0.12f),
                    shape = CircleShape
                )
        )
        // Mic icon
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(colors.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Mic,
                contentDescription = "Listening",
                tint = colors.primary,
                modifier = Modifier.size(36.dp)
            )
        }
    }
}
