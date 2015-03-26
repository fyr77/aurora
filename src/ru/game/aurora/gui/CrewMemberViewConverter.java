package ru.game.aurora.gui;

import de.lessvoid.nifty.controls.Button;
import de.lessvoid.nifty.controls.ListBox;
import de.lessvoid.nifty.elements.Element;
import ru.game.aurora.npc.CrewMember;
import ru.game.aurora.util.EngineUtils;

public class CrewMemberViewConverter
        implements ListBox.ListBoxViewConverter<CrewMember> {
    @Override
    public void display(Element element, CrewMember item) {
        EngineUtils.setTextForGUIElement(element.findElementByName("#line-text"), item.getLocalizedName("crew") + "\n" + item.getLocalizedText("crew"));
        EngineUtils.setImageForGUIElement(element.findElementByName("#line-icon"), item.getDrawable().getImage());
        if (!item.hasAction()) {
            element.findElementByName("#callButton").hide();
        } else {
            Button b = element.findNiftyControl("#callButton", Button.class);
            b.getElement().show();
            b.setText(item.getActionButtonCaption());
        }
        element.setUserData(item);
    }

    @Override
    public int getWidth(Element element, CrewMember o) {
        return 512;
    }

    @Override
    public int getHeight(Element element, CrewMember crewMember) {
        return 138;
    }
}
