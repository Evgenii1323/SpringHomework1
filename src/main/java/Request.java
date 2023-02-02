import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

public class Request {
    private final String method;
    private final String path;
    private final String query;
    private final List<NameValuePair> queryParams;

    public Request(String[] parts) {
        this.method = parts[0];
        URI uri = URI.create(parts[1]);
        this.path = uri.getPath();
        this.query = uri.getQuery();
        this.queryParams = URLEncodedUtils.parse(URI.create(parts[1]), "UTF-8");
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public String getQuery() {
        return query;
    }

    public List<NameValuePair> getQueryParam(String name) {
        return queryParams.stream()
                .filter(i -> i.getName().equals(name))
                .collect(Collectors.toList());
    }

    public List<NameValuePair> getQueryParams() {
        return queryParams;
    }
}