package ru.game.aurora.world.generation;

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
    private List<String> names = new LinkedList<>();

    public StarSystemNamesCollection() {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream("star_names.txt")))) {
            String line = br.readLine();
            while (line != null) {
                names.add(line);
                line = br.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized String popName() {
        if (names.isEmpty()) {
            return "Nameless star";
        }
        return names.remove(0) + " system";
    }
}
