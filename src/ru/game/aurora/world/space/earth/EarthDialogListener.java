package ru.game.aurora.world.space.earth;

import ru.game.aurora.application.Configuration;
import ru.game.aurora.application.Localization;
import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.dialog.DialogListener;
import ru.game.aurora.dialog.Reply;
import ru.game.aurora.dialog.Statement;
import ru.game.aurora.gui.EarthProgressScreenController;
import ru.game.aurora.gui.GUI;
import ru.game.aurora.world.World;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 10.06.13
 * Time: 16:22
 */

public class EarthDialogListener implements DialogListener {
    private static final long serialVersionUID = 6653410057967364076L;

    private Earth earth;

    public EarthDialogListener(Earth earth) {
        this.earth = earth;
    }

    @Override
    public void onDialogEnded(World world, Dialog dialog, int returnCode, Map<String, String> flags) {

        if (returnCode == 1) {
            // player has chosen to dump research info

            int daysPassed = world.getTurnCount() - earth.getLastVisitTurn();
            Statement stmt;


            if (daysPassed > Configuration.getIntProperty("game.minimumTripDays")) {

                ((EarthProgressScreenController) GUI.getInstance().getNifty().getScreen("earth_progress_screen").getScreenController()).updateStats();
                int totalScore = earth.dumpResearch(world);
                double scorePerTurn = (double) totalScore / (daysPassed);
                stmt = new Statement(0, String.format(Localization.getText("dialogs", "earth.progress_string"), totalScore, scorePerTurn), new Reply(0, 0, ""));

                if (scorePerTurn < Configuration.getDoubleProperty("game.progress.targetScorePerDay")) {
                    world.getPlayer().increaseFailCount();
                    if (world.getPlayer().getFailCount() > Configuration.getIntProperty("game.allowedFails")) {
                        // unsatisfactory
                        stmt.replies[0] = new Reply(0, 3, "continue");
                    } else {
                        // poor
                        stmt.replies[0] = new Reply(0, 2, "continue");
                    }
                } else {
                    // ok
                    stmt.replies[0] = new Reply(0, 1, "continue");
                }
                earth.setLastVisitTurn(world.getTurnCount());
            } else {
                stmt = new Statement(0, Localization.getText("dialogs", "earth.trip_too_short"), new Reply(0, -1, "ok"));
            }
            earth.getProgressDialog().putStatement(stmt);

            world.addOverlayWindow(earth.getProgressDialog());

            if (daysPassed > Configuration.getIntProperty("game.minimumTripDays")) {
                GUI.getInstance().pushCurrentScreen();
                GUI.getInstance().getNifty().gotoScreen("earth_progress_screen");
            }

        } else {
            // return
            world.setCurrentRoom(earth.getOwner());
            GUI.getInstance().getNifty().gotoScreen("star_system_gui");
        }


        //reset dialog state
        earth.getEarthDialog().enter(world);

    }
}
