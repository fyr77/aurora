package ru.game.aurora.world;

import ru.game.aurora.gui.FailScreenController;
import ru.game.aurora.gui.GUI;

/**
* Created with IntelliJ IDEA.
* User: User
* Date: 16.05.14
* Time: 15:27
*/
public class GameOverEffectListener implements IStateChangeListener<World> {

    private static final long serialVersionUID = -5155503207553019512L;

    @Override
    public void stateChanged(World world) {
        GUI.getInstance().getNifty().gotoScreen("fail_screen");
        FailScreenController controller = (FailScreenController) GUI.getInstance().getNifty().findScreenController(FailScreenController.class.getCanonicalName());
        controller.set("ship_destroyed_gameover", "ship_destroyed");
    }
}
