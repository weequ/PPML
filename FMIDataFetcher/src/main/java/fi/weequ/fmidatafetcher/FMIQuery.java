package fi.weequ.fmidatafetcher;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

public class FMIQuery {

    private URI uri;
    
    public FMIQuery(URI uri) {
        this.uri = uri;
    }

    public InputStream execute() throws IOException {
        HttpGet httpget = new HttpGet(uri);
        CloseableHttpClient httpclient = HttpClients.createDefault();
        CloseableHttpResponse response = httpclient.execute(httpget);
        return response.getEntity().getContent();
    }
}
