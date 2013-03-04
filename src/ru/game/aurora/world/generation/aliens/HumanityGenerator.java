/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 06.02.13
 * Time: 15:17
 */
package ru.game.aurora.world.generation.aliens;


import ru.game.aurora.application.CommonRandom;
import ru.game.aurora.npc.*;
import ru.game.aurora.npc.shipai.LandOnPlanetAI;
import ru.game.aurora.world.World;
import ru.game.aurora.world.generation.WorldGeneratorPart;
import ru.game.aurora.world.quest.AuroraProbe;
import ru.game.aurora.world.space.HomeworldGenerator;
import ru.game.aurora.world.space.NPCShip;
import ru.game.aurora.world.space.StarSystem;

public class HumanityGenerator implements WorldGeneratorPart
{
    private static final long serialVersionUID = -1289210420627927980L;

    @Override
    public void updateWorld(World world) {
        final AlienRace humans = new AlienRace("Humanity", "earth_transport", 10, Dialog.loadFromFile("dialogs/human_ship_default_dialog.json"));
        humans.setTravelDistance(1);
        world.getRaces().put(humans.getName(), humans);
        world.addListener(new SingleShipFixedTime(4, new AuroraProbe(0, 0), Dialog.loadFromFile(getClass().getClassLoader().getResourceAsStream("dialogs/quest/aurora_probe_detected.json"))));

        // earth
        final StarSystem solarSystem = HomeworldGenerator.createSolarSystem(humans);
        solarSystem.setQuestLocation(true);
        humans.setHomeworld(solarSystem);
        world.getGalaxyMap().getObjects().add(solarSystem);
        world.getGalaxyMap().setTileAt(9, 9, world.getGalaxyMap().getObjects().size() - 1);

        // set custom ship AIs, only land on planets, do not leave star system
        world.addListener(new StandardAlienShipEvent(humans, new NPCShipFactory() {

            private static final long serialVersionUID = 5580476648808262345L;

            @Override
            public NPCShip createShip() {
                NPCShip ship = humans.getDefaultFactory().createShip();
                ship.setAi(new LandOnPlanetAI(solarSystem.getPlanets()[CommonRandom.getRandom().nextInt(solarSystem.getPlanets().length)]));
                return ship;
            }
        }, true));
    }
}
