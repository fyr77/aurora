
/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 06.02.13
 * Time: 15:14
 */
package ru.game.aurora.world.generation.aliens;

import ru.game.aurora.npc.AlienRace;
import ru.game.aurora.npc.Dialog;
import ru.game.aurora.npc.SingleShipFixedTime;
import ru.game.aurora.world.World;
import ru.game.aurora.world.generation.WorldGeneratorPart;
import ru.game.aurora.world.space.NPCShip;

/**
 * Creates Gardener alien race
 */
public class GardenerGenerator implements WorldGeneratorPart
{
    @Override
    public void updateWorld(World world) {
        AlienRace gardenerRace = new AlienRace("Gardeners", "gardener_ship", 8, Dialog.loadFromFile(getClass().getClassLoader().getResourceAsStream("dialogs/gardener_default_dialog.json")));
        NPCShip gardenerShip = new NPCShip(0, 0, gardenerRace.getShipSprite(), gardenerRace, null, null);
        gardenerShip.setAi(null);
        world.addListener(new SingleShipFixedTime(1, gardenerShip, Dialog.loadFromFile(getClass().getClassLoader().getResourceAsStream("dialogs/gardener_ship_detected.json"))));
    }
}
