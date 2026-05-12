package com.example.scrollic.screens.extra

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.scrollic.R
import com.example.scrollic.network.NewsItem
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun NewsCard(
    newsItem: NewsItem,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current  // ← Получаем контекст здесь

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

                // Тема (вместо тегов)
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFFE8ECF4),
                    modifier = Modifier
                ) {
                    Text(
                        text = "Тема ${newsItem.theme_id}",
                        fontSize = 12.sp,
                        color = Color(0xFF636363),
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Текст новости (краткий или полный)
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
                                // Открыть URL во внешнем браузере - используем контекст
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

// Функция для форматирования даты
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