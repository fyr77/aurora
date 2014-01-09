package ru.game.aurora.world.generation.quest;

import ru.game.aurora.application.CommonRandom;
import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.dialog.DialogListener;
import ru.game.aurora.npc.AlienRace;
import ru.game.aurora.world.AuroraTiledMap;
import ru.game.aurora.world.Dungeon;
import ru.game.aurora.world.GameEventListener;
import ru.game.aurora.world.World;
import ru.game.aurora.world.generation.WorldGeneratorPart;
import ru.game.aurora.world.generation.humanity.HumanityGenerator;
import ru.game.aurora.world.planet.DungeonEntrance;
import ru.game.aurora.world.planet.Planet;
import ru.game.aurora.world.space.StarSystem;

import java.util.Map;

/**
 * If player sells full info about earth to Klisk, there is some chance that they will sell this info to one of other races
 * This will lead to one of special events in solar system
 */
public class EarthInvasionGenerator implements WorldGeneratorPart
{
    private static final long serialVersionUID = 1113857719613332116L;

    private static final class RogueInvasionAdder extends GameEventListener
    {
        private static final long serialVersionUID = -2497678330932578786L;

        private int count = 0;

        @Override
        public boolean onPlayerEnterStarSystem(World world, StarSystem ss) {
            final AlienRace humanity = world.getRaces().get(HumanityGenerator.NAME);
            if (ss == humanity.getHomeworld()) {
                ++count;
            } else {
                return true;
            }

            if (count < 4 || world.getGlobalVariables().containsKey("earth.special_dialog")) {
                return true;
            }
            world.addOverlayWindow(Dialog.loadFromFile("dialogs/encounters/rogues_altar_scientist.json"));

            Dialog earthDialog = Dialog.loadFromFile("dialogs/encounters/rogues_altar_earth.json");

            earthDialog.setListener(new DialogListener() {

                private static final long serialVersionUID = -6367061348256715021L;

                @Override
                public void onDialogEnded(World world, Dialog dialog, int returnCode, Map<String, String> flags) {
                    if (flags.containsKey("klisk.philosophy_research")) {
                        // add new research of a klisk philosophy
                    }
                }
            });

            world.getGlobalVariables().put("earth.special_dialog", earthDialog);

            // now create dungeon on a moon

            Planet moon = (Planet) humanity.getHomeworld().getPlanets()[2].getSatellites().get(0);

            DungeonEntrance entrance = new DungeonEntrance(moon, 5, 5, "builders_pyramid", new Dungeon(world, new AuroraTiledMap("maps/rogue_altar.tmx"), moon));
            moon.setNearestFreePoint(entrance, 5, 5);
            moon.getMap().getObjects().add(entrance);

            return false;
        }
    }

    @Override
    public void updateWorld(World world) {
        if (CommonRandom.getRandom().nextBoolean()) {
            // nothing happens
            //return;
        }

        world.addListener(new RogueInvasionAdder());
    }
}
