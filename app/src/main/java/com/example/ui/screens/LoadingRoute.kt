package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.ScanningAnimation

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun LoadingScreen(
    message: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("loading_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // High-fidelity custom radar/scanner animation
            ScanningAnimation(
                size = 180.dp
            )

            Spacer(modifier = Modifier.height(36.dp))

            // AI Status subtitle
            Text(
                text = "SHOPTOOL AI ENGINE",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Animated fading dynamic status message
            AnimatedContent(
                targetState = message,
                transitionSpec = {
                    fadeIn() + slideInVertically { it / 2 } with fadeOut() + slideOutVertically { -it / 2 }
                },
                label = "LoadingMessage"
            ) { targetMessage ->
                Text(
                    text = targetMessage,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.testTag("loading_status_text")
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Identifying embedded products, aggregating details, and compiling comparison stores in real-time.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp),
                lineHeight = 16.sp
            )
        }
    }
}
