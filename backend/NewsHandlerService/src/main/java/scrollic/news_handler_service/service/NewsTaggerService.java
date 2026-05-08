package scrollic.news_handler_service.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import scrollic.news_handler_service.dto.NewsArticle;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class NewsTaggerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NewsTaggerService.class);

    private static final Map<Integer, List<String>> KEYWORDS = new HashMap<>();

    private static final int CATEGORY_OTHER = 19;

    private static final Map<String, Double> WEIGHTS = Map.of(
            "title", 2.0,
            "description", 1.0,
            "content", 0.5
    );

    static {
        KEYWORDS.put(1, List.of("спорт", "футбол", "хоккей", "баскетбол",
                "теннис", "олимпиада", "чемпионат", "тренер", "матч", "стадион"));
        KEYWORDS.put(2, List.of("политика", "президент", "правительство", "депутат",
                "выборы", "госдума", "кремль", "закон", "санкции", "партия"));
        KEYWORDS.put(3, List.of("экономика", "бизнес", "рынок", "инвестиции",
                "валюта", "рубль", "курс", "торги", "банк", "налог"));
        KEYWORDS.put(4, List.of("общество", "социум", "опросили", "пенсии",
                "миграция", "доходы", "благотворительность", "волонтеры"));
        KEYWORDS.put(5, List.of("технологии", "гаджет", "сервис", "приложение",
                "софт", "нейросеть", "робот", "искусственный интеллект"));
        KEYWORDS.put(6, List.of("здоровье", "медицина", "больница", "врач",
                "лечение", "вакцина", "вирус", "болезнь", "клиника"));
        KEYWORDS.put(7, List.of("культура", "кино", "театр", "музей",
                "выставка", "концерт", "книга", "писатель", "артист"));
        KEYWORDS.put(8, List.of("наука", "исследование", "ученый", "открытие",
                "космос", "лаборатория", "эксперимент", "технологии"));
        KEYWORDS.put(9, List.of("экология", "климат", "отходы", "лес",
                "озеленение", "свалка", "загрязнение", "эко"));
        KEYWORDS.put(10, List.of("финансы", "деньги", "акции", "дивиденды",
                "депозит", "кредит", "ипотека", "бюджет", "доход", "расход"));
        KEYWORDS.put(11, List.of("мир", "зарубежье", "европа", "сша",
                "китай", "конфликт", "переговоры", "международный"));
        KEYWORDS.put(12, List.of("instagram", "телеграм", "соцсеть", "like",
                "подписчик", "блогер", "тикток", "твиттер", "репост"));
        KEYWORDS.put(13, List.of("интервью", "беседа", "эксклюзив",
                "рассказал", "поделился", "мнение"));
        KEYWORDS.put(14, List.of("аналитика", "анализ", "исследование",
                "прогноз", "рейтинг", "данные показывают"));
        KEYWORDS.put(15, List.of("тренд", "популярный", "вирусный",
                "новое веяние", "мода", "хайп"));
        KEYWORDS.put(16, List.of("образование", "школа", "университет",
                "студент", "учитель", "егэ", "урок", "курс"));
        KEYWORDS.put(17, List.of("событие", "фестиваль", "форум",
                "конференция", "открытие", "выставка"));
        KEYWORDS.put(18, List.of("развлечения", "игры", "юмор", "тикток",
                "стрим", "мем", "сериал", "шоу", "комедия"));
    }

    public int tagNews(NewsArticle article) {
        Map<Integer, Double> scores = calculateScores(article);
        return selectTag(scores);
    }

    private int selectTag(Map<Integer, Double> scores) {
        return scores.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(CATEGORY_OTHER);
    }

    private Map<Integer, Double> calculateScores(NewsArticle article) {
        Map<Integer, Double> scores = new HashMap<>();

        Map<String, String> texts = Map.of(
                "title", toLowerCase(article.getTitle()),
                "description", toLowerCase(article.getDescription()),
                "content", toLowerCase(article.getContent())
        );

        for (Map.Entry<Integer, List<String>> entry : KEYWORDS.entrySet()) {
            int tag = entry.getKey();
            List<String> keywords = entry.getValue();

            double totalScore = 0;
            for (Map.Entry<String, Double> fieldWeight : WEIGHTS.entrySet()) {
                String field = fieldWeight.getKey();
                double weight = fieldWeight.getValue();

                totalScore += countMatches(texts.get(field), keywords) * weight;
            }

            scores.put(tag, totalScore);
        }

        return scores;
    }

    private int countMatches(String text, List<String> keywords) {
        if (text == null || text.isEmpty()) {
            return 0;
        }

        int count = 0;
        for (String keyword : keywords) {
            if(text.contains(keyword)) { count++; }
        }
        return count;
    }

    private String toLowerCase(String value) {
        return value != null ? value.toLowerCase() : "";
    }
}
