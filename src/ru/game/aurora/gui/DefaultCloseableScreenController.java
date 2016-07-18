package ru.game.aurora.gui;

import de.lessvoid.nifty.screen.ScreenController;
import org.newdawn.slick.Input;
import ru.game.aurora.application.InputBinding;

abstract public class DefaultCloseableScreenController implements ScreenController {

    public void closeScreen(){
        GUI.getInstance().popAndSetScreen();
    }

    public void inputUpdate(Input input) {
        if(input.isKeyPressed(InputBinding.keyBinding.get(InputBinding.Action.INTERACT))
        || input.isKeyPressed(InputBinding.keyBinding.get(InputBinding.Action.INTERACT_SECONDARY))
        || input.isKeyPressed(Input.KEY_ESCAPE)){
            closeScreen();
            return;
        }
    }
}