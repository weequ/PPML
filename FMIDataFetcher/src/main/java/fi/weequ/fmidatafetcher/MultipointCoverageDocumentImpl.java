package fi.weequ.fmidatafetcher;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.w3c.dom.Document;

public class MultipointCoverageDocumentImpl implements MultipointCoverageDocument {

    private static final DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'").withZoneUTC();
    private final Document document;
    
    public MultipointCoverageDocumentImpl(Document document) {
        this.document = document;
    }
    
    @Override
    public String valueElementContent() {
        return document.getElementsByTagName("gml:doubleOrNilReasonTupleList").item(0).getTextContent().trim();
    }

    @Override
    public DateTime beginTime() {
        return DateTime.parse(document.getElementsByTagName("gml:beginPosition").item(0).getTextContent().trim(), dtf);
    }

    @Override
    public DateTime endTime() {
        return DateTime.parse(document.getElementsByTagName("gml:endPosition").item(0).getTextContent().trim(), dtf);
    }
    
}
