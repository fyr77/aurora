/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 06.12.12
 * Time: 16:38
 */

package ru.game.aurora.player.research.projects;

import ru.game.aurora.player.Player;
import ru.game.aurora.player.research.Geodata;
import ru.game.aurora.player.research.ResearchProjectDesc;

/**
 * Cartography project converts Raw Geodata into Processed Geodata.
 * Is always available
 */
public class Cartography extends ResearchProjectDesc {
    private Geodata geodata;

    public Cartography(Geodata geodata) {
        super("Cartography", "Processing of raw geological data received \n from sensors and scanning and converting it into its final \n and usable form, such as terrain maps");
        this.geodata = geodata;
    }

    @Override
    public void update(Player player, int scientists) {
        geodata.process(scientists);
    }

    @Override
    public String getStatusString(Player player, int scientists) {
        return String.format("Remaining raw geodata: %d, est. %s days left", geodata.getRaw(), scientists != 0 ? Integer.toString(geodata.getRaw() / scientists) : "<inf>");
    }

    @Override
    public boolean isCompleted() {
        return geodata.getRaw() == 0;
    }

    @Override
    public boolean isRepeatable() {
        return true;
    }
}
