package ru.game.aurora.world.space.earth;

import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.dialog.DialogListener;
import ru.game.aurora.gui.FailScreenController;
import ru.game.aurora.gui.GUI;
import ru.game.aurora.player.earth.PrivateMessage;
import ru.game.aurora.world.World;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 11.06.13
 * Time: 19:23
 */
public class EarthProgressDialogListener implements DialogListener {
    private static final long serialVersionUID = 266428070604878318L;

    private final Earth earth;

    public EarthProgressDialogListener(Earth earth) {
        this.earth = earth;
    }

    @Override
    public void onDialogEnded(World world, Dialog dialog, int returnCode, Map<String, String> flags) {
        // remove 'dialog_screen' from top
        GUI.getInstance().popScreen();
        if (returnCode == -1) {
            // pop previouse screen and replace it with retirement gameover
            GUI.getInstance().popScreen();
            GUI.getInstance().pushScreen("fail_screen");
            GUI.getInstance().getNifty().gotoScreen("fail_screen");
            FailScreenController controller = (FailScreenController) GUI.getInstance().getNifty().findScreenController(FailScreenController.class.getCanonicalName());

            controller.set("retirement_gameover", "retire");
            return;
        }
        world.setCurrentRoom(earth.getOwner());
        GUI.getInstance().getNifty().gotoScreen("earth_screen");

        if (world.getPlayer().getShip().getLostCrewMembers() > 0 && !world.getGlobalVariables().containsKey("messages.mother.received")) {
            world.getPlayer().getEarthState().getMessages().add(new PrivateMessage(world, "letters.mother.sender", "letters.mother", "message"));
            world.getGlobalVariables().put("messages.mother.received", 1);
        }
        // refilling crew
        world.getPlayer().getShip().refillCrew(world);

    }
}
