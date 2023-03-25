package logging;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.ConsoleHandler;
import java.util.logging.Logger;

public class DBLogger {

    private static final Map<Class<?>, Logger> LOGGERS = new HashMap<>();

    private DBLogger(){}

    public static Logger getLogger(Class<?> c){
        Logger logger = LOGGERS.get(c);
        if(logger != null) return logger;

        ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(new DBFormatter());
        logger = Logger.getLogger(c.getName());
        logger.setUseParentHandlers(false);
        logger.addHandler(handler);
        LOGGERS.put(c, logger);
        return logger;
    }

}
