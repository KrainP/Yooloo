package utils;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.logging.*;

public class YoolooLogger
{
    static Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    static FileHandler fileHandler = null;
    //Handler handler = new ConsoleHandler();

    static
    {
        File directory = new File("./logs/");
        logger.setLevel(Level.ALL);
        //handler.setLevel(Level.ALL);

        if (!directory.exists())
        {
            directory.mkdirs();
        }

        try
        {
            fileHandler = new FileHandler(String .valueOf(directory) + "/" + LocalDateTime.now().toString().replace(":", "-") + ".log");
            fileHandler.setLevel(Level.ALL);
        }
        catch (SecurityException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        fileHandler.setFormatter(new SimpleFormatter());
        //logger.addHandler(handler);
        logger.addHandler(fileHandler);
    }
    public static void info(String msg)
    {
        logger.info(msg);
    }
    public static void error(String msg)
    {
        logger.severe(msg);
    }

    public static void debug(String msg)
    {
        logger.fine(msg);
    }
}
