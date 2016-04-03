/**
 * Created with IntelliJ IDEA.
 * User: User
 * Date: 13.06.13
 * Time: 22:02
 */
package ru.game.aurora.gui;


import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.NiftyEventSubscriber;
import de.lessvoid.nifty.controls.*;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.elements.render.TextRenderer;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import org.newdawn.slick.Image;
import ru.game.aurora.application.Localization;
import ru.game.aurora.gui.niffy.ProgressBarControl;
import ru.game.aurora.player.earth.EarthState;
import ru.game.aurora.player.earth.EarthUpgrade;
import ru.game.aurora.player.earth.PrivateMessage;
import ru.game.aurora.player.engineering.ShipUpgrade;
import ru.game.aurora.steam.AchievementManager;
import ru.game.aurora.steam.AchievementNames;
import ru.game.aurora.util.EngineUtils;
import ru.game.aurora.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class EarthScreenController implements ScreenController {

    private final World world;

    private Element messagesList;

    //todop: [save] move these multisets into player object when save compatibility is no longer required
    private Multiset<ShipUpgrade> storageMultiset;

    private Multiset<ShipUpgrade> inventoryMultiset;

    private ListBox<Multiset.Entry<ShipUpgrade>> storageList;

    private ListBox<Multiset.Entry<ShipUpgrade>> inventoryList;

    private Element shipYardTab;

    private Element upgradeImage;

    private Element upgradeText;

    private Element humanityProgressTab;

    private ProgressBarControl freeSpace;


    public EarthScreenController(World world) {
        this.world = world;
        storageMultiset = HashMultiset.create();
        inventoryMultiset = HashMultiset.create();
    }

    @Override
    public void bind(Nifty nifty, Screen screen) {
        messagesList = screen.findElementByName("messages_list");
        shipYardTab = screen.findElementByName("shipyard");
        humanityProgressTab = screen.findElementByName("upgrades");
        storageList = shipYardTab.findNiftyControl("storageList", ListBox.class);
        inventoryList = shipYardTab.findNiftyControl("inventoryList", ListBox.class);
        upgradeText = shipYardTab.findElementByName("upgrade_text");
        upgradeImage = shipYardTab.findElementByName("upgrade_icon");
        freeSpace = shipYardTab.findControl("free_space", ProgressBarControl.class);
    }

    @Override
    public void onStartScreen() {
        fillMessages();
        fillUpgrades();
        world.setPaused(true);
        updateShipyardLabels();


        updateHumanityTab();
    }

    private void updateHumanityTab() {
        fillHumanityUpgrades(humanityProgressTab.findElementByName("ship_upgrades_tab"), EarthUpgrade.Type.SHIP);
        fillHumanityUpgrades(humanityProgressTab.findElementByName("space_upgrades_tab"), EarthUpgrade.Type.SPACE);
        fillHumanityUpgrades(humanityProgressTab.findElementByName("earth_upgrades_tab"), EarthUpgrade.Type.EARTH);
    }

    private int getAmountToAdd(EarthUpgrade.Type currentHumanityProgressTab, int initalAmount) {
        EarthState earthState = world.getPlayer().getEarthState();
        final int currentProgress = earthState.getProgress(currentHumanityProgressTab);
        List<EarthUpgrade> upgrades = EarthUpgrade.getUpgrades(currentHumanityProgressTab);
        final int topUpgradeCost = upgrades.get(upgrades.size() - 1).getValue();
        return Math.min(topUpgradeCost - currentProgress, initalAmount);
    }

    /**
     * Checks if player has discovered all upgrades
     */
    private void checkSingularityAchievement() {
        for (EarthUpgrade.Type t : EarthUpgrade.Type.values()) {
            if (world.getPlayer().getEarthState().getProgress(t) < EarthUpgrade.getMax(t)) {
                return;
            }
        }
        AchievementManager.getInstance().achievementUnlocked(AchievementNames.singularity);
    }

    public void add500() {
        EarthUpgrade.Type currentHumanityProgressTab = getCurrentHumanityProgressTab();
        final EarthState earthState = world.getPlayer().getEarthState();
        int amount = getAmountToAdd(currentHumanityProgressTab, Math.min(500, world.getPlayer().getEarthState().getUndistributedProgress()));
        if (amount <= 0) {
            return;
        }
        earthState.addProgress(world, currentHumanityProgressTab, amount);
        updateHumanityTab();
        // maybe some new modules became available, update the shipyard tab contents
        fillUpgrades();
        checkSingularityAchievement();
    }

    public void addAll() {
        // Add min of all available progress and max possible upgrade cost
        EarthUpgrade.Type currentHumanityProgressTab = getCurrentHumanityProgressTab();
        final EarthState earthState = world.getPlayer().getEarthState();
        int amount = getAmountToAdd(currentHumanityProgressTab, earthState.getUndistributedProgress());
        if (amount <= 0) {
            return;
        }
        earthState.addProgress(world, currentHumanityProgressTab, amount);
        updateHumanityTab();
        fillUpgrades();
        checkSingularityAchievement();
    }

    private EarthUpgrade.Type getCurrentHumanityProgressTab() {
        switch (humanityProgressTab.findNiftyControl("progress_tabs", TabGroup.class).getSelectedTabIndex()) {
            case 0:
                return EarthUpgrade.Type.SHIP;
            case 1:
                return EarthUpgrade.Type.SPACE;
            default:
                return EarthUpgrade.Type.EARTH;
        }
    }

    private void fillHumanityUpgrades(Element tab, EarthUpgrade.Type type) {
        ProgressBarControl progressBarControl = tab.findControl("#progressbar", ProgressBarControl.class);
        int max = EarthUpgrade.getMax(type);
        int current = world.getPlayer().getEarthState().getProgress(type);
        progressBarControl.setProgress((float) current / (float) max);
        progressBarControl.setProgress((float) current / (float) max);
        progressBarControl.setText(String.valueOf(current) + "/" + String.valueOf(max));
        EngineUtils.setTextForGUIElement(tab.findElementByName("#remaining-points-text"),
                String.format(Localization.getText("gui", "progress.remaining_points"), world.getPlayer().getEarthState().getUndistributedProgress()));
        ListBox<EarthUpgrade> listBox = tab.findNiftyControl("#items", ListBox.class);
        listBox.clear();
        listBox.addAllItems(EarthUpgrade.getUpgrades(type));
    }

    private void fillUpgrades() {
        EngineUtils.resetScrollbarX(storageList);
        EngineUtils.resetScrollbarX(inventoryList);
        storageList.clear();
        inventoryList.clear();
        storageMultiset.clear();
        inventoryMultiset.clear();
        storageMultiset.addAll(world.getPlayer().getEarthState().getAvailableUpgrades());
        inventoryMultiset.addAll(world.getPlayer().getShip().getUpgrades());

        storageList.addAllItems(storageMultiset.entrySet());
        inventoryList.addAllItems(inventoryMultiset.entrySet());
    }

    private void fillMessages() {
        ListBox l = messagesList.findNiftyControl("itemsList", ListBox.class);
        l.clear();
        EngineUtils.resetScrollbarX(l);
        List<PrivateMessage> privateMessages = world.getPlayer().getEarthState().getMessages();
        for (ListIterator listIterator = privateMessages.listIterator(privateMessages.size()); listIterator.hasPrevious(); ) {
            l.addItem(listIterator.previous());
        }
    }

    @Override
    public void onEndScreen() {
        world.setPaused(false);
    }

    public void closeScreen() {
        GUI.getInstance().getNifty().gotoScreen("star_system_gui");
        world.getPlayer().getShip().refillCrew(world);
    }

    private void updateShipyardLabels() {
        Element sciCountElement = shipYardTab.findElementByName("sci_count").findElementByName("#count");
        Element engiCountElement = shipYardTab.findElementByName("engi_count").findElementByName("#count");
        Element milCountElement = shipYardTab.findElementByName("mil_count").findElementByName("#count");

        EngineUtils.setTextForGUIElement(sciCountElement, String.valueOf(world.getPlayer().getShip().getMaxScientists()));
        EngineUtils.setTextForGUIElement(engiCountElement, String.valueOf(world.getPlayer().getShip().getMaxEngineers()));
        EngineUtils.setTextForGUIElement(milCountElement, String.valueOf(world.getPlayer().getShip().getMaxMilitary()));

        List<Multiset.Entry<ShipUpgrade>> selected = new ArrayList<>();
        selected.addAll(inventoryList.getSelection());
        selected.addAll(storageList.getSelection());
        if (!selected.isEmpty()) {
            Multiset.Entry<ShipUpgrade> su = selected.get(0);
            EngineUtils.setTextForGUIElement(upgradeText, su.getElement().getLocalizedDescription());
            EngineUtils.setTextForGUIElement(shipYardTab.findElementByName("upgrade_name"), su.getElement().getLocalizedName(su.getElement().getLocalizationGroup()));
            EngineUtils.setImageForGUIElement(upgradeImage, su.getElement().getDrawable().getImage());
        } else {
            EngineUtils.setTextForGUIElement(upgradeText, "");
            EngineUtils.setTextForGUIElement(shipYardTab.findElementByName("upgrade_name"), "");
            EngineUtils.setImageForGUIElement(upgradeImage, "no_image");
        }

        final int freeSpace1 = world.getPlayer().getShip().getFreeSpace();
        freeSpace.setProgress((100 - (float) freeSpace1) / 100.0f);
        freeSpace.setText(Localization.getText("gui", "shipyard.remaining_space") + " " + freeSpace1);
        fillUpgrades();
        GUI.getInstance().getNifty().getCurrentScreen().layoutLayers();
    }

    public void onShipToShipyardClicked() {
        Multiset.Entry<ShipUpgrade> su = inventoryList.getFocusItem();
        if (su.getCount() == 1) {
            inventoryList.removeItemByIndex(inventoryList.getFocusItemIndex());
        }
        inventoryMultiset.remove(su.getElement());
        world.getPlayer().getShip().removeUpgrade(world, su.getElement());
        updateShipyardLabels();
    }

    public void onShipyardToShipClicked() {
        Multiset.Entry<ShipUpgrade> su = storageList.getFocusItem();
        if (su.getElement().getSpace() > world.getPlayer().getShip().getFreeSpace()) {
            return;
        }
        if (!inventoryMultiset.contains(su.getElement())) {
            inventoryList.addItem(su);
        }
        inventoryMultiset.add(su.getElement());
        world.getPlayer().getShip().addUpgrade(world, su.getElement());
        updateShipyardLabels();
    }

    private void deselectAll(ListBox<Multiset.Entry<ShipUpgrade>> l) {
        for (Integer i : l.getSelectedIndices()) {
            l.deselectItemByIndex(i);
        }
    }


    @NiftyEventSubscriber(pattern = ".*storage_to_inventory")
    public void onReleased(String id, ButtonClickedEvent event) {
        storageList.selectItem((Multiset.Entry<ShipUpgrade>) event.getButton().getElement().getParent().getUserData());
        deselectAll(inventoryList);
    }

    @NiftyEventSubscriber(pattern = ".*inventory_to_storage")
    public void onPrimaryReleased(String id, ButtonClickedEvent event) {
        inventoryList.selectItem((Multiset.Entry<ShipUpgrade>) event.getButton().getElement().getParent().getUserData());
        deselectAll(storageList);
    }

    @NiftyEventSubscriber(pattern = ".*List")
    public void onListBoxSelectionChanged(final String id, final ListBoxSelectionChangedEvent event) {
        if (event.getSelectionIndices().isEmpty()) {
            return;
        }

        if (id.equals(inventoryList.getId()) || id.equals(storageList.getId())) {
            ShipUpgrade su = ((Multiset.Entry<ShipUpgrade>) event.getSelection().get(0)).getElement();
            EngineUtils.setTextForGUIElement(upgradeText, su.getLocalizedDescription());
            EngineUtils.setTextForGUIElement(shipYardTab.findElementByName("upgrade_name"), su.getLocalizedName(su.getLocalizationGroup()));
            EngineUtils.setImageForGUIElement(upgradeImage, su.getDrawable().getImage());
            return;
        }


        if (id.equals("itemsList")) {
            Element imagePanel = messagesList.findElementByName("selectedItemImg");

            TextRenderer tr = messagesList.findElementByName("selectedItemText").getRenderer(TextRenderer.class);
            Element detailsText = messagesList.findElementByName("messageDetailsText");
            if (event.getSelection().isEmpty()) {
                tr.setText("<No item selected>");
                EngineUtils.setImageForGUIElement(imagePanel, "no_image");
                EngineUtils.setTextForGUIElement(detailsText, String.format(Localization.getText("private_messages", "message_details_template"), "", ""));
                return;
            }
            PrivateMessage pm = (PrivateMessage) event.getSelection().get(0);
            pm.setRead(true);
            tr.setText(pm.getLocalizedText("private_messages"));
            final Image image = pm.getDrawable().getImage();
            image.setFilter(Image.FILTER_LINEAR);
            EngineUtils.setImageForGUIElement(imagePanel, image);
            EngineUtils.setTextForGUIElement(detailsText, String.format(Localization.getText("private_messages", "message_details_template"), pm.getSender(), pm.getReceivedAt()));

            ScrollPanel scrollPanel = messagesList.findNiftyControl("scrollbarPanelId", ScrollPanel.class);
            scrollPanel.setVerticalPos(0);
            messagesList.layoutElements();
        }

    }
}
