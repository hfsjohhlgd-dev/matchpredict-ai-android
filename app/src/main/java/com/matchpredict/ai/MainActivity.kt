
package com.matchpredict.ai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import java.net.HttpURLConnection
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private val Dark = Color(0xFF07110E)
private val CardDark = Color(0xFF12221C)
private val Green = Color(0xFF00E59B)
private val White = Color(0xFFF5FFF9)
private val Muted = Color(0xFF91A69B)
private val BorderGreen = Color(0xFF244536)

private val MoroccoZone = ZoneId.of("Africa/Casablanca")

private const val FIXTURES_URL =
    "https://raw.githubusercontent.com/hfsjohhlgd-dev/matchpredict-ai-android/main/data/fixtures.json"

data class FootballMatch(
    val id: Long,
    val league: String,
    val home: String,
    val away: String,
    val date: String,
    val timestamp: Long,
    val status: String,
    val statusShort: String,
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

    var matches by remember {
        mutableStateOf<List<FootballMatch>>(emptyList())
    }

    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var selectedTab by remember { mutableStateOf("Today") }
    var refreshKey by remember { mutableIntStateOf(0) }

    var currentTime by remember {
        mutableStateOf(ZonedDateTime.now(MoroccoZone))
    }

    // تحديث الساعة كل 30 ثانية
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = ZonedDateTime.now(MoroccoZone)
            delay(30_000)
        }
    }

    // تحميل المباريات
    LaunchedEffect(refreshKey) {
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

    val timeText = currentTime.format(
        DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH)
    )

    val dateText = currentTime.format(
        DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy", Locale.ENGLISH)
    )

    Scaffold(
        containerColor = Dark,
        bottomBar = {
            NavigationBar(
                containerColor = CardDark,
                contentColor = White
            ) {
                val tabs = listOf(
                    Triple("Today", "⚽", "Today"),
                    Triple("Predictions", "◈", "Predict"),
                    Triple("Statistics", "▥", "Stats"),
                    Triple("Pro", "★", "Pro")
                )

                tabs.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab.first,
                        onClick = { selectedTab = tab.first },
                        icon = {
                            Text(
                                tab.second,
                                fontSize = 21.sp,
                                color = if (selectedTab == tab.first)
                                    Green else Muted
                            )
                        },
                        label = {
                            Text(
                                tab.third,
                                fontSize = 10.sp
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Green,
                            selectedTextColor = Green,
                            unselectedIconColor = Muted,
                            unselectedTextColor = Muted,
                            indicatorColor = Green.copy(alpha = 0.12f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF10251B),
                            Dark,
                            Dark
                        )
                    )
                )
                .padding(innerPadding)
        ) {

            when (selectedTab) {

                "Today" -> {

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            horizontal = 16.dp,
                            vertical = 18.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {

                        item {
                            HeaderSection(
                                time = timeText,
                                date = dateText,
                                onRefresh = {
                                    refreshKey++
                                }
                            )
                        }

                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        "Today's Matches",
                                        color = White,
                                        fontSize = 19.sp,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Text(
                                        "${matches.size} matches available",
                                        color = Muted,
                                        fontSize = 12.sp
                                    )
                                }

                                Surface(
                                    color = Green.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(30.dp)
                                ) {
                                    Text(
                                        "● LIVE DATA",
                                        color = Green,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(
                                            horizontal = 12.dp,
                                            vertical = 8.dp
                                        )
                                    )
                                }
                            }
                        }

                        if (loading) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(35.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        color = Green
                                    )
                                }
                            }
                        }

                        if (error != null) {
                            item {
                                InfoCard(
                                    title = "Connection error",
                                    message = error ?: ""
                                )
                            }
                        }

                        if (!loading && error == null && matches.isEmpty()) {
                            item {
                                InfoCard(
                                    title = "No matches found",
                                    message = "Try refreshing the fixtures."
                                )
                            }
                        }

                        items(
                            items = matches,
                            key = { it.id }
                        ) { match ->
                            MatchCard(match)
                        }

                        item {
                            Text(
                                "Data provided by football fixtures API.",
                                color = Muted,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(
                                    top = 10.dp,
                                    bottom = 15.dp
                                )
                            )
                        }
                    }
                }

                "Predictions" -> {
                    PlaceholderScreen(
                        icon = "◈",
                        title = "AI Predictions",
                        description = "Match predictions will appear here after connecting the predictions data."
                    )
                }

                "Statistics" -> {
                    PlaceholderScreen(
                        icon = "▥",
                        title = "Football Statistics",
                        description = "Goals, corners, cards and team statistics will be displayed here."
                    )
                }

                "Pro" -> {
                    PlaceholderScreen(
                        icon = "★",
                        title = "MatchPredict Pro",
                        description = "Advanced analysis and premium features are coming soon."
                    )
                }
            }
        }
    }
}

@Composable
fun HeaderSection(
    time: String,
    date: String,
    onRefresh: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(0xFF173D2B),
                        Color(0xFF10251B)
                    )
                ),
                RoundedCornerShape(24.dp)
            )
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "MATCHPREDICT",
                    color = White,
                    fontSize = 21.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                Text(
                    "AI FOOTBALL ANALYTICS",
                    color = Green,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
            }

            Text(
                "⚽",
                fontSize = 34.sp
            )
        }

        HorizontalDivider(
            color = Green.copy(alpha = 0.25f)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "MOROCCO TIME 🇲🇦",
                    color = Muted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    time,
                    color = Green,
                    fontSize = 38.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                Text(
                    date,
                    color = White,
                    fontSize = 11.sp
                )
            }

            Button(
                onClick = onRefresh,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Green,
                    contentColor = Dark
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    "↻ Refresh",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun MatchCard(match: FootballMatch) {

    val isLive = match.statusShort in listOf(
        "1H", "HT", "2H", "ET", "P", "LIVE"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardDark
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            BorderGreen
        )
    ) {

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(13.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    match.league,
                    color = Muted,
                    fontSize = 11.sp,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Surface(
                    color = if (isLive)
                        Green.copy(alpha = 0.15f)
                    else
                        Color(0xFF25352D),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        if (isLive) "● LIVE" else match.status,
                        color = if (isLive) Green else Muted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(
                            horizontal = 10.dp,
                            vertical = 6.dp
                        )
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    match.home,
                    color = White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    modifier = Modifier.weight(1f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 10.dp)
                ) {
                    Text(
                        if (isLive || match.score != "- -") match.score else "VS",
                        color = Green,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Text(
                        match.date,
                        color = Muted,
                        fontSize = 10.sp
                    )
                }

                Text(
                    match.away,
                    color = White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    modifier = Modifier.weight(1f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            HorizontalDivider(
                color = BorderGreen
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "🇲🇦 Morocco Time",
                    color = Muted,
                    fontSize = 10.sp
                )

                Text(
                    if (isLive) "Match in progress" else match.status,
                    color = Green,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
fun PlaceholderScreen(
    icon: String,
    title: String,
    description: String
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(22.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    CardDark,
                    RoundedCornerShape(24.dp)
                )
                .padding(25.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            Text(
                icon,
                color = Green,
                fontSize = 48.sp
            )

            Text(
                title,
                color = White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                description,
                color = Muted,
                fontSize = 13.sp,
                lineHeight = 21.sp
            )

            Text(
                "COMING SOON",
                color = Green,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun InfoCard(
    title: String,
    message: String
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = CardDark
        ),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                title,
                color = Green,
                fontWeight = FontWeight.Bold
            )

            Text(
                message,
                color = Muted,
                fontSize = 13.sp
            )
        }
    }
}

fun loadMatches(): List<FootballMatch> {

    val connection =
        URL(FIXTURES_URL).openConnection() as HttpURLConnection

    try {
        connection.requestMethod = "GET"
        connection.connectTimeout = 15000
        connection.readTimeout = 15000

        connection.setRequestProperty(
            "Cache-Control",
            "no-cache"
        )

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

            val home =
                teams?.optJSONObject("home")
                    ?.optString("name", "Home") ?: "Home"

            val away =
                teams?.optJSONObject("away")
                    ?.optString("name", "Away") ?: "Away"

            val leagueName =
                league?.optString("name", "Football") ?: "Football"

            val rawDate =
                fixture?.optString("date", "") ?: ""

            val timestamp =
                fixture?.optLong("timestamp", 0L) ?: 0L

            val localDate = try {
                OffsetDateTime.parse(rawDate)
                    .atZoneSameInstant(MoroccoZone)
                    .format(
                        DateTimeFormatter.ofPattern(
                            "dd MMM • HH:mm",
                            Locale.ENGLISH
                        )
                    )
            } catch (e: Exception) {
                try {
                    Instant.parse(rawDate)
                        .atZone(MoroccoZone)
                        .format(
                            DateTimeFormatter.ofPattern(
                                "dd MMM • HH:mm",
                                Locale.ENGLISH
                            )
                        )
                } catch (e2: Exception) {
                    rawDate
                }
            }

            val statusObject = fixture?.optJSONObject("status")

            val status =
                statusObject?.optString("long", "Scheduled")
                    ?: "Scheduled"

            val statusShort =
                statusObject?.optString("short", "NS")
                    ?: "NS"

            val homeGoals =
                goals?.opt("home")
                    ?.takeUnless { it == JSONObject.NULL }
                    ?.toString() ?: "-"

            val awayGoals =
                goals?.opt("away")
                    ?.takeUnless { it == JSONObject.NULL }
                    ?.toString() ?: "-"

            result.add(
                FootballMatch(
                    id = fixture?.optLong("id", i.toLong())
                        ?: i.toLong(),
                    league = leagueName,
                    home = home,
                    away = away,
                    date = localDate,
                    timestamp = timestamp,
                    status = status,
                    statusShort = statusShort,
                    score = "$homeGoals - $awayGoals"
                )
            )
        }

        return result.sortedBy { it.timestamp }

    } finally {
        connection.disconnect()
    }
}
