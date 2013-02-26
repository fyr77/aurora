/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 06.02.13
 * Time: 15:17
 */
package ru.game.aurora.world.generation.aliens;


import ru.game.aurora.npc.AlienRace;
import ru.game.aurora.npc.Dialog;
import ru.game.aurora.npc.SingleShipFixedTime;
import ru.game.aurora.npc.StandartAlienShipEvent;
import ru.game.aurora.world.World;
import ru.game.aurora.world.generation.WorldGeneratorPart;
import ru.game.aurora.world.quest.AuroraProbe;
import ru.game.aurora.world.space.HomeworldGenerator;
import ru.game.aurora.world.space.StarSystem;

public class HumanityGenerator implements WorldGeneratorPart
{
    @Override
    public void updateWorld(World world) {
        AlienRace humans = new AlienRace("Humanity", "earth_transport", 10, Dialog.loadFromFile("dialogs/human_ship_default_dialog.json"));
        humans.setTravelDistance(1);
        world.getRaces().put(humans.getName(), humans);
        world.addListener(new SingleShipFixedTime(4, new AuroraProbe(0, 0), Dialog.loadFromFile(getClass().getClassLoader().getResourceAsStream("dialogs/quest/aurora_probe_detected.json"))));

        // earth
        StarSystem solarSystem = HomeworldGenerator.createSolarSystem(humans);
        solarSystem.setQuestLocation(true);
        humans.setHomeworld(solarSystem);
        world.getGalaxyMap().getObjects().add(solarSystem);
        world.getGalaxyMap().setTileAt(9, 9, world.getGalaxyMap().getObjects().size() - 1);
        world.addListener(new StandartAlienShipEvent(humans, true));
    }
}
