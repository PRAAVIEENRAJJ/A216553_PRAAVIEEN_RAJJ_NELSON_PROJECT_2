package com.example.a216553_praavieenrajj_mrnelson_project_2.Screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.a216553_praavieenrajj_mrnelson_project_2.Data.UserProfile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(profile: UserProfile, onBack: () -> Unit) {

    // SDG Green Theme
    val darkGreen = Color(0xFF1B5E20)
    val lightGreen = Color(0xFF66BB6A)
    val softGreen = Color(0xFFE8F5E9)

    Scaffold(

        // Top Navigation Bar
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "My Profile",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },

                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },

                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = darkGreen
                )
            )
        }

    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            darkGreen,
                            lightGreen
                        )
                    )
                )
                .padding(paddingValues)
                .padding(20.dp)
        ) {

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(12.dp)
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    // Profile Icon
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .clip(CircleShape)
                            .background(softGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Profile Icon",
                            tint = darkGreen,
                            modifier = Modifier.size(90.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = profile.fullName,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = darkGreen
                    )

                    Spacer(modifier = Modifier.height(30.dp))

                    ProfileItem("Username", profile.username, darkGreen, softGreen)
                    ProfileItem("Email", profile.email, darkGreen, softGreen)
                    ProfileItem("Phone", profile.phone, darkGreen, softGreen)

                    Spacer(modifier = Modifier.height(30.dp))

                    Button(
                        onClick = onBack,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = darkGreen
                        )
                    ) {
                        Text(
                            text = "Back to Home",
                            fontSize = 18.sp,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileItem(
    label: String,
    value: String,
    darkGreen: Color,
    softGreen: Color
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = softGreen
        )
    ) {

        Column(
            modifier = Modifier.padding(18.dp)
        ) {

            Text(
                text = label,
                fontSize = 14.sp,
                color = darkGreen,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                text = value,
                fontSize = 18.sp,
                color = Color.Black,
                fontWeight = FontWeight.Medium
            )
        }
    }
}