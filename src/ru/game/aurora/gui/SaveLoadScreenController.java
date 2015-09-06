/**
 * Created with IntelliJ IDEA.
 * User: User
 * Date: 19.08.14
 * Time: 22:59
 */

package ru.game.aurora.gui;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.NiftyEventSubscriber;
import de.lessvoid.nifty.controls.ButtonClickedEvent;
import de.lessvoid.nifty.controls.ListBox;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import ru.game.aurora.application.AuroraGame;
import ru.game.aurora.application.SaveGameManager;
import ru.game.aurora.modding.ModManager;
import ru.game.aurora.world.World;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class SaveLoadScreenController implements ScreenController {
    private ListBox<SaveGameManager.SaveGameSlot> slots;

    @Override
    public void bind(Nifty nifty, Screen screen) {
        slots = screen.findNiftyControl("items", ListBox.class);
    }

    @Override
    public void onStartScreen() {
        slots.clear();
        List<SaveGameManager.SaveGameSlot> list = new ArrayList<>();
        list.add(SaveGameManager.getAutosaveSlot());
        Collections.addAll(list, SaveGameManager.getSlots());
        slots.addAllItems(list);
    }

    @Override
    public void onEndScreen() {

    }

    public void closeScreen() {
        GUI.getInstance().popAndSetScreen();
    }

    public void savePressed() {
        if (slots.getFocusItem().isLoaded()) {
            Nifty nifty = GUI.getInstance().getNifty();
            Element popup = nifty.createPopup("overwrite_save");
            nifty.showPopup(nifty.getCurrentScreen(), popup.getId(), null);
        } else {
            doSave();
        }
    }

    public void doSave() {
        closePopup();
        SaveGameManager.saveGame(slots.getFocusItem(), GUI.getInstance().getWorldInstance());
        GUI.getInstance().getNifty().getCurrentScreen().layoutLayers();
        closeScreen();
    }

    public void doLoad() {
        closePopup();
        World world = SaveGameManager.loadGame(slots.getFocusItem());
        if (world == null) {
            Nifty nifty = GUI.getInstance().getNifty();
            Element popup = nifty.createPopup("load_failed");
            nifty.showPopup(nifty.getCurrentScreen(), popup.getId(), null);
            return;
        }
        ModManager.getInstance().onGameLoaded(world);
        world.gameLoaded();
        AuroraGame.onGameLoaded(world);
    }

    public void closePopup() {
        Element topMostPopup = GUI.getInstance().getNifty().getTopMostPopup();
        if (topMostPopup != null) {
            GUI.getInstance().getNifty().closePopup(topMostPopup.getId());
        }
    }

    public void loadPressed() {
        if (!slots.getFocusItem().isLoaded()) {
            return;
        }

        if (GUI.getInstance().getWorldInstance() != null) {
            Nifty nifty = GUI.getInstance().getNifty();
            Element popup = nifty.createPopup("really_load");
            nifty.showPopup(nifty.getCurrentScreen(), popup.getId(), null);
        } else {
            doLoad();
        }
    }

    //magic
    @NiftyEventSubscriber(pattern = ".*Button")
    public void onClicked(String id, ButtonClickedEvent event) {
        slots.selectItem((SaveGameManager.SaveGameSlot) event.getButton().getElement().getParent().getUserData());
    }
}
