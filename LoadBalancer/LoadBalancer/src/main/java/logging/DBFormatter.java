package logging;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class DBFormatter extends Formatter {

    private static final DateFormat df = new SimpleDateFormat("hh:mm:ss");

    @Override
    public String format(LogRecord record) {
        StringBuilder format = new StringBuilder();
        format.append(switch (record.getLevel().toString()) {
            case "INFO" -> "\u001B[32m";
            case "WARNING", "SEVERE" -> "\u001B[31m";
            default -> "\u001B[37m";
        });
        format.append("[").append(record.getLevel().toString()).append(' ').append(df.format(new Date(record.getMillis()))).append("] ");
        format.append("[").append(record.getSourceClassName()).append('.').append(record.getSourceMethodName()).append("]\n");
        format.append(record.getMessage()).append("\n\n").append("\u001B[0m");

        return format.toString();
    }
}
