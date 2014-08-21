/**
 * Created with IntelliJ IDEA.
 * User: User
 * Date: 19.08.14
 * Time: 23:25
 */

package ru.game.aurora.gui;

import de.lessvoid.nifty.controls.ListBox;
import de.lessvoid.nifty.elements.Element;
import ru.game.aurora.application.AuroraGame;
import ru.game.aurora.application.Localization;
import ru.game.aurora.application.SaveGameManager;
import ru.game.aurora.util.EngineUtils;


public class SaveViewConverter implements ListBox.ListBoxViewConverter<SaveGameManager.SaveGameSlot> {

    private String buildString(SaveGameManager.SaveGameSlot item) {
        if (!item.isLoaded()) {
            return Localization.getText("gui", "saveload.empty_slot");
        }
        return (item.isAutosave ? Localization.getText("gui", "saveload.autosave") + "\n" : "")
                + item.date.toString() + "\n"
                + item.gameLocation + "\n"
                + item.gameDate;
    }

    @Override
    public void display(Element element, SaveGameManager.SaveGameSlot item) {
        EngineUtils.setTextForGUIElement(element.findElementByName("#line-text"), buildString(item));
        EngineUtils.setImageForGUIElement(element.findElementByName("#line-icon"), item.getScreenshot());
        if (item.isAutosave) {
            element.findElementByName("#saveButton").hide();
        } else {
            element.findElementByName("#saveButton").show();
        }
        if (!AuroraGame.isGameRunning()) {
            element.findElementByName("#saveButton").disable();
        } else {
            element.findElementByName("#saveButton").enable();
        }
        if (!item.isLoaded()) {
            element.findElementByName("#loadButton").disable();
        } else {
            element.findElementByName("#loadButton").enable();
        }
    }

    @Override
    public int getWidth(Element element, SaveGameManager.SaveGameSlot item) {
        return SaveGameManager.SCREEN_SIZE + 400 + 2 * 150 + 20;
    }
}
