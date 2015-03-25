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
import de.lessvoid.nifty.controls.ButtonClickedEvent;
import de.lessvoid.nifty.controls.ListBox;
import de.lessvoid.nifty.controls.ListBoxSelectionChangedEvent;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.elements.render.ImageRenderer;
import de.lessvoid.nifty.elements.render.TextRenderer;
import de.lessvoid.nifty.render.NiftyImage;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import de.lessvoid.nifty.slick2d.render.image.ImageSlickRenderImage;
import ru.game.aurora.application.Localization;
import ru.game.aurora.application.ResourceManager;
import ru.game.aurora.gui.niffy.ProgressBarControl;
import ru.game.aurora.player.earth.PrivateMessage;
import ru.game.aurora.player.engineering.ShipUpgrade;
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
            if (event.getSelection().isEmpty()) {
                tr.setText("<No item selected>");
                imagePanel.getRenderer(ImageRenderer.class).setImage(new NiftyImage(GUI.getInstance().getNifty().getRenderEngine(), new ImageSlickRenderImage(ResourceManager.getInstance().getImage("no_image"))));
                return;
            }
            PrivateMessage pm = (PrivateMessage) event.getSelection().get(0);
            tr.setText(pm.getLocalizedText("private_messages"));
            imagePanel.getRenderer(ImageRenderer.class).setImage(new NiftyImage(GUI.getInstance().getNifty().getRenderEngine(), new ImageSlickRenderImage(pm.getDrawable().getImage())));
            messagesList.layoutElements();
        }

    }
}
