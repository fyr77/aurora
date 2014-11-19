package ru.game.aurora.gui;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.controls.*;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.input.NiftyInputEvent;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.xml.xpp3.Attributes;
import ru.game.aurora.application.Localization;
import ru.game.aurora.util.EngineUtils;

import java.util.*;

public class HelpPopupControl implements Controller {

    private static List<String> helpMesageIds = new ArrayList<>();

    private static int currentIdx;

    private Element textElement;

    private Element myElement;

    private CheckBox skipHelp;

    private Button prevButton;

    public static void setHelpIds(Collection<String> ids)
    {
        helpMesageIds.clear();
        currentIdx = 0;
        helpMesageIds.addAll(ids);
    }

    public static void setHelpIds(String... ids)
    {
        helpMesageIds.clear();
        currentIdx = 0;
        Collections.addAll(helpMesageIds, ids);
    }

    public static void addHelpIds(String... ids)
    {
        Collections.addAll(helpMesageIds, ids);
    }

    public static void showHelp(String... ids) {
        setHelpIds(ids);
        showHelp();
    }

    public static void showHelp()
    {
        if (GUI.getInstance().getWorldInstance().getGlobalVariables().containsKey("skipHelp")) {
            return;
        }
        GUI.getInstance().getNifty().getCurrentScreen().findControl("help_popup", HelpPopupControl.class).update();
        GUI.getInstance().getNifty().getCurrentScreen().findNiftyControl("help_window", Window.class).getElement().show();
    }

    public static void hideHelp()
    {
        GUI.getInstance().getNifty().getCurrentScreen().findNiftyControl("help_window", Window.class).getElement().hide();
    }

    @Override
    public void bind(Nifty nifty, Screen screen, Element element, Properties properties, Attributes attributes) {
        textElement = element.findElementByName("helpText");
        skipHelp = element.findNiftyControl("skipHelp", CheckBox.class);
        prevButton = element.findNiftyControl("prev_button", Button.class);
        myElement = element;
    }

    @Override
    public void init(Properties properties, Attributes attributes) {

    }

    @Override
    public void onStartScreen() {
        prevButton.disable();
        update();
    }

    @Override
    public void onFocus(boolean b) {
        update();
    }

    @Override
    public boolean inputEvent(NiftyInputEvent niftyInputEvent) {
        return false;
    }

    public void prevHelp()
    {
        currentIdx--;
        if (currentIdx == 0) {
            prevButton.disable();
        }
        update();
    }

    private void update()
    {
        if (currentIdx < helpMesageIds.size()) {
            EngineUtils.setTextForGUIElement(textElement, Localization.getText("help", helpMesageIds.get(currentIdx)));
        }
        ScrollPanel sp = myElement.findNiftyControl("scrollbarPanelId", ScrollPanel.class);
        sp.setVerticalPos(0);
        GUI.getInstance().getNifty().getCurrentScreen().layoutLayers();
    }

    public void nextHelp()
    {
        prevButton.enable();
        currentIdx++;
        if (currentIdx >= helpMesageIds.size() || skipHelp.isChecked()) {
            hideHelp();
            if (skipHelp.isChecked()) {
                GUI.getInstance().getWorldInstance().getGlobalVariables().put("skipHelp", true);
            }
        } else {
            update();
        }
    }

    public boolean isHelpSkipChecked() {
        return skipHelp.isChecked();
    }
}
