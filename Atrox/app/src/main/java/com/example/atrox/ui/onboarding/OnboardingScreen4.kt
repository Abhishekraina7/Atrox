package com.example.atrox.ui.onboarding

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.atrox.R
import android.provider.ContactsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import kotlinx.coroutines.delay

data class CountryInfo(val code: String, val name: String, val flag: String)
val countryList = listOf(
    CountryInfo("+91", "India", "🇮🇳"),
    CountryInfo("+1", "United States", "🇺🇸"),
    CountryInfo("+44", "United Kingdom", "🇬🇧"),
    CountryInfo("+61", "Australia", "🇦🇺"),
    CountryInfo("+81", "Japan", "🇯🇵"),
    CountryInfo("+49", "Germany", "🇩🇪"),
    CountryInfo("+33", "France", "🇫🇷"),
    CountryInfo("+86", "China", "🇨🇳"),
    CountryInfo("+55", "Brazil", "🇧🇷"),
    CountryInfo("+27", "South Africa", "🇿🇦"),
    CountryInfo("+7", "Russia", "🇷🇺"),
    CountryInfo("+39", "Italy", "🇮🇹"),
    CountryInfo("+34", "Spain", "🇪🇸"),
    CountryInfo("+52", "Mexico", "🇲🇽"),
    CountryInfo("+62", "Indonesia", "🇮🇩"),
    CountryInfo("+90", "Turkey", "🇹🇷"),
    CountryInfo("+82", "South Korea", "🇰🇷"),
    CountryInfo("+966", "Saudi Arabia", "🇸🇦"),
    CountryInfo("+971", "United Arab Emirates", "🇦🇪"),
    CountryInfo("+234", "Nigeria", "🇳🇬")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen4(
    viewModel: Onboarding4ViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToDashboard: () -> Unit,
    onNavigateToSkip: () -> Unit
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val regulatorName by viewModel.regulatorName.collectAsState()
    val countryCode by viewModel.countryCode.collectAsState()
    val context = LocalContext.current
    var showCountryDialog by remember { mutableStateOf(false) }
    
    val contactPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact(),
        onResult = { uri ->
            if (uri != null) {
                // Extract phone number from the returned URI
                var phoneNumber = ""
                val cursor = context.contentResolver.query(uri, null, null, null, null)
                cursor?.use {
                    if (it.moveToFirst()) {
                        val hasPhoneIndex = it.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)
                        val idIndex = it.getColumnIndex(ContactsContract.Contacts._ID)
                        
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
                // Strip non-digits and update ViewModel
                val cleanedNumber = phoneNumber.replace(Regex("[^0-9+]"), "")
                if (cleanedNumber.isNotEmpty()) {
                    viewModel.onSearchQueryChanged(cleanedNumber)
                }
            }
        }
    )

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                contactPickerLauncher.launch(null)
            } else {
                android.widget.Toast.makeText(context, "Contact permission is required for Regulator features", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    )

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is Onboarding4Event.NavigateBack -> onNavigateBack()
                is Onboarding4Event.NavigateToDashboard -> onNavigateToDashboard()
                is Onboarding4Event.NavigateToSkip -> onNavigateToSkip()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(top = 24.dp, bottom = 32.dp)
    ) {
        // --- 2. Top Bar ---
        Box(
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.ArrowBack,
                contentDescription = stringResource(R.string.onboarding_back),
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(24.dp)
                    .clickable { viewModel.onBackClicked() }
            )

            Text(
                text = stringResource(R.string.onboarding_add_regulator_title),
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // --- 3. Trust Indicator Strip ---
        GuardianTrustStrip()

        Spacer(modifier = Modifier.height(24.dp))

        // --- 4. Description Section ---
        Text(
            text = stringResource(R.string.onboarding_regulator_description),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 15.sp,
            lineHeight = 22.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)
        )

        // --- 4.5 Regulator Name field ---
        Text(
            text = stringResource(R.string.onboarding_name_text_field_label),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = regulatorName,
            onValueChange = { viewModel.onRegulatorNameChanged(it) },
            placeholder = { Text("Guardian's Name", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Rounded.Person,
                    contentDescription = "Regulator Name Icon",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
            ),
            singleLine = true
        )

        // --- 5. Phone field ---
        Text(
            text = stringResource(R.string.onboarding_guardian_phone_label),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.onSearchQueryChanged(it) },
            placeholder = { Text(stringResource(R.string.onboarding_guardian_phone_placeholder), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)) },
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
            ),
            singleLine = true
        )

        if (showCountryDialog) {
            CountryCodePickerDialog(
                onDismissRequest = { showCountryDialog = false },
                onCodeSelected = { 
                    viewModel.onCountryCodeChanged(it)
                }
            )
        }

        // --- 6. Quick Filters ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FilterChip(stringResource(R.string.onboarding_contacts_filter), onClick = { permissionLauncher.launch(android.Manifest.permission.READ_CONTACTS) })
        }

        Spacer(modifier = Modifier.weight(1f, fill = false))
        Spacer(modifier = Modifier.height(40.dp))

        val isPhoneValid = searchQuery.replace(Regex("[^0-9]"), "").length >= 10
        val isFormValid = isPhoneValid && regulatorName.isNotBlank()
        val savePermissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = { isGranted ->
                if (isGranted) {
                    viewModel.onContinueClicked()
                } else {
                    android.widget.Toast.makeText(context, "Contact permission is required to save Regulator", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        )

        Button(
            onClick = {
                if (androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_CONTACTS) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    viewModel.onContinueClicked()
                } else {
                    savePermissionLauncher.launch(android.Manifest.permission.READ_CONTACTS)
                }
            },
            enabled = isFormValid,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(text = stringResource(R.string.onboarding_complete_setup), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Rounded.CheckCircle,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.onboarding_skip_for_now),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { viewModel.onSkipClicked() }
                .padding(8.dp)
        )
    }
}

@Composable
fun FilterChip(text: String, onClick: () -> Unit = {}) {
    Box(
        modifier = Modifier
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 13.sp
        )
    }
}

@Composable
fun CountryCodePickerDialog(
    onDismissRequest: () -> Unit,
    onCodeSelected: (String) -> Unit
) {
    var dialogSearchQuery by remember { mutableStateOf("") }
    val filteredCountries = remember(dialogSearchQuery) {
        countryList.filter { it.name.contains(dialogSearchQuery, ignoreCase = true) || it.code.contains(dialogSearchQuery) }
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            OutlinedTextField(
                value = dialogSearchQuery,
                onValueChange = { dialogSearchQuery = it },
                placeholder = { Text("Search country...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        },
        text = {
            LazyColumn {
                items(filteredCountries) { country ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onCodeSelected(country.code)
                                onDismissRequest()
                            }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = country.flag, fontSize = 24.sp, modifier = Modifier.padding(end = 16.dp))
                        Column {
                            Text(text = country.name, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                            Text(text = country.code, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Close")
            }
        }
    )
}

private data class GuardianFeature(val emoji: String, val title: String, val subtitle: String, val accentColor: Color)

private val guardianFeatures = listOf(
    GuardianFeature("📊", "Progress Reports", "Weekly updates on focus habits", Color(0xFF42A5F5)),
    GuardianFeature("🔔", "Smart Alerts", "Notified when goals are missed", Color(0xFFFF9800)),
    GuardianFeature("🛡️", "Accountability", "Trusted oversight of your journey", Color(0xFF6C63FF)),
)

@Composable
fun GuardianTrustStrip() {
    val animations = remember { guardianFeatures.map { Animatable(0f) } }
    LaunchedEffect(Unit) {
        animations.forEachIndexed { index, anim ->
            delay(index * 200L)
            anim.animateTo(1f, animationSpec = tween(500, easing = EaseInOutCubic))
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        guardianFeatures.forEachIndexed { index, feature ->
            val progress = animations[index].value
            GuardianFeatureCard(
                feature = feature,
                modifier = Modifier
                    .weight(1f)
                    .graphicsLayer {
                        alpha = progress
                        translationY = (1f - progress) * 30f
                    }
            )
        }
    }
}

@Composable
private fun GuardianFeatureCard(feature: GuardianFeature, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
            .padding(vertical = 16.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(feature.accentColor.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = feature.emoji, fontSize = 20.sp)
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = feature.title,
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = feature.subtitle,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 10.sp,
            textAlign = TextAlign.Center,
            lineHeight = 14.sp
        )
    }
}
