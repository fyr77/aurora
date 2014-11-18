/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 16.04.13
 * Time: 16:01
 */
package ru.game.aurora.world.generation;

import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.gui.HelpPopupControl;
import ru.game.aurora.world.GameEventListener;
import ru.game.aurora.world.GameObject;
import ru.game.aurora.world.World;
import ru.game.aurora.world.planet.Planet;
import ru.game.aurora.world.space.NPCShip;
import ru.game.aurora.world.space.StarSystem;

import java.util.ArrayList;
import java.util.List;


public class TutorialGenerator implements WorldGeneratorPart {
    private static final long serialVersionUID = 3572666350589263973L;


    @Override
    public void updateWorld(World world) {

        world.getGlobalVariables().put("tutorial.landing", null);

        // Repair tutorial dialog - activated after taking damage for the first time
        world.addListener(new GameEventListener() {

            private static final long serialVersionUID = 6031148398441609424L;

            private int count = -1;

            private boolean repairTutorialShown = false;

            private boolean starSystemTutorialShown = false;

            private boolean spaceCombatTutorialShown = false;

            private boolean planetTutorialShown = false;

            @Override
            public boolean onTurnEnded(World world) {
                if (count < 0) {
                    return false;
                }

                if (--count == 0) {
                    world.addOverlayWindow(Dialog.loadFromFile("dialogs/tutorials/repair_tutorial.json"));
                    --count;
                    return true;
                } else {
                    return false;
                }
            }

            @Override
            public boolean onPlayerEnterStarSystem(World world, StarSystem ss) {
                if (starSystemTutorialShown && spaceCombatTutorialShown) {
                    return false;
                }

                List<String> helpIds = new ArrayList<String>();
                if (!starSystemTutorialShown) {
                    helpIds.add("star_system");
                    helpIds.add("star_system_2");
                    starSystemTutorialShown = true;
                }

                if (!spaceCombatTutorialShown) {
                    for (GameObject go : ss.getObjects()) {
                        if (go instanceof NPCShip) {
                            helpIds.add("star_system_battle");
                            helpIds.add("star_system_battle_2");
                            spaceCombatTutorialShown = true;
                            break;
                        }
                    }
                }
                if (!helpIds.isEmpty()) {
                    HelpPopupControl.setHelpIds(helpIds);
                    HelpPopupControl.showHelp();
                }
                return false;
            }

            @Override
            public boolean onPlayerLandedPlanet(World world, Planet planet) {
                if (!planetTutorialShown) {
                    planetTutorialShown = true;
                    HelpPopupControl.showHelp("planet", "planet_2", "planet_3");
                }
                return false;
            }

            @Override
            public boolean onPlayerShipDamaged(World world) {
                if (!repairTutorialShown) {
                    count = 2;
                    repairTutorialShown = true;
                }
                return false;
            }


        });


    }
}
