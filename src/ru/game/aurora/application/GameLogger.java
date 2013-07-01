/**
 * User: jedi-philosopher
 * Date: 29.11.12
 * Time: 23:24
 */
package ru.game.aurora.application;

import java.util.ArrayList;
import java.util.List;

public class GameLogger {

    private List<LoggerAppender> appenders = new ArrayList<>();

    private static final GameLogger instance = new GameLogger();

    public static GameLogger getInstance() {
        return instance;
    }

    public GameLogger()
    {
        appenders.add(new ConsoleAppender());
    }

    public static interface LoggerAppender
    {
        public void logMessage(String message);
    }

    public static final class ConsoleAppender implements LoggerAppender
    {

        @Override
        public void logMessage(String message) {
            System.out.println(message);
        }
    }

    public void addAppender(LoggerAppender appender)
    {
        appenders.add(appender);
    }

    public void logMessage(String message)
    {
        for (LoggerAppender appender : appenders) {
            appender.logMessage(message);
        }
    }
}
