package fi.weequ.fingriddatafetcher;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.SortedMap;
import java.util.TreeMap;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;


public class FingridDataFetcher {
    
    
    
    public static SortedMap<DateTime, Double> fetch(DateTime utcBeginTime, DateTime utcEndTime) throws URISyntaxException, IOException {
        System.out.println(utcBeginTime.toString("yyyyMMdd"));
        URIBuilder uriBuilder = new URIBuilder();
        uriBuilder = uriBuilder.setScheme("http");
        uriBuilder = uriBuilder.setHost("www.fingrid.fi");
        uriBuilder = uriBuilder.setPath("/fi/sahkomarkkinat/kulutus-ja-tuotanto/Sivut/default.aspx/Excel/TimeSeries.xls");
        uriBuilder = uriBuilder.setParameter("beginDate", utcBeginTime.toString("yyyyMMdd"));
        uriBuilder = uriBuilder.setParameter("endDate", utcEndTime.toString("yyyyMMdd"));
        uriBuilder = uriBuilder.setParameter("variables", "H4sIAAAAAAAEAO29B2AcSZYlJi9tynt_SvVK1-B0oQiAYBMk2JBAEOzBiM3mkuwdaUcjKasqgcplVmVdZhZAzO2dvPfee--999577733ujudTif33_8_XGZkAWz2zkrayZ4hgKrIHz9-fB8_Ih7_ZFYX2aTMmyP769nyvDp6_CJb5Efnxe__dl2u23Xz-1_P28d3-cPHZ8vLvG5_MivX1CIrm_zxXf-jx_zjWVUvsvZ1WxfLi6MfH_34j-88vtv_4vFXy6J9k79rj7747vzu_PFd-_fjN0Vb5kev_9G_bP72H_07lqni8fiufP74ebHMT6qyqtO73h_fbhfl0Y8_e7ZDz-O74cePX8-rqy-X5fUzAlTnT7M2M-hHvnn8PL_Il7OzRXaRn9R51uazo7ZeU-PIFxiaT7wIKZ-d_f5vvvryzfGLN1_-_r_Pt9_8nNKyXVdttmyrWxFzh5-fQ2LaP5uj_wfNrW2SrwIAAA2");
        uriBuilder = uriBuilder.setParameter("cultureId", "fi-FI");
        uriBuilder = uriBuilder.setParameter("dataTimePrecision", "5");
        URI uri = uriBuilder.build();
        HttpGet httpget = new HttpGet(uri);
        CloseableHttpClient httpclient = HttpClients.createDefault();
        CloseableHttpResponse response = httpclient.execute(httpget);
        InputStream is = response.getEntity().getContent();
        
        //POIFSFileSystem fs = new POIFSFileSystem(is);
        HSSFWorkbook workbook = new HSSFWorkbook(is);
 
        HSSFSheet sheet = workbook.getSheetAt(0);
        SortedMap<DateTime, Double> result = new TreeMap<>();
        boolean first = true;
        for (Row row : sheet) {
            if (first) {
                first = false;
                continue;
            }

            String utcTimeStr = row.getCell(0).getStringCellValue();
            DateTime utcTime = DateTimeFormat.forPattern("dd.MM.yyyy HH:mm").withZoneUTC().parseDateTime(utcTimeStr);
            Double electricityConsumption = row.getCell(2).getNumericCellValue();
            result.put(utcTime, electricityConsumption);
        }
        workbook.close();
        is.close();
        return result;
    }
    
    
}
