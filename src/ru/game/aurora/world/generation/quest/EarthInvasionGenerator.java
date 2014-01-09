package ru.game.aurora.world.generation.quest;

import ru.game.aurora.application.CommonRandom;
import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.dialog.DialogListener;
import ru.game.aurora.npc.AlienRace;
import ru.game.aurora.world.GameEventListener;
import ru.game.aurora.world.World;
import ru.game.aurora.world.generation.WorldGeneratorPart;
import ru.game.aurora.world.generation.humanity.HumanityGenerator;
import ru.game.aurora.world.space.StarSystem;

import java.util.Map;

/**
 * If player sells full info about earth to Klisk, there is some chance that they will sell this info to one of other races
 * This will lead to one of special events in solar system
 */
public class EarthInvasionGenerator implements WorldGeneratorPart
{
    private static final long serialVersionUID = 1113857719613332116L;

    private void generateRoguesEvent(World world)
    {
        final AlienRace humanity = world.getRaces().get(HumanityGenerator.NAME);

        world.addListener(new GameEventListener() {

            private static final long serialVersionUID = 5416195464346353846L;

            @Override
            public boolean onPlayerEnterStarSystem(World world, StarSystem ss) {
                if (ss == humanity.getHomeworld()) {
                    world.addOverlayWindow(Dialog.loadFromFile("dialogs/encounters/rogues_altar_scientist.json"));
                    return false;
                }
                return true;
            }
        });

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
    }

    @Override
    public void updateWorld(World world) {
        if (CommonRandom.getRandom().nextBoolean()) {
            // nothing happens
            return;
        }

        generateRoguesEvent(world);
    }
}
