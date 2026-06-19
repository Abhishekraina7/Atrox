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
import androidx.compose.material.icons.rounded.Create
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.material3.BottomAppBarDefaults.windowInsets
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

private val ColorBackground = Color(0xFF0F1016)
private val ColorCard = Color(0xFF1E2235)
private val ColorSearchBox = Color(0xFF181C2C)
private val ColorAccent = Color(0xFF6C63FF)
private val ColorTextPrimary = Color.White
private val ColorTextSecondary = Color(0xFF8B92A5)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    onAddNote: () -> Unit = {},
    viewModel: NotesViewModel = hiltViewModel()
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val notes by viewModel.notes.collectAsState()
    var showMenu by remember { mutableStateOf(false) }
    
    val categories = NoteCategory.values().toList()
    
    val filteredNotes = notes.filter { 
        (selectedCategory == NoteCategory.ALL || it.category == selectedCategory) &&
        (searchQuery.isBlank() || it.title.contains(searchQuery, ignoreCase = true) || it.content.contains(searchQuery, ignoreCase = true))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Notes",
                        color = ColorTextPrimary,
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
                                tint = ColorTextPrimary
                            )
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            modifier = Modifier.background(ColorCard)
                        ) {
                            DropdownMenuItem(
                                text = { Text("ListView", color = ColorTextPrimary) },
                                onClick = { showMenu = false }
                            )
                            DropdownMenuItem(
                                text = { Text("Sort by Time created", color = ColorTextPrimary) },
                                onClick = { showMenu = false }
                            )
                            DropdownMenuItem(
                                text = { Text("Sort by the time edited", color = ColorTextPrimary) },
                                onClick = { showMenu = false }
                            )
                        }
                    }
                },
                windowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
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
                    .background(ColorSearchBox, RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = "Search",
                    tint = ColorTextSecondary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                BasicTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    textStyle = TextStyle(color = ColorTextPrimary, fontSize = 16.sp),
                    cursorBrush = SolidColor(ColorAccent),
                    decorationBox = { innerTextField ->
                        if (searchQuery.isEmpty()) {
                            Text("Search entries...", color = ColorTextSecondary, fontSize = 16.sp)
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
                    val bgColor = if (isSelected) ColorAccent else ColorSearchBox
                    val textColor = if (isSelected) Color.White else ColorTextSecondary
                    
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
            
            // Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 100.dp), // Extra padding for bottom nav
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(filteredNotes, span = { note ->
                    if (note.isSpanning) GridItemSpan(2) else GridItemSpan(1)
                }) { note ->
                    if (note.isSpanning) {
                        SpanningNoteCard(note)
                    } else {
                        NoteCard(note)
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
                    .shadow(16.dp, RoundedCornerShape(16.dp), ambientColor = ColorAccent, spotColor = ColorAccent)
                    .clip(RoundedCornerShape(16.dp))
                    .background(ColorAccent)
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .background(ColorCard, RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = note.title,
                color = ColorTextPrimary,
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
                    tint = ColorAccent,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = note.content,
            color = ColorTextSecondary,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        Text(
            text = note.timestamp,
            color = ColorTextSecondary.copy(alpha = 0.5f),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
        )
    }
}

@Composable
fun SpanningNoteCard(note: NoteItem) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(ColorCard, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
    ) {
        // Image or gradient placeholder
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF3B5B78), // Light bluish slate
                            ColorCard
                        )
                    )
                )
        )
        
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = note.title,
                color = ColorTextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = note.content,
                color = ColorTextSecondary,
                fontSize = 14.sp,
                lineHeight = 20.sp,
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = note.timestamp,
                color = ColorTextSecondary.copy(alpha = 0.5f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )
        }
    }
}
