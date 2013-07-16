package ru.game.aurora.world.space.earth;

import de.lessvoid.nifty.Nifty;
import ru.game.aurora.dialog.DialogListener;
import ru.game.aurora.dialog.Reply;
import ru.game.aurora.dialog.Statement;
import ru.game.aurora.gui.DialogController;
import ru.game.aurora.gui.EarthProgressScreenController;
import ru.game.aurora.gui.GUI;
import ru.game.aurora.world.World;

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
    public void onDialogEnded(World world, int returnCode) {

        if (returnCode == 1) {
            // player has chosen to dump research info

            int daysPassed = world.getTurnCount() - earth.getLastVisitTurn();
            Statement stmt;


            if (daysPassed > 50) {

                ((EarthProgressScreenController) GUI.getInstance().getNifty().getScreen("earth_progress_screen").getScreenController()).updateStats();
                int totalScore = earth.dumpResearch(world);
                double scorePerTurn = (double) totalScore / (daysPassed);
                stmt = new Statement(0, String.format("Let us see. You have brought us new %d points of data, giving %f points/day", totalScore, scorePerTurn), new Reply(0, 0, ""));

                if (scorePerTurn < 0.01) {
                    world.getPlayer().increaseFailCount();
                    if (world.getPlayer().getFailCount() > 3) {
                        // unsatisfactory
                        stmt.replies[0] = new Reply(0, 3, "=continue=");
                    } else {
                        // poor
                        stmt.replies[0] = new Reply(0, 2, "=continue=");
                    }
                } else {
                    // ok
                    stmt.replies[0] = new Reply(0, 1, "=continue=");
                }
                earth.setLastVisitTurn(world.getTurnCount());
            } else {
                stmt = new Statement(0, "We are pleased to see you come back, but your flight was too short to judge your performance. Come back later after you have acquired more data", new Reply(0, -1, "Ok"));
            }
            earth.getProgressDialog().putStatement(stmt);


            // hack: we do not want to return to current dialog screen after opening a new one, push next screen instead
            GUI.getInstance().pushScreen("earth_screen");
            earth.getProgressDialog().enter(world);
            Nifty nifty = GUI.getInstance().getNifty();
            ((DialogController) nifty.getScreen("dialog_screen").getScreenController()).setDialog(earth.getProgressDialog());
            nifty.gotoScreen("dialog_screen");

            if (daysPassed > 50) {
                // show research screen, must call after addOverlayWindow(dialog)
                GUI.getInstance().pushCurrentScreen();
                GUI.getInstance().getNifty().gotoScreen("earth_progress_screen");
            }
        }


        //reset dialog state
        earth.getEarthDialog().enter(world);

    }
}