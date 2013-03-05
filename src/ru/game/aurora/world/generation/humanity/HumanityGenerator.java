/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 06.02.13
 * Time: 15:17
 */
package ru.game.aurora.world.generation.humanity;


import ru.game.aurora.application.CommonRandom;
import ru.game.aurora.npc.*;
import ru.game.aurora.npc.shipai.LandOnPlanetAI;
import ru.game.aurora.player.earth.PrivateMessage;
import ru.game.aurora.world.World;
import ru.game.aurora.world.generation.WorldGeneratorPart;
import ru.game.aurora.world.quest.AuroraProbe;
import ru.game.aurora.world.space.HomeworldGenerator;
import ru.game.aurora.world.space.NPCShip;
import ru.game.aurora.world.space.StarSystem;

import java.util.List;

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

        // add welcoming messages
        List<PrivateMessage> pm = world.getPlayer().getEarthState().getMessages();

        pm.add(new PrivateMessage("Good Luck", "Greetings. \n On behalf of Earth Command of Aurora space exploration project I whis you good luck in your mission." +
                "The fate of Earth, the future of humanity depends on heroes like you and your crew. \n A. V. Buren, Aurora CEO", "message"));
        pm.add(new PrivateMessage("Status Report", "Greetings. This is an automated message sent to you by UNS information center. All cargo loaded according to " +
                " provided manifests and your ship is ready to launch at any time. Have a nice flight. \n Do not respond to this message.", "message"));

        // add Enterprise ship event
        world.addListener(new EnterpriseEncounterCreator());
    }
}
