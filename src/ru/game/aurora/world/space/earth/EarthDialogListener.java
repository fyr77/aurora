package ru.game.aurora.world.space.earth;

import ru.game.aurora.dialog.DialogListener;
import ru.game.aurora.dialog.Reply;
import ru.game.aurora.dialog.Statement;
import ru.game.aurora.gui.GUI;
import ru.game.aurora.world.World;

/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 10.06.13
 * Time: 16:22
 */

public class EarthDialogListener implements DialogListener
{
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
                // show research screen
                GUI.getInstance().pushCurrentScreen();
                GUI.getInstance().getNifty().gotoScreen("earth_progress_screen");

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
                stmt = new Statement(0, "We are pleased to see you come back, but your flight was too short to judge your perfomance. Come back later after you have acquired more data", new Reply(0, -1, "Ok"));
            }
            earth.getProgressDialog().putStatement(stmt);
            world.addOverlayWindow(earth.getProgressDialog());
        }

        //reset dialog state
        earth.getEarthDialog().enter(world);
    }
}
