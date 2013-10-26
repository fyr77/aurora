package ru.game.aurora.world;

import ru.game.aurora.gui.FailScreenController;
import ru.game.aurora.gui.GUI;

/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 08.10.13
 * Time: 16:29
 */
public class CrewChangeListener extends GameEventListener {
    private static final long serialVersionUID = 918401389630037926L;

    @Override
    public boolean onCrewChanged(World world) {
        int count = world.getPlayer().getShip().getTotalCrew();
        if (world.getCurrentDungeon() != null) {
            count += world.getPlayer().getLandingParty().getTotalMembers();
        }

        if (count <= 0) {
            GUI.getInstance().getNifty().gotoScreen("fail_screen");
            FailScreenController controller = (FailScreenController) GUI.getInstance().getNifty().findScreenController(FailScreenController.class.getCanonicalName());
            controller.set("crew_lost_gameover", "crew_lost");
        }
        return false;
    }
}
