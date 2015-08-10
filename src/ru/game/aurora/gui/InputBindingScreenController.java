package ru.game.aurora.gui;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.NiftyEventSubscriber;
import de.lessvoid.nifty.controls.ButtonClickedEvent;
import de.lessvoid.nifty.controls.ListBox;
import de.lessvoid.nifty.controls.listbox.ListBoxView;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import org.newdawn.slick.Input;
import org.newdawn.slick.KeyListener;
import ru.game.aurora.application.Configuration;
import ru.game.aurora.application.InputBinding;

import java.util.Map;

/**
 * Created by Егор on 10.08.2015.
 */
public class InputBindingScreenController implements ScreenController {

    private Input input;

    private ListBox<Map.Entry<InputBinding.Action, Integer>> keyMap;

    public InputBindingScreenController(Input input) {
        this.input = input;
    }

    @Override
    public void bind(Nifty nifty, Screen screen) {
        keyMap = screen.findNiftyControl("items", ListBox.class);
    }

    @Override
    public void onStartScreen() {
        keyMap.addAllItems(InputBinding.keyBinding.entrySet());
    }

    @Override
    public void onEndScreen() {

    }

    public void applySettings()
    {
        for (Map.Entry<InputBinding.Action, Integer> entry : keyMap.getItems()) {
            InputBinding.keyBinding.put(entry.getKey(), entry.getValue());
        }

        Configuration.saveSystemProperties();
    }

    public void cancelSettings()
    {
        GUI.getInstance().getNifty().gotoScreen("settings_screen");
    }

    @NiftyEventSubscriber(pattern = "#redefineButton")
    public void redefine(String id, ButtonClickedEvent event) {

        final InputBinding.Action action = (InputBinding.Action) event.getButton().getElement().getParent().getUserData();
        input.addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(int i, char c) {

            }

            @Override
            public void keyReleased(int i, char c) {
                input.removeKeyListener(this);
                for (Map.Entry<InputBinding.Action, Integer> e : keyMap.getItems()) {
                    if (e.getKey() == action) {
                        e.setValue(i);
                        break;
                    }
                }
            }

            @Override
            public void setInput(Input input) {

            }

            @Override
            public boolean isAcceptingInput() {
                return true;
            }

            @Override
            public void inputEnded() {

            }

            @Override
            public void inputStarted() {

            }
        });
    }
}
