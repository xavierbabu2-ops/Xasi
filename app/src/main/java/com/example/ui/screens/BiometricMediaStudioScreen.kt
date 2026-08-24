package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SovereignBiometricMediaManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BiometricMediaStudioScreen(onBack: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val biometricManager = remember { SovereignBiometricMediaManager(context) }

    val faceStatus by biometricManager.faceDetectionStatus.collectAsState()
    val voiceStatus by biometricManager.voiceDetectionStatus.collectAsState()
    val faceCount by biometricManager.detectedFacesCount.collectAsState()
    val voiceMatch by biometricManager.lastDetectedVoiceMatch.collectAsState()
    val ownerPhoto by biometricManager.ownerPhotoUri.collectAsState()
    val customImages by biometricManager.customAppImages.collectAsState()

    var selectedKeyToReplace by remember { mutableStateOf("header_banner") }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            if (selectedKeyToReplace == "owner_profile") {
                biometricManager.updateOwnerPhoto(it.toString())
            } else {
                biometricManager.setCustomAppImage(selectedKeyToReplace, it.toString())
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("xasi Biometric & Media Sovereign Studio", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0B132B))
            )
        },
        containerColor = Color(0xFF0B132B)
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Owner Photo & Custom Image Manager
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1C2541)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "📷 Owner Photo & App Image Customizer",
                            color = Color(0xFFFFB703),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Xavier Babu: Upload your owner photo or replace any embedded asset inside xasi instantly.",
                            color = Color(0xFF8D99AE),
                            fontSize = 12.sp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(70.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF3A506B))
                                    .border(2.dp, Color(0xFF00E5FF), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                if (ownerPhoto != null) {
                                    Text("Photo Set", color = Color.White, fontSize = 10.sp)
                                } else {
                                    Icon(Icons.Default.Person, contentDescription = "Owner", tint = Color.White, modifier = Modifier.size(36.dp))
                                }
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = {
                                        selectedKeyToReplace = "owner_profile"
                                        imagePickerLauncher.launch("image/*")
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF))
                                ) {
                                    Text("Change Owner Photo", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }

                                Button(
                                    onClick = {
                                        selectedKeyToReplace = "header_banner"
                                        imagePickerLauncher.launch("image/*")
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB703))
                                ) {
                                    Text("Replace App Asset Image", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            // 2. Face Finding & Detecting Studio
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1C2541)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "👁️ Face Finding & Detecting Engine",
                            color = Color(0xFF00E5FF),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = faceStatus,
                            color = Color.White,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Detected Owner Faces: $faceCount",
                            color = Color(0xFFFFB703),
                            fontSize = 12.sp
                        )

                        Button(
                            onClick = { biometricManager.performFaceDetectionScan() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3A506B)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Face, contentDescription = "Scan Face", tint = Color(0xFF00E5FF))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Run Live Face Finding & Recognition", color = Color.White)
                        }
                    }
                }
            }

            // 3. Voice Finding & Detecting Studio
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1C2541)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "🎙️ Voice Finding & Voice Detecting Studio",
                            color = Color(0xFF00E5FF),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = voiceStatus,
                            color = Color.White,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Last Voiceprint Match: $voiceMatch",
                            color = Color(0xFFFFB703),
                            fontSize = 12.sp
                        )

                        Button(
                            onClick = { biometricManager.performVoiceDetectionScan("வணக்கம் சேவியர்பாபு (Vanakkam Xavier Babu)") },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3A506B)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Mic, contentDescription = "Scan Voice", tint = Color(0xFFFFB703))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Run Live Voice Finding & Biometric Match", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}
