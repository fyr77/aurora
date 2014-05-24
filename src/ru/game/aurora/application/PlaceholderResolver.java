package ru.game.aurora.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Replaces placeholders like ${id} with their values
 */
public class PlaceholderResolver {
    private static Pattern pattern = Pattern.compile("\\$\\{([\\w\\.]+)\\}");

    private static final Logger logger = LoggerFactory.getLogger(PlaceholderResolver.class);

    public static String resolvePlaceholders(String originalString, Map<String, ?> values) {
        Matcher matcher = pattern.matcher(originalString);
        StringBuilder resultBuilder = new StringBuilder();
        int index = 0;
        while (matcher.find()) {
            int start = matcher.start();
            resultBuilder.append(originalString.substring(index, start));
            final String name = matcher.group(1);
            Object value = values.get(name);
            if (value == null) {
                logger.warn("Placeholder " + name + " not resolved");
                resultBuilder.append("${").append(name).append("}");
            } else {
                resultBuilder.append(value.toString());
            }
            index = matcher.end();
        }
        if (index < originalString.length()) {
            resultBuilder.append(originalString.substring(index));
        }

        return resultBuilder.toString();
    }
}
