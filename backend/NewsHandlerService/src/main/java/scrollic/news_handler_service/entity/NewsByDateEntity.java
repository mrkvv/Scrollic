package scrollic.news_handler_service.entity;

import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Table("news_by_date")
public class NewsByDateEntity {

    // TODO: Что туда класть??
    @PrimaryKeyColumn(name = "date_bucket", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private String dateBucket;

    @PrimaryKeyColumn(name = "created_at", ordinal = 1, type = PrimaryKeyType.CLUSTERED)
    private Instant createdAt;

    @PrimaryKeyColumn(name = "id", ordinal = 2, type = PrimaryKeyType.CLUSTERED)
    private UUID id;

    @Column("theme_id")
    private int themeId;

    @Column("popularity")
    private int popularity;

    @Column("head")
    private String head;

    @Column("summary")
    private String summary;

    @Column("url")
    private String url;

    public NewsByDateEntity() { }

    public String getDateBucket() {
        return dateBucket;
    }

    public void setDateBucket(String dateBucket) {
        this.dateBucket = dateBucket;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public int getThemeId() {
        return themeId;
    }

    public void setThemeId(int themeId) {
        this.themeId = themeId;
    }

    public int getPopularity() {
        return popularity;
    }

    public void setPopularity(int popularity) {
        this.popularity = popularity;
    }

    public String getHead() {
        return head;
    }

    public void setHead(String head) {
        this.head = head;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
