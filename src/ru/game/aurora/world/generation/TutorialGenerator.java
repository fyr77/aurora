/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 16.04.13
 * Time: 16:01
 */
package ru.game.aurora.world.generation;

import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.world.GameEventListener;
import ru.game.aurora.world.World;


public class TutorialGenerator implements WorldGeneratorPart
{
    private static final long serialVersionUID = 3572666350589263973L;



    @Override
    public void updateWorld(World world) {

        // Repair tutorial dialog - activated after taking damage for the first time
        world.addListener(new GameEventListener() {

            private static final long serialVersionUID = 6031148398441609424L;

            private int count = -1;

            @Override
            public void onTurnEnded(World world) {
                if (count < 0) {
                    return;
                }

                if (--count == 0) {
                    world.addOverlayWindow(Dialog.loadFromFile("dialogs/tutorials/repair_tutorial.json"));
                    isAlive = false;
                }
            }

            @Override
            public void onPlayerShipDamaged(World world) {
                count = 2;
            }


        });



    }
}
