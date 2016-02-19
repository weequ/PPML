package fi.weequ.fmidatafetcher;

import java.net.URISyntaxException;
import org.apache.http.client.utils.URIBuilder;

public class FMIQueryBuilder {
    
    private URIBuilder uriBuilder;
    
    
    public FMIQueryBuilder(String apiKey, String storedQueryId) {
        uriBuilder = new URIBuilder();
        uriBuilder = uriBuilder.setScheme("http");
        uriBuilder = uriBuilder.setHost("data.fmi.fi");
        uriBuilder = uriBuilder.setPath("/fmi-apikey/"+apiKey+"/wfs");
        uriBuilder = uriBuilder.setParameter("request", "getFeature");
        uriBuilder = uriBuilder.setParameter("storedquery_id", storedQueryId);
    }
    
    public FMIQueryBuilder setStartTime(String startTime) {
        uriBuilder = uriBuilder.setParameter("starttime", startTime);
        return this;
    }
    
    public FMIQueryBuilder setEndTime(String endTime) {
        uriBuilder = uriBuilder.setParameter("endtime", endTime);
        return this;
    }
    
    public FMIQueryBuilder setWmo(String wmo) {
        uriBuilder = uriBuilder.setParameter("wmo", wmo);
        return this;
    }
    
    public FMIQuery build() throws URISyntaxException {
        return new FMIQuery(uriBuilder.build());
    }

    
}
