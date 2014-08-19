/**
 * User: jedi-philosopher
 * Date: 29.11.12
 * Time: 23:24
 */
package ru.game.aurora.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class GameLogger {

    private static final Logger logger = LoggerFactory.getLogger(GameLogger.class);

    public static final int MAX_LOG_ENTRIES = 50;

    private final List<String> logItems = new ArrayList<>(MAX_LOG_ENTRIES);

    private final List<LoggerAppender> appenders = new ArrayList<>();

    private static final GameLogger instance = new GameLogger();

    public static GameLogger getInstance() {
        return instance;
    }

    public GameLogger() {
        appenders.add(new ConsoleAppender());
    }

    public static interface LoggerAppender {
        public void logMessage(String message);
    }

    public static final class ConsoleAppender implements LoggerAppender {

        @Override
        public void logMessage(String message) {
            logger.info(message);
        }
    }

    public void addAppender(LoggerAppender appender) {
        appenders.add(appender);
    }

    public void logMessage(String message) {
        if (logItems.size() > MAX_LOG_ENTRIES) {
            logItems.remove(0);
        }
        for (LoggerAppender appender : appenders) {
            appender.logMessage(message);
        }
        logItems.add(message);
    }

    public List<String> getLogItems() {
        return logItems;
    }
}
