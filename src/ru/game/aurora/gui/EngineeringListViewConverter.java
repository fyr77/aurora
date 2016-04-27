/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 16.07.13
 * Time: 15:21
 */
package ru.game.aurora.gui;

import de.lessvoid.nifty.controls.Label;
import de.lessvoid.nifty.controls.ListBox;
import de.lessvoid.nifty.elements.Element;
import ru.game.aurora.player.engineering.EngineeringProject;

public class EngineeringListViewConverter implements ListBox.ListBoxViewConverter {
    private String getText(Object obj) {
        EngineeringProject ep = (EngineeringProject) obj;
        return ep.getLocalizedName("engineering") + "\nEngineers: " + ep.getEngineersAssigned();
    }

    @Override
    public void display(Element listBoxItem, Object obj) {
        Label l = listBoxItem.findNiftyControl("#name_text", Label.class);
        l.setText(getText(obj));
        listBoxItem.setUserData(obj);
    }

    @Override
    public int getWidth(Element element, Object item) {
        display(element, item);
        return element.getWidth();
    }

    @Override
    public int getHeight(Element element, Object o) {
        display(element, o);
        return element.getConstraintHeight().getValueAsInt(1);
    }
}