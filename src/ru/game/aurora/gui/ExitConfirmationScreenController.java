package ru.game.aurora.gui;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import ru.game.aurora.application.AuroraGame;

/**
 * Created with IntelliJ IDEA.
 * User: User
 * Date: 17.01.14
 * Time: 20:55
 */
public class ExitConfirmationScreenController implements ScreenController {

    private boolean goToMainMenu = false;

    @Override
    public void bind(Nifty nifty, Screen screen) {
    }

    @Override
    public void onStartScreen() {
    }

    @Override
    public void onEndScreen() {
    }

    public void closeScreen() {
        GUI.getInstance().popAndSetScreen();
    }

    public void exitGame() {
        if (!goToMainMenu) {
            AuroraGame.exitGame();
        } else {
            AuroraGame.goToMainMenu();
        }
    }

    public void setGoToMainMenu(boolean goToMainMenu) {
        this.goToMainMenu = goToMainMenu;
    }
}
