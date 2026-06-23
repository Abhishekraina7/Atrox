package com.example.atrox.ui.home.notes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    onAddNote: () -> Unit = {},
    onNoteClick: (String) -> Unit = {},
    viewModel: NotesViewModel = hiltViewModel()
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val notes by viewModel.notes.collectAsState()
    var showMenu by remember { mutableStateOf(false) }
    var isListView by remember { mutableStateOf(false) }

    val categories = NoteCategory.values().toList()

    val filteredNotes = notes.filter {
        (selectedCategory == NoteCategory.ALL || it.category == selectedCategory) &&
        (searchQuery.isBlank() || it.title.contains(searchQuery, ignoreCase = true) || it.content.contains(searchQuery, ignoreCase = true))
    }

    val colors = MaterialTheme.colorScheme

    Scaffold(
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Notes",
                        color = colors.onBackground,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                },
                actions = {
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                imageVector = Icons.Rounded.MoreVert,
                                contentDescription = "More Option",
                                tint = colors.onBackground
                            )
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            modifier = Modifier.background(colors.surfaceVariant)
                        ) {
                            DropdownMenuItem(
                                text = { Text(if (isListView) "GridView" else "ListView", color = colors.onBackground) },
                                onClick = { 
                                    isListView = !isListView
                                    showMenu = false 
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Sort by Time created", color = colors.onBackground) },
                                onClick = { showMenu = false }
                            )
                            DropdownMenuItem(
                                text = { Text("Sort by the time edited", color = colors.onBackground) },
                                onClick = { showMenu = false }
                            )
                        }
                    }
                },
                windowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.background,
                    titleContentColor = colors.onBackground
                )
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Search Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .height(52.dp)
                        .background(colors.surface, RoundedCornerShape(16.dp))
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = "Search",
                        tint = colors.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.updateSearchQuery(it) },
                        textStyle = TextStyle(color = colors.onBackground, fontSize = 16.sp),
                        cursorBrush = SolidColor(colors.primary),
                        decorationBox = { innerTextField ->
                            if (searchQuery.isEmpty()) {
                                Text("Search entries...", color = colors.onSurfaceVariant, fontSize = 16.sp)
                            }
                            innerTextField()
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Chips
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(categories) { category ->
                        val isSelected = selectedCategory == category
                        val bgColor = if (isSelected) colors.primary else colors.surface
                        val textColor = if (isSelected) Color.White else colors.onSurfaceVariant

                        Box(
                            modifier = Modifier
                                .background(bgColor, RoundedCornerShape(20.dp))
                                .clip(RoundedCornerShape(20.dp))
                                .clickable { viewModel.selectCategory(category) }
                                .padding(horizontal = 20.dp, vertical = 10.dp)
                        ) {
                            val text = when (category) {
                                NoteCategory.ALL -> "All"
                                NoteCategory.PERSONAL -> "Personal"
                                NoteCategory.JOURNAL -> "Journal"
                                NoteCategory.WORK -> "Work"
                            }
                            Text(
                                text = text,
                                color = textColor,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Grid or List
                LazyVerticalGrid(
                    columns = if (isListView) GridCells.Fixed(1) else GridCells.Fixed(2),
                    contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 100.dp), // Extra padding for bottom nav
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(filteredNotes, span = { note ->
                        if (isListView) GridItemSpan(1)
                        else if (note.isSpanning) GridItemSpan(2)
                        else GridItemSpan(1)
                    }) { note ->
                        Box(modifier = Modifier.clickable { onNoteClick(note.id) }) {
                            if (note.isSpanning) {
                                SpanningNoteCard(note)
                            } else {
                                NoteCard(note)
                            }
                        }
                    }
                }
            }

            // Floating Action Button
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(innerPadding)
                    .padding(bottom = 24.dp, end = 20.dp)
                    .size(56.dp)
                    .shadow(16.dp, RoundedCornerShape(16.dp), ambientColor = colors.primary, spotColor = colors.primary)
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.primary)
                    .clickable { onAddNote() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = "Create Note",
                    tint = Color.White,
                    modifier = Modifier.size(30.dp)
                )
            }
        }
    }
}

@Composable
fun NoteCard(note: NoteItem) {
    val colors = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .background(colors.surfaceVariant, RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = note.title,
                color = colors.onBackground,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            if (note.hasAudio) {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Rounded.Mic,
                    contentDescription = "Audio note",
                    tint = colors.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = note.content,
            color = colors.onSurfaceVariant,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = note.timestamp,
            color = colors.onSurfaceVariant.copy(alpha = 0.5f),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
        )
    }
}

@Composable
fun SpanningNoteCard(note: NoteItem) {
    val colors = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surfaceVariant, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
    ) {
        // Gradient placeholder using primary color matching theme
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            colors.primary.copy(alpha = 0.4f),
                            colors.surfaceVariant
                        )
                    )
                )
        )

        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = note.title,
                color = colors.onBackground,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = note.content,
                color = colors.onSurfaceVariant,
                fontSize = 14.sp,
                lineHeight = 20.sp,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = note.timestamp,
                color = colors.onSurfaceVariant.copy(alpha = 0.5f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )
        }
    }
}
