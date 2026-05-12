package com.example.scrollic.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.scrollic.R
import com.example.scrollic.navigation.Screen
import com.example.scrollic.network.NewsItem
import com.example.scrollic.screens.extra.DrawerContent
import com.example.scrollic.network.FeedUiState
import com.example.scrollic.network.FeedViewModel
import kotlin.math.roundToInt
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(
    navController: NavController,
    feedViewModel: FeedViewModel
) {
    var isMenuOpen by remember { mutableStateOf(false) }
    val uiState by feedViewModel.uiState.collectAsState()

    // Загружаем новости при первом открытии
    LaunchedEffect(Unit) {
        feedViewModel.loadFeed()
    }

    val transition = updateTransition(isMenuOpen, label = "drawer")

    val offsetX by transition.animateFloat(label = "offset") {
        if (it) 600f else 0f
    }

    val scale by transition.animateFloat(label = "scale") {
        if (it) 0.85f else 1f
    }

    val radius by transition.animateDp(label = "radius") {
        if (it) 24.dp else 0.dp
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Drawer (боковое меню)
        DrawerContent(
            modifier = Modifier
                .zIndex(0f)
                .alpha(if (isMenuOpen) 1f else 0f),
            onItemClick = { route ->
                isMenuOpen = false
                navController.navigate(route)
            },
            onLogoutClick = {
                navController.navigate(Screen.Auth.route) {
                    popUpTo(Screen.Home.route) {
                        inclusive = true
                    }
                }
            }
        )

        // Основной контент
        Box(
            modifier = Modifier
                .zIndex(1f)
                .fillMaxSize()
                .offset { IntOffset(offsetX.roundToInt(), 0) }
                .scale(scale)
                .clip(RoundedCornerShape(radius))
                .clickable(
                    enabled = isMenuOpen,
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    isMenuOpen = false
                }
        ) {
            // Фоновое изображение
            Image(
                painter = painterResource(R.drawable.main_background),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Контент с лентой новостей
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Верхняя панель с кнопкой меню
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 25.dp, top = 60.dp, end = 16.dp)
                ) {
                    IconButton(
                        onClick = {
                            isMenuOpen = !isMenuOpen
                        }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.menu_icon),
                            contentDescription = "Меню",
                            tint = Color.Unspecified
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))
                }

                // Лента новостей
                when (uiState) {
                    is FeedUiState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    is FeedUiState.Success -> {
                        val newsList = (uiState as FeedUiState.Success).news

                        if (newsList.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Новостей пока нет",
                                    fontSize = 16.sp,
                                    color = Color.Gray
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(vertical = 8.dp)
                            ) {
                                items(newsList, key = { it.id }) { news ->
                                    NewsCard(
                                        newsItem = news,
                                        modifier = Modifier
                                    )
                                }

                                item {
                                    Spacer(modifier = Modifier.height(16.dp))
                                }
                            }
                        }
                    }

                    is FeedUiState.Error -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text(
                                    text = (uiState as FeedUiState.Error).message,
                                    fontSize = 16.sp,
                                    color = Color.Red
                                )
                                Button(
                                    onClick = { feedViewModel.refresh() }
                                ) {
                                    Text("Повторить")
                                }
                            }
                        }
                    }

                    else -> {}
                }
            }
        }
    }
}

@Composable
fun NewsCard(
    newsItem: NewsItem,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { isExpanded = !isExpanded },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
        ) {
            // Изображение новости
            if (!newsItem.url_picture.isNullOrBlank()) {
                val painter = rememberAsyncImagePainter(
                    model = newsItem.url_picture,
                    error = painterResource(R.drawable.ic_launcher_foreground),
                    placeholder = painterResource(R.drawable.ic_launcher_foreground)
                )

                Image(
                    painter = painter,
                    contentDescription = newsItem.head,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            // Контент новости
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Заголовок
                Text(
                    text = newsItem.head,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A),
                    lineHeight = 24.sp,
                    maxLines = if (isExpanded) Int.MAX_VALUE else 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Тема
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFFE8ECF4),
                    modifier = Modifier
                ) {
                    Text(
                        text = getThemeName(newsItem.theme_id),
                        fontSize = 12.sp,
                        color = Color(0xFF636363),
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Текст новости
                Text(
                    text = if (isExpanded) newsItem.text else (newsItem.summary ?: if (newsItem.text.length > 150) newsItem.text.take(150) + "..." else newsItem.text),
                    fontSize = 14.sp,
                    color = Color(0xFF4A4A4A),
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Дата и ссылка
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatDate(newsItem.created_at),
                        fontSize = 12.sp,
                        color = Color.Gray
                    )

                    if (isExpanded && newsItem.url.isNotBlank()) {
                        Text(
                            text = "Читать полностью →",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(newsItem.url))
                                context.startActivity(intent)
                            }
                        )
                    }
                }
            }
        }
    }
}

// Вспомогательные функции
fun formatDate(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        outputFormat.format(date ?: Date())
    } catch (e: Exception) {
        dateString
    }
}

fun getThemeName(themeId: Int): String {
    return when (themeId) {
        1 -> "Спорт"
        2 -> "Политика"
        3 -> "Экономика"
        4 -> "Общество"
        5 -> "Технологии"
        else -> "Разное"
    }
}