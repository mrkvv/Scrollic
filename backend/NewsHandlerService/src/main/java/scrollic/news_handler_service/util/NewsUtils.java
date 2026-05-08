package scrollic.news_handler_service.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public final class NewsUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(NewsUtils.class);

    private static final DateTimeFormatter DATE_BUCKET_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private NewsUtils() { }

    private static String formatDateBucket(Instant instant) {
        return LocalDate.ofInstant(instant, ZoneOffset.UTC)
                .format(DATE_BUCKET_FORMATTER);
    }

    public static String getDateBucket(Instant publishedTime) {
        return formatDateBucket(publishedTime
                .plus(7, java.time.temporal.ChronoUnit.DAYS));
    }

    public static UUID generateUuidFromUrl(String url) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(url.getBytes(StandardCharsets.UTF_8));
            return UUID.nameUUIDFromBytes(digest);
        } catch (NoSuchAlgorithmException e) {
            LOGGER.warn("MD5 не доступен, был использован случайный UUID");
            return UUID.randomUUID();
        }
    }
}
