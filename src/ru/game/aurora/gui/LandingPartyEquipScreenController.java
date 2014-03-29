/**
 * Created with IntelliJ IDEA.
 * User: User
 * Date: 07.07.13
 * Time: 20:04
 */

package ru.game.aurora.gui;

import com.google.common.collect.Multiset;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.NiftyEventSubscriber;
import de.lessvoid.nifty.controls.*;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.elements.render.TextRenderer;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import de.lessvoid.nifty.tools.Color;
import ru.game.aurora.application.Configuration;
import ru.game.aurora.application.Localization;
import ru.game.aurora.application.ResourceManager;
import ru.game.aurora.util.EngineUtils;
import ru.game.aurora.world.Ship;
import ru.game.aurora.world.World;
import ru.game.aurora.world.equip.LandingPartyWeapon;
import ru.game.aurora.world.planet.InventoryItem;
import ru.game.aurora.world.planet.LandingParty;


public class LandingPartyEquipScreenController implements ScreenController {

    private Color redColor = new Color(200, 0, 0, 255);

    private World world;

    private LandingParty landingParty;

    private Screen myScreen;

    private Element myWindow;

    private Element weightText;

    private Element statusText;

    private ListBox<Multiset.Entry<InventoryItem>> storageList;

    private ListBox<Multiset.Entry<InventoryItem>> inventoryList;

    public LandingPartyEquipScreenController(World world) {
        this.world = world;
        landingParty = world.getPlayer().getLandingParty();
        if (landingParty == null) {
            landingParty = new LandingParty(0, 0, ResourceManager.getInstance().getLandingPartyWeapons().getEntity("assault"), 1, 1, 1, Configuration.getIntProperty("player.landing_party.defaultHP"));
            world.getPlayer().setLandingParty(landingParty);
        }
    }

    @Override
    public void bind(Nifty nifty, Screen screen) {
        myScreen = screen;
        myWindow = myScreen.findElementByName("equip_window");
        weightText = myScreen.findElementByName("weight_text");
        statusText= myScreen.findElementByName("status_text");
        storageList = screen.findNiftyControl("storageList", ListBox.class);
        inventoryList = screen.findNiftyControl("inventoryList", ListBox.class);
    }

    @Override
    public void onStartScreen() {
        myWindow.setVisible(true);
        world.setPaused(true);
        Scrollbar scrollbar = myScreen.findNiftyControl("scientists_count", Scrollbar.class);
        scrollbar.setValue(landingParty.getScience());
        scrollbar = myScreen.findNiftyControl("engineers_count", Scrollbar.class);
        scrollbar.setValue(landingParty.getEngineers());
        scrollbar = myScreen.findNiftyControl("military_count", Scrollbar.class);
        scrollbar.setValue(landingParty.getMilitary());

        DropDown weaponSelect = myScreen.findNiftyControl("weapon_select", DropDown.class);
        weaponSelect.clear();
        for (InventoryItem item : world.getPlayer().getInventory().keySet()) {
            if (item instanceof LandingPartyWeapon) {
                weaponSelect.addItem(item);
            }
        }

        updateLabels();
        refreshLists();
    }

    private void updateLabels() {
        myScreen.findElementByName("scientists_count_text").getRenderer(TextRenderer.class).setText(Localization.getText("gui", "landing_party.scientists") + " " + landingParty.getScience());
        myScreen.findElementByName("engineers_count_text").getRenderer(TextRenderer.class).setText(Localization.getText("gui", "landing_party.engineers") + " " + landingParty.getEngineers());
        myScreen.findElementByName("military_count_text").getRenderer(TextRenderer.class).setText(Localization.getText("gui", "landing_party.military") + " " + landingParty.getMilitary());
        myScreen.findElementByName("total_count").getRenderer(TextRenderer.class).setText(Localization.getText("gui", "landing_party.total") + " " + landingParty.getTotalMembers() + " / 10");

        EngineUtils.setTextForGUIElement(weightText, String.format(Localization.getText("gui", "landing_party.weight"), landingParty.getInventoryWeight(), landingParty.getMaxWeight()));
    }

    @Override
    public void onEndScreen() {
        world.setPaused(false);
    }

    @NiftyEventSubscriber(id = "weapon_select")
    public void onWeaponSelected(final String id, final DropDownSelectionChangedEvent event) {
        if (event.getSelection() == null) {
            return;
        }
        final LandingPartyWeapon weapon = (LandingPartyWeapon) event.getSelection();
        landingParty.setWeapon(weapon);

        EngineUtils.setImageForGUIElement(myScreen.findElementByName("selected_weapon_img"), weapon.getImage());
        myScreen.findElementByName("selected_weapon_text").getRenderer(TextRenderer.class).setText(weapon.getName());
    }

    @NiftyEventSubscriber(pattern = ".*_count")
    public void onScrollbarMoved(final String id, final ScrollbarChangedEvent event) {
        String scrollbarId = event.getScrollbar().getId();
        final Ship ship = world.getPlayer().getShip();
        switch (scrollbarId) {
            case "scientists_count": {
                int oldVal = landingParty.getScience();
                landingParty.setScience((int) event.getValue());
                if (landingParty.getTotalMembers() > 10 || landingParty.getScience() > ship.getScientists()) {
                    if (oldVal > ship.getScientists()) {
                        oldVal = 0;
                    }
                    event.getScrollbar().setValue(oldVal);
                    landingParty.setScience(oldVal);
                }
                break;
            }
            case "engineers_count": {
                int oldVal = landingParty.getEngineers();
                landingParty.setEngineers((int) event.getValue());
                if (landingParty.getTotalMembers() > 10 || landingParty.getEngineers() > ship.getEngineers()) {
                    if (oldVal > ship.getEngineers()) {
                        oldVal = 0;
                    }
                    event.getScrollbar().setValue(oldVal);
                    landingParty.setEngineers(oldVal);
                }
                break;
            }
            default:
                int oldVal = landingParty.getMilitary();
                landingParty.setMilitary((int) event.getValue());
                if (landingParty.getTotalMembers() > 10 || landingParty.getMilitary() > ship.getMilitary()) {
                    if (oldVal > ship.getMilitary()) {
                        oldVal = 0;
                    }
                    event.getScrollbar().setValue(oldVal);
                    landingParty.setMilitary(oldVal);
                }
                break;
        }

        if (!landingParty.canBeLaunched(world)) {
            // disable close buttons, because current party configuration is invalid
            myScreen.findElementByName("close_button").disable();
            statusText.getRenderer(TextRenderer.class).setColor(redColor);
            EngineUtils.setTextForGUIElement(statusText, Localization.getText("gui", "landing_party.can_not_launch"));
        } else {
            myScreen.findElementByName("close_button").enable();
            statusText.getRenderer(TextRenderer.class).setColor(new Color("#3C2C41"));
            EngineUtils.setTextForGUIElement(statusText, Localization.getText("gui", "landing_party.can_launch"));
        }
        updateLabels();
    }

    public void closeScreen() {
        GUI.getInstance().popAndSetScreen();
    }

    public void onStorageToInventoryClicked() {
        world.getPlayer().getLandingParty().pickUp(storageList.getFocusItem().getElement(), 1);
        world.getPlayer().getShip().getStorage().setCount(storageList.getFocusItem().getElement(), storageList.getFocusItem().getCount() - 1);
        if (storageList.getFocusItem().getCount() == 0) {
            storageList.removeItem(storageList.getFocusItem());
        }
        refreshLists();
    }

    public void onInventoryToStorageClicked() {
        world.getPlayer().getShip().addItem(inventoryList.getFocusItem().getElement(), 1);
        world.getPlayer().getLandingParty().getInventory().setCount(inventoryList.getFocusItem().getElement(), inventoryList.getFocusItem().getCount() - 1);
        if (inventoryList.getFocusItem().getCount() == 0) {
            inventoryList.removeItem(inventoryList.getFocusItem());
        }
        refreshLists();
    }

    private void refreshLists() {
        storageList.clear();
        for (Multiset.Entry<InventoryItem> entry : world.getPlayer().getShip().getStorage().entrySet()) {
            storageList.addItem(entry);
        }
        inventoryList.clear();
        for (Multiset.Entry<InventoryItem> entry : world.getPlayer().getLandingParty().getInventory().entrySet()) {
            inventoryList.addItem(entry);
        }
        updateLabels();
    }

    //это - очень сильное колдунство. onClicked занят ниже. Поэтому тут - onReleased. Костыль
    @NiftyEventSubscriber(pattern = ".*storage_to_inventory")
    public void onReleased(String id, ButtonClickedEvent event) {
        int numericId = Integer.parseInt(id.split("#")[0]);
        ListBox itemsList = storageList;
        numericId -= Integer.parseInt(itemsList.getElement().findElementByName("#child-root").getElements().get(0).getId());
        itemsList.setFocusItemByIndex(numericId);
    }

    //костыль к костылю. YO DAWG
    @NiftyEventSubscriber(pattern = ".*inventory_to_storage")
    public void onPrimaryReleased(String id, ButtonClickedEvent event) {
        int numericId = Integer.parseInt(id.split("#")[0]);
        ListBox itemsList = inventoryList;
        numericId -= Integer.parseInt(itemsList.getElement().findElementByName("#child-root").getElements().get(0).getId());
        itemsList.setFocusItemByIndex(numericId);
    }
}
