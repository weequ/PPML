package fi.weequ.fmidatafetcher;

import org.joda.time.DateTime;


public interface MultipointCoverageDocument {
    public String valueElementContent();
    
    public DateTime beginTime();
    
    public DateTime endTime();
}
