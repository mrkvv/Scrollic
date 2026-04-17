package com.example.scrollic.screens

import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.example.scrollic.navigation.Screen
import com.example.scrollic.screens.extra.DrawerContent
import kotlin.math.roundToInt
import com.example.scrollic.R
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

@Composable
fun HomeScreen(navController: NavController) {

    var isMenuOpen by remember { mutableStateOf(false) }
    val newsList = remember { MockNews.newsList } // Получаем список новостей

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

                // Лента новостей (LazyColumn для вертикальной прокрутки)
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(newsList) { news ->  // ← Исправлено: items, а не items
                        NewsCard(
                            news = news,
                            modifier = Modifier  // ← Убрали animateItemPlacement
                        )
                    }

                    // Добавляем отступ снизу для удобства прокрутки
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

data class News(
    val id: Int,
    val title: String,
    val content: String,
    val imageResId: Int?,
    val tags: List<String>
)

object MockNews {
    val newsList = listOf(
        News(
            id = 1,
            title = "Запуск Scrollic!",
            content = "Мы рады представить новое приложение для чтения новостей. Scrollic поможет вам быть в курсе последних событий в мире технологий и не только. Подписывайтесь на интересующие вас темы и получайте персонализированную ленту новостей.",
            imageResId = null, // Временно, позже замените на реальные картинки
            tags = listOf("Анонс", "Scrollic", "Новости")
        ),
        News(
            id = 2,
            title = "Учёные нашли ещё один повод не отказываться от кофе",
            content = "Исследователи из Фуданьского университета изучили данные почти 400 тысяч человек и пришли к выводу, что регулярное употребление кофе может быть связано с более низким риском депрессии и тревожности. Предполагается, что всё дело в кофеине – он способен ненадолго улучшать настроение и влиять на выработку дофамина.",
            imageResId = R.drawable.photo1,
            tags = listOf("Разное")
        ),
        News(
            id = 3,
            title = "Disney сократила более 1000 сотрудников по всем подразделениям",
            content = "Компания пошла на масштабные увольнения в рамках оптимизации расходов. Под сокращения попали сотрудники сразу в нескольких направлениях бизнеса. Особенно сильно решение ударило по Marvel Studios, которой руководит Кевин Файги. По данным источников, в студии уволили почти всю команду, связанную с визуальными эффектами, включая специалистов с опытом работы более десяти лет. После сокращений, как сообщается, осталась только небольшая группа сотрудников, которая будет координировать найм специалистов под отдельные проекты.",
            imageResId = R.drawable.photo2,
            tags = listOf("Разное")
        ),

    )
}

@Composable
fun NewsCard(
    news: News,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
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
            if (news.imageResId != null) {
                Image(
                    painter = painterResource(id = news.imageResId),
                    contentDescription = news.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                    contentScale = ContentScale.FillWidth
                )
            }

            // Контент новости
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Заголовок
                Text(
                    text = news.title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A),
                    lineHeight = 28.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Теги
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    news.tags.forEach { tag ->
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color(0xFFE8ECF4),
                            modifier = Modifier
                        ) {
                            Text(
                                text = tag,
                                fontSize = 12.sp,
                                color = Color(0xFF636363),
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Полный текст новости
                Text(
                    text = news.content,
                    fontSize = 14.sp,
                    color = Color(0xFF4A4A4A),
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NewsPreview() {
    // Создаём тестовую новость для превью
    val sampleNews = News(
        id = 1,
        title = "Запуск Scrollic!",
        content = "Мы рады представить новое приложение для чтения новостей. Scrollic поможет вам быть в курсе последних событий.",
        imageResId = R.drawable.ic_launcher_foreground,
        tags = listOf("Анонс", "Scrollic", "Новости")
    )

    NewsCard(
        news = sampleNews,
        modifier = Modifier.padding(16.dp)
    )
}