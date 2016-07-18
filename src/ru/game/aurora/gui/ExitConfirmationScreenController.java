package ru.game.aurora.gui;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.screen.Screen;
import org.newdawn.slick.Input;
import ru.game.aurora.application.AuroraGame;

/**
 * Created with IntelliJ IDEA.
 * User: User
 * Date: 17.01.14
 * Time: 20:55
 */
public class ExitConfirmationScreenController extends DefaultCloseableScreenController {

    private boolean goToMainMenu = false;

    @Override
    public void bind(Nifty nifty, Screen screen) {

    }

    @Override
    public void onStartScreen() {
        if(GUI.getInstance().getWorldInstance() != null) {
            GUI.getInstance().getWorldInstance().setPaused(true);
        }
    }

    @Override
    public void onEndScreen() {
        if(GUI.getInstance().getWorldInstance() != null) {
            GUI.getInstance().getWorldInstance().setPaused(false);
        }
    }

    public void exitGame() {
        if (!goToMainMenu) {
            AuroraGame.exitGame();
        } else {
            if(GUI.getInstance().getWorldInstance() != null){
                GUI.getInstance().getWorldInstance().setGameOver(true);
            }
            else {
                AuroraGame.goToMainMenu();
            }
        }
    }

    public void setGoToMainMenu(boolean goToMainMenu) {
        this.goToMainMenu = goToMainMenu;
    }

    @Override
    public void inputUpdate(Input input) {
        if(input.isKeyPressed(Input.KEY_ENTER)){
            exitGame();
            return;
        }
        else if(input.isKeyPressed(Input.KEY_ESCAPE)){
            closeScreen();
            return;
        }
    }
}
