/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 06.02.13
 * Time: 15:14
 */
package ru.game.aurora.world.generation.aliens;

import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.npc.AlienRace;
import ru.game.aurora.world.World;
import ru.game.aurora.world.generation.WorldGeneratorPart;

/**
 * Creates Gardener alien race
 */
public class GardenerGenerator implements WorldGeneratorPart {
    private static final long serialVersionUID = -2142318077060757284L;

    public static final String NAME = "Gardeners";

    @Override
    public void updateWorld(World world) {
        final Dialog dialog = Dialog.loadFromFile("dialogs/gardener_1.json");

        AlienRace gardenerRace = new AlienRace(NAME, "gardener_ship", dialog);

        world.getRaces().put(gardenerRace.getName(), gardenerRace);

        world.getGlobalVariables().put("gardeners.first_warp", true);
    }
}
