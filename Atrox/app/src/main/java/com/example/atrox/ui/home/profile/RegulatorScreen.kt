package com.example.atrox.ui.home.profile

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.PersonOff
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.*
import android.provider.ContactsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import com.example.atrox.ui.onboarding.CountryCodePickerDialog
import com.example.atrox.ui.onboarding.countryList
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.atrox.ui.theme.atroxColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegulatorScreen(
    onNavigateBack: () -> Unit,
    viewModel: RegulatorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = MaterialTheme.colorScheme
    val extendedColors = MaterialTheme.atroxColors
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var showAddDialog by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }
    
    var nameInput by remember { mutableStateOf("") }
    var phoneInput by remember { mutableStateOf("") }
    var countryCode by remember { mutableStateOf("+91") }
    var showCountryDialog by remember { mutableStateOf(false) }

    val contactPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact(),
        onResult = { uri ->
            if (uri != null) {
                var phoneNumber = ""
                val cursor = context.contentResolver.query(uri, null, null, null, null)
                cursor?.use {
                    if (it.moveToFirst()) {
                        val hasPhoneIndex = it.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)
                        val idIndex = it.getColumnIndex(ContactsContract.Contacts._ID)
                        val nameIndex = it.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                        
                        if (nameIndex >= 0) {
                            nameInput = it.getString(nameIndex) ?: ""
                        }

                        if (hasPhoneIndex >= 0 && idIndex >= 0) {
                            val hasPhone = it.getInt(hasPhoneIndex) > 0
                            val id = it.getString(idIndex)
                            
                            if (hasPhone) {
                                val phonesCursor = context.contentResolver.query(
                                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                    null,
                                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                                    arrayOf(id),
                                    null
                                )
                                phonesCursor?.use { pCursor ->
                                    if (pCursor.moveToFirst()) {
                                        val numberIndex = pCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                                        if (numberIndex >= 0) {
                                            phoneNumber = pCursor.getString(numberIndex)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                val cleanedNumber = phoneNumber.filter { it.isDigit() }
                if (cleanedNumber.isNotEmpty()) {
                    phoneInput = cleanedNumber.take(10)
                }
            }
        }
    )

    Scaffold(
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Your Regulator",
                        color = colors.onBackground,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = colors.onBackground
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showInfoDialog = true }) {
                        Icon(
                            imageVector = Icons.Rounded.Info,
                            contentDescription = "Info",
                            tint = colors.onBackground
                        )
                    }
                },
                windowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp),
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.background)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // ── Active Regulator Section ─────────────────
            AnimatedVisibility(
                visible = uiState.hasRegulator,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, extendedColors.cardElevated, RoundedCornerShape(24.dp)),
                        colors = CardDefaults.cardColors(containerColor = colors.surfaceVariant),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Avatar layout
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.linearGradient(
                                                listOf(Color(0xFFD4A574), Color(0xFFA67C52))
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (uiState.name.isNotEmpty()) uiState.name.first().toString().uppercase() else "R",
                                        color = Color.White,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = uiState.name,
                                            color = colors.onBackground,
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = uiState.phone,
                                        color = colors.onSurfaceVariant,
                                        fontSize = 14.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // Status and Connected Since Info
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // Status Box
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(extendedColors.cardElevated, RoundedCornerShape(16.dp))
                                        .padding(16.dp)
                                ) {
                                    Text(
                                        text = "STATUS",
                                        color = colors.onSurfaceVariant,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = uiState.status,
                                        color = colors.onBackground,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                // Connected Since Box
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(extendedColors.cardElevated, RoundedCornerShape(16.dp))
                                        .padding(16.dp)
                                ) {
                                    Text(
                                        text = "CONNECTED SINCE",
                                        color = colors.onSurfaceVariant,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = uiState.connectedSince,
                                        color = colors.onBackground,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    // Change Regulator Button (Destructive)
                    OutlinedButton(
                        onClick = {
                            nameInput = uiState.name
                            val parts = uiState.phone.split(" ")
                            if (parts.size >= 2) {
                                countryCode = parts[0]
                                phoneInput = parts[1]
                            } else {
                                phoneInput = uiState.phone
                            }
                            showAddDialog = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.error),
                        border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(imageVector = Icons.Rounded.SwapHoriz, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Change Regulator", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    HorizontalDivider(color = colors.onSurfaceVariant.copy(alpha = 0.1f))
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            // ── No Regulator Section (Dotted border-like) ─────────────────
            AnimatedVisibility(
                visible = !uiState.hasRegulator,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = colors.primary.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(24.dp)
                        ),
                    colors = CardDefaults.cardColors(containerColor = colors.surfaceVariant.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(24.dp)
                ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(colors.onSurfaceVariant.copy(alpha = 0.08f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PersonOff,
                            contentDescription = null,
                            tint = colors.onSurfaceVariant,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "No Regulator set",
                        color = colors.onBackground,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Add someone you trust to keep you accountable and monitor your focus progress.",
                        color = colors.onSurfaceVariant,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { showAddDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
                    ) {
                        Text(text = "Add Regulator", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    // Add Regulator Dialog
    if (showAddDialog) {
        Dialog(onDismissRequest = { showAddDialog = false }) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = colors.surface,
                tonalElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = if (uiState.hasRegulator) "Change Regulator" else "Add Regulator",
                        color = colors.onBackground,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { 
                            if (it.length <= 50) {
                                nameInput = it.filter { char -> char.isLetterOrDigit() || char.isWhitespace() || char == '-' || char == '\'' }
                            }
                        },
                        label = { Text("Name") },
                        placeholder = { Text("e.g. Sarah Jenkins") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.Person,
                                contentDescription = "Regulator Name Icon",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = phoneInput,
                        onValueChange = { 
                            val digitsOnly = it.filter { char -> char.isDigit() }
                            if (digitsOnly.length <= 10) {
                                phoneInput = digitsOnly
                            }
                        },
                        label = { Text("Phone Number") },
                        placeholder = { Text("e.g. 9876543210") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        leadingIcon = {
                            Row(
                                modifier = Modifier
                                    .clickable { showCountryDialog = true }
                                    .padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val selectedCountry = countryList.find { it.code == countryCode }
                                Text(text = "${selectedCountry?.flag ?: "🌐"} $countryCode", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    TextButton(
                        onClick = { contactPickerLauncher.launch(null) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Choose from Contacts", color = colors.primary)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showAddDialog = false }) {
                            Text("Cancel", color = colors.onSurfaceVariant)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (nameInput.isNotBlank() && phoneInput.isNotBlank()) {
                                    viewModel.addRegulator(
                                        name = nameInput,
                                        phone = "$countryCode $phoneInput"
                                    )
                                    showAddDialog = false
                                } else {
                                    Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
                        ) {
                            Text("Save", color = Color.White)
                        }
                    }
                }
            }
        }
        
        if (showCountryDialog) {
            CountryCodePickerDialog(
                onDismissRequest = { showCountryDialog = false },
                onCodeSelected = { 
                    countryCode = it
                }
            )
        }
    }

    // Info Dialog
    if (showInfoDialog) {
        Dialog(onDismissRequest = { showInfoDialog = false }) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = colors.surface,
                tonalElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "What is a Regulator?",
                            color = colors.onBackground,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { showInfoDialog = false }) {
                            Icon(imageVector = Icons.Rounded.Close, contentDescription = "Close", tint = colors.onBackground)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "A Regulator is a trusted guardian or partner who helps you stay accountable to your productivity goals. They can monitor your focus times, streaks, and milestone badges.",
                        color = colors.onSurfaceVariant,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "They cannot access your private notes or credentials. Their access is strictly limited to viewing your high-level focus statistics.",
                        color = colors.onSurfaceVariant,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { showInfoDialog = false },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
                    ) {
                        Text("Got it", color = Color.White)
                    }
                }
            }
        }
    }
}
