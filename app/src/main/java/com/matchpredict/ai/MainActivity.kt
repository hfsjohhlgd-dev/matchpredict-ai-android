
package com.matchpredict.ai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Dark = Color(0xFF101820)
private val CardDark = Color(0xFF1C2935)
private val Green = Color(0xFF00D68F)
private val White = Color.White
private val Muted = Color(0xFF9EADB9)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                MatchPredictApp()
            }
        }
    }
}

@Composable
fun MatchPredictApp() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Dark
    ) {
        LazyColumn(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    "MATCHPREDICT AI",
                    color = Green,
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Intelligent Football Predictions",
                    color = Muted,
                    fontSize = 13.sp
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Today's Matches", color = White, fontSize = 20.sp)
                    Text("AI ENGINE", color = Green, fontSize = 12.sp)
                }
            }

            item {
                MatchCard(
                    league = "Premier League",
                    home = "Arsenal",
                    away = "Chelsea",
                    prediction = "Over 2.5 Goals",
                    confidence = "Demo: 72%"
                )
            }

            item {
                MatchCard(
                    league = "La Liga",
                    home = "Real Madrid",
                    away = "Barcelona",
                    prediction = "Both Teams To Score",
                    confidence = "Demo: 68%"
                )
            }

            item {
                MatchCard(
                    league = "Botola Pro",
                    home = "Wydad",
                    away = "Raja",
                    prediction = "Under 3.5 Goals",
                    confidence = "Demo: 65%"
                )
            }

            item {
                Text(
                    "Prediction engine is currently in demo mode.",
                    color = Muted,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun MatchCard(
    league: String,
    home: String,
    away: String,
    prediction: String,
    confidence: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(league, color = Muted, fontSize = 12.sp)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(home, color = White, fontWeight = FontWeight.Bold)
                Text("VS", color = Green, fontWeight = FontWeight.Bold)
                Text(away, color = White, fontWeight = FontWeight.Bold)
            }

            HorizontalDivider(color = Muted.copy(alpha = 0.3f))

            Text("AI Analysis", color = Green, fontWeight = FontWeight.Bold)
            Text(prediction, color = White, fontSize = 16.sp)
            Text(confidence, color = Muted, fontSize = 13.sp)
        }
    }
}
