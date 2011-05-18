/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ion.core.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 *
 * @author cmueller
 */
public class IonTime {

    private static final SimpleDateFormat sdf;
    private static final TimeZone tz;

    static {
        tz = TimeZone.getTimeZone("UTC");
        sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        sdf.setTimeZone(tz);
    }

    private static SimpleDateFormat getSdf(String pattern) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        sdf.setTimeZone(tz);
        return sdf;
    }
    
    private String iso8601;
    private long millis;

    /**
     * Factory method for returning an IonTime object representing the provided ISO8601 compliant date in the form:<br>
     * "yyyy-MM-dd'T'HH:mm:ss.sss'Z'"
     * @param iso8601 The string date
     * @return an IonTime object representing the specified date
     * @throws ParseException if an error is encountered when attempting to parse the specified string
     */
    public static IonTime parseISO8601(String iso8601) throws ParseException {
        long millis = sdf.parse(iso8601).getTime();
        return new IonTime(iso8601, millis);
    }

    public static IonTime parseMillis(long millis) {
        String iso = sdf.format(new java.util.Date(millis));
        return new IonTime(iso, millis);
    }

    public static IonTime now() {
        return parseMillis(new java.util.Date().getTime());
    }

    private IonTime(String iso8601, long millis) {
        this.iso8601 = iso8601;
        this.millis = millis;
    }

    public String getIso8601() {
        return iso8601;
    }

    public long getMillis() {
        return millis;
    }

    @Override
    public String toString() {
        return new StringBuilder(iso8601).append(" {").append(millis).append("}").toString();
    }
}
