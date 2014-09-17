/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 19.06.14
 * Time: 13:56
 */
package ru.game.aurora.gui.niffy;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.NiftyEventSubscriber;
import de.lessvoid.nifty.controls.ButtonClickedEvent;
import de.lessvoid.nifty.controls.Controller;
import de.lessvoid.nifty.controls.ListBox;
import de.lessvoid.nifty.controls.WindowClosedEvent;
import de.lessvoid.nifty.controls.window.WindowControl;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.input.NiftyInputEvent;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.xml.xpp3.Attributes;
import ru.game.aurora.gui.GUI;
import ru.game.aurora.world.GameObject;
import ru.game.aurora.world.IStateChangeListener;

import java.util.List;
import java.util.Properties;


public class InteractionTargetSelectorController implements Controller {
    private Element myWindow;

    private ListBox<GameObject> listBox;

    private IStateChangeListener<GameObject> callback;

    private static String popupId;

    public static void open(IStateChangeListener<GameObject> selectionListener, List<GameObject> so) {
        final Nifty nifty = GUI.getInstance().getNifty();
        Element target_selection_popup = nifty.createPopup("target_selection_popup");
        popupId = target_selection_popup.getId();
        InteractionTargetSelectorController controller = target_selection_popup.findControl("interaction_target_selector", InteractionTargetSelectorController.class);
        controller.setCallback(selectionListener);
        controller.setObjects(so);
        nifty.showPopup(nifty.getCurrentScreen(), popupId, null);

    }

    public void setObjects(List<GameObject> objects) {
        listBox.clear();
        listBox.addAllItems(objects);
        myWindow.layoutElements();
    }

    @Override
    public void bind(Nifty nifty, Screen screen, Element element, Properties parameter, Attributes controlDefinitionAttributes) {
        myWindow = element.findControl("#target_selection_window", WindowControl.class).getElement();
        listBox = element.findNiftyControl("#objects", ListBox.class);

    }

    @Override
    public void init(Properties parameter, Attributes controlDefinitionAttributes) {
    }

    @Override
    public void onStartScreen() {
        myWindow.setVisible(true);
        GUI.getInstance().getWorldInstance().setPaused(true);
    }

    @Override
    public void onFocus(boolean getFocus) {
    }

    @Override
    public boolean inputEvent(NiftyInputEvent inputEvent) {
        return false;
    }


    @NiftyEventSubscriber(pattern = ".*target_selection_window")
    public void onClose(String id, WindowClosedEvent event) {
        GUI.getInstance().getNifty().closePopup(GUI.getInstance().getNifty().getTopMostPopup().getId());
        GUI.getInstance().getWorldInstance().setPaused(false);
    }

    @NiftyEventSubscriber(pattern = ".*Button")
    public void onClicked(String id, ButtonClickedEvent event) {
        // hack: this method is called twice for some reason, second call to closePopup leads to a crash
        if (callback != null) {
            int numericId = Integer.parseInt(id.split("#")[0]);
            numericId -= Integer.parseInt(listBox.getElement().findElementByName("#child-root").getElements().get(0).getId());
            final int finalNumericId = numericId;
            GUI.getInstance().getNifty().closePopup(popupId);
            callback.stateChanged(listBox.getItems().get(finalNumericId));
            callback = null;
            GUI.getInstance().getWorldInstance().setPaused(false);
        }
    }

    public void setCallback(IStateChangeListener<GameObject> callback) {
        this.callback = callback;
    }
}
