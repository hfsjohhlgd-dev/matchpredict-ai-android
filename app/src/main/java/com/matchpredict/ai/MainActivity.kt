package com.matchpredict.ai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import java.net.HttpURLConnection

private val Dark = Color(0xFF101820)
private val CardDark = Color(0xFF1C2935)
private val Green = Color(0xFF00D68F)
private val White = Color.White
private val Muted = Color(0xFF9EADB9)

private const val FIXTURES_URL =
    "https://raw.githubusercontent.com/hfsjohhlgd-dev/matchpredict-ai-android/main/data/fixtures.json"

data class FootballMatch(
    val league: String,
    val home: String,
    val away: String,
    val date: String,
    val status: String,
    val score: String
)

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
    var matches by remember { mutableStateOf<List<FootballMatch>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        loading = true
        error = null

        try {
            matches = withContext(Dispatchers.IO) {
                loadMatches()
            }
        } catch (e: Exception) {
            error = e.message ?: "Could not load matches"
        }

        loading = false
    }

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
                    "Real Football Fixtures",
                    color = Muted,
                    fontSize = 13.sp
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Today's & Upcoming Matches", color = White, fontSize = 18.sp)
                    Text("LIVE DATA", color = Green, fontSize = 12.sp)
                }
            }

            if (loading) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(30.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Green)
                    }
                }
            }

            if (error != null) {
                item {
                    Text(
                        "Loading error: $error",
                        color = Color.Red,
                        fontSize = 13.sp
                    )
                }
            }

            if (!loading && error == null && matches.isEmpty()) {
                item {
                    Text(
                        "No matches found in the current data file.",
                        color = Muted
                    )
                }
            }

            items(matches) { match ->
                MatchCard(match)
            }

            item {
                Text(
                    "Fixtures are updated automatically by GitHub Actions.",
                    color = Muted,
                    fontSize = 12.sp
                )
            }
        }
    }
}

fun loadMatches(): List<FootballMatch> {
    val connection = URL(FIXTURES_URL).openConnection() as HttpURLConnection

    try {
        connection.requestMethod = "GET"
        connection.connectTimeout = 15000
        connection.readTimeout = 15000

        val responseCode = connection.responseCode
        if (responseCode != 200) {
            throw Exception("Server response: $responseCode")
        }

        val jsonText = connection.inputStream.bufferedReader().use {
            it.readText()
        }

        val root = JSONObject(jsonText)
        val response = root.optJSONArray("response")
            ?: throw Exception("Invalid fixtures data")

        val result = mutableListOf<FootballMatch>()

        for (i in 0 until response.length()) {
            val item = response.getJSONObject(i)
            val league = item.optJSONObject("league")
            val teams = item.optJSONObject("teams")
            val fixture = item.optJSONObject("fixture")
            val goals = item.optJSONObject("goals")

            val home = teams?.optJSONObject("home")?.optString("name", "Home") ?: "Home"
            val away = teams?.optJSONObject("away")?.optString("name", "Away") ?: "Away"
            val leagueName = league?.optString("name", "Football") ?: "Football"

            val rawDate = fixture?.optString("date", "") ?: ""
            val date = rawDate.replace("T", " ").take(16)

            val status = fixture
                ?.optJSONObject("status")
                ?.optString("long", "Scheduled") ?: "Scheduled"

            val homeGoals = goals?.opt("home")
                ?.takeUnless { it == JSONObject.NULL }?.toString() ?: "-"

            val awayGoals = goals?.opt("away")
                ?.takeUnless { it == JSONObject.NULL }?.toString() ?: "-"

            result.add(
                FootballMatch(
                    league = leagueName,
                    home = home,
                    away = away,
                    date = date,
                    status = status,
                    score = "$homeGoals - $awayGoals"
                )
            )
        }

        return result
    } finally {
        connection.disconnect()
    }
}

@Composable
fun MatchCard(match: FootballMatch) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(match.league, color = Muted, fontSize = 12.sp)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    match.home,
                    color = White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text("VS", color = Green, fontWeight = FontWeight.Bold)
                Text(
                    match.away,
                    color = White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
            }

            HorizontalDivider(color = Muted.copy(alpha = 0.3f))

            Text("Date: ${match.date}", color = Muted, fontSize = 12.sp)
            Text("Status: ${match.status}", color = Green, fontSize = 13.sp)
            Text("Score: ${match.score}", color = White, fontSize = 14.sp)
        }
    }
}
