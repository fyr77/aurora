package ru.game.aurora.gui;

import de.lessvoid.nifty.controls.ListBox;
import de.lessvoid.nifty.elements.Element;
import org.newdawn.slick.Input;
import ru.game.aurora.application.InputBinding;
import ru.game.aurora.util.EngineUtils;

import java.util.Map;

/**
 * Created by Егор on 10.08.2015.
 */
public class InputBindingViewConverter implements ListBox.ListBoxViewConverter<Map.Entry<InputBinding.Action, Integer>> {

    @Override
    public void display(Element element, Map.Entry<InputBinding.Action, Integer> actionIntegerEntry) {
        EngineUtils.setTextForGUIElement(element.findElementByName("#action-text"), actionIntegerEntry.getKey().toString());
        EngineUtils.setTextForGUIElement(element.findElementByName("#value-text"), Input.getKeyName(actionIntegerEntry.getValue()));
        element.setUserData(actionIntegerEntry.getKey());
    }

    @Override
    public int getWidth(Element element, Map.Entry<InputBinding.Action, Integer> actionIntegerEntry) {
        display(element, actionIntegerEntry);
        return element.getWidth();
    }

    @Override
    public int getHeight(Element element, Map.Entry<InputBinding.Action, Integer> actionIntegerEntry) {
        display(element, actionIntegerEntry);
        return element.getHeight();
    }
}
