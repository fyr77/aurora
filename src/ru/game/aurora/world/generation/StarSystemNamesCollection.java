package ru.game.aurora.world.generation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: User
 * Date: 11.05.13
 * Time: 17:17
 */
public class StarSystemNamesCollection {
    private static final Logger logger = LoggerFactory.getLogger(StarSystemNamesCollection.class);

    private final List<String> names = new LinkedList<>();

    public StarSystemNamesCollection() {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream("star_names.txt")))) {
            String line = br.readLine();
            while (line != null) {
                names.add(line);
                line = br.readLine();
            }
        } catch (IOException e) {
            logger.error("Failed to read star names file", e);
        }
    }

    public synchronized String popName() {
        if (names.isEmpty()) {
            logger.warn("Not enough star names");
            return "Nameless star";
        }
        return names.remove(0) + " system";
    }
}
