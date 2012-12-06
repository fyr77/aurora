/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 06.12.12
 * Time: 16:15
 */
package ru.game.aurora.player.research;

/**
 * Contains all data about science and research done by player
 */
public class ResearchState {
    private Geodata geodata = new Geodata();

    public Geodata getGeodata() {
        return geodata;
    }
}
