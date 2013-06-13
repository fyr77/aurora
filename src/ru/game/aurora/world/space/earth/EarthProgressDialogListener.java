package ru.game.aurora.world.space.earth;

import ru.game.aurora.dialog.DialogListener;
import ru.game.aurora.gui.FailScreen;
import ru.game.aurora.world.World;

/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 11.06.13
 * Time: 19:23
 */
public class EarthProgressDialogListener implements DialogListener {
    private static final long serialVersionUID = 266428070604878318L;

    private Earth earth;

    public EarthProgressDialogListener(Earth earth) {
        this.earth = earth;
    }

    @Override
    public void onDialogEnded(World world, int returnCode) {
        if (returnCode == -1) {
            world.setCurrentRoom(FailScreen.createRetirementFailScreen());
            return;
        }
        world.setCurrentRoom(earth.getOwner());


        // refilling crew
        world.getPlayer().getShip().refillCrew();

    }
}
