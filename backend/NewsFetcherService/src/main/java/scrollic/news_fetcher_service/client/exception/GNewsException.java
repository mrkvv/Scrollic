package scrollic.news_fetcher_service.client.exception;

public class GNewsException extends RuntimeException {

    private final int statusCode;

    public GNewsException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public boolean isRetryable() {
        return statusCode >= 500 || statusCode == 429;
    }
}
