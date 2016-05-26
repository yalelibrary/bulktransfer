package yale;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import java.util.concurrent.TimeUnit;

/**
 * @author Osman Din
 */
public class HttpClientManager {

    private final PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();

    protected HttpClient httpClient;

    private static final int DEFAULT_MAX_PER_ROUTE = 5;

    private static final int IDLE_TIMEOUT = 3;

    private static final String appUrl = buildAppRestUrl();

    public HttpClientManager() {
        setConnectionManagerProps(connectionManager);
        httpClient = HttpClients.custom().setConnectionManager(connectionManager).build();
    }

    private static void setConnectionManagerProps(final PoolingHttpClientConnectionManager cm) {
        cm.setMaxTotal(Integer.MAX_VALUE);
        cm.setDefaultMaxPerRoute(DEFAULT_MAX_PER_ROUTE);
        cm.closeIdleConnections(IDLE_TIMEOUT, TimeUnit.SECONDS);
    }

    public HttpGet doGET(final String param) {
        final String url = appUrl + param + "/";
        HttpGet get = new HttpGet(url);
        return get;
    }

    private static String getProp(String s) {
        return System.getProperty(s);
    }

    public HttpClient getHttpClient() {
        return httpClient;
    }

    private static String buildAppRestUrl() {
        return "http://localhost:8085/fileservice/rest/search/"; //TODO dynamic
    }
}

