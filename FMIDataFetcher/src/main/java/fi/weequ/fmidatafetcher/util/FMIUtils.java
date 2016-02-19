package fi.weequ.fmidatafetcher.util;

import org.joda.time.DateTime;

public class FMIUtils {
    public static String jodaToFMIDate(DateTime jodaTime) {
        return jodaTime.toString("yyyy-MM-dd'T'HH:mm:ss'Z'");//jodaTime.getYear()+"-"+jodaTime.getMonthOfYear()+"-"+jodaTime.getDayOfMonth()+"T00:00:00Z";
    }
}
