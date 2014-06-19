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
import ru.game.aurora.world.IStateChangeListener;
import ru.game.aurora.world.space.SpaceObject;

import java.util.List;
import java.util.Properties;


public class InteractionTargetSelectorController implements Controller
{
    private Element myWindow;

    private ListBox<SpaceObject> listBox;

    private IStateChangeListener<SpaceObject> callback;

    private static String popupId;

    public static void open(IStateChangeListener<SpaceObject> selectionListener, List<SpaceObject> so)
    {
        final Nifty nifty = GUI.getInstance().getNifty();
        Element target_selection_popup = nifty.findPopupByName("target_selection_popup");
        if (target_selection_popup == null) {
            target_selection_popup = nifty.createPopup("target_selection_popup");
            popupId = target_selection_popup.getId();
        }
        nifty.showPopup(nifty.getCurrentScreen(), popupId, null);
        InteractionTargetSelectorController controller = target_selection_popup.findControl("interaction_target_selector", InteractionTargetSelectorController.class);
        controller.setCallback(selectionListener);
        controller.setObjects(so);
    }

    public void setObjects(List<SpaceObject> objects)
    {
        listBox.clear();
        listBox.addAllItems(objects);
    }

    @Override
    public void bind(Nifty nifty, Screen screen, Element element, Properties parameter, Attributes controlDefinitionAttributes) {
        myWindow = element.findControl("target_selection_window", WindowControl.class).getElement();
        listBox = element.findNiftyControl("objects", ListBox.class);

    }

    @Override
    public void init(Properties parameter, Attributes controlDefinitionAttributes) {
    }

    @Override
    public void onStartScreen() {
        myWindow.setVisible(true);
    }

    @Override
    public void onFocus(boolean getFocus) {
    }

    @Override
    public boolean inputEvent(NiftyInputEvent inputEvent) {
        return false;
    }

    public void selectPressed()
    {
        if (callback != null) {
            callback.stateChanged(listBox.getSelection().get(0));
            callback = null;
        }
        GUI.getInstance().getNifty().closePopup(popupId);
    }

    public void onClose(String id, WindowClosedEvent event) {
        myWindow.show();
        GUI.getInstance().getNifty().closePopup(popupId);
    }

    @NiftyEventSubscriber(pattern = ".*Button")
    public void onClicked(String id, ButtonClickedEvent event) {
        int numericId = Integer.parseInt(id.split("#")[0]);
        numericId -= Integer.parseInt(listBox.getElement().findElementByName("#child-root").getElements().get(0).getId());
        listBox.setFocusItemByIndex(numericId);
    }

    public void setCallback(IStateChangeListener<SpaceObject> callback) {
        this.callback = callback;
    }
}
