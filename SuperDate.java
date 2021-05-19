package git;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class SuperDate extends Date {
    final static String TIMEZONE = "PST";
    String pattern = "EEE MMM d HH:mm:ss yyyy Z";
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
    public SuperDate() {
        super();
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone(TIMEZONE));
    }
    public SuperDate(long i) {
        super(0l);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone(TIMEZONE));

    }
    @Override
    public String toString() {
        String date = simpleDateFormat.format(this);
        return date;
    }
}
