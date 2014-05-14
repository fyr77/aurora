/**
 * Created with IntelliJ IDEA.
 * User: User
 * Date: 13.06.13
 * Time: 22:02
 */
package ru.game.aurora.gui;


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
import ru.game.aurora.application.ResourceManager;
import ru.game.aurora.player.earth.PrivateMessage;
import ru.game.aurora.player.engineering.ShipUpgrade;
import ru.game.aurora.util.EngineUtils;
import ru.game.aurora.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class EarthScreenController implements ScreenController {

    private World world;

    private Element messagesList;

    private ListBox<ShipUpgrade> storageList;

    private ListBox<ShipUpgrade> inventoryList;

    private Element shipYardTab;

    private Element upgradeImage;

    private Element upgradeText;


    public EarthScreenController(World world) {
        this.world = world;
    }

    @Override
    public void bind(Nifty nifty, Screen screen) {
        messagesList = screen.findElementByName("messages_list");
        shipYardTab = screen.findElementByName("shipyard");
        storageList = shipYardTab.findNiftyControl("storageList", ListBox.class);
        inventoryList = shipYardTab.findNiftyControl("inventoryList", ListBox.class);
        upgradeText = shipYardTab.findElementByName("upgrade_text");
        upgradeImage = shipYardTab.findElementByName("upgrade_icon");
    }

    @Override
    public void onStartScreen() {
        fillMessages();
        fillUpgrades();
        world.setPaused(true);
        updateShipyardLabels();
    }

    private void fillUpgrades() {
        storageList.clear();
        inventoryList.clear();

        storageList.addAllItems(new ArrayList<>(world.getPlayer().getEarthState().getAvailableUpgrades()));
        inventoryList.addAllItems(world.getPlayer().getShip().getUpgrades());
    }

    private void fillMessages() {
        ListBox l = messagesList.findNiftyControl("itemsList", ListBox.class);
        l.clear();
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

    private void updateShipyardLabels()
    {
        Element sciCountElement = shipYardTab.findElementByName("sci_count").findElementByName("#count");
        Element engiCountElement = shipYardTab.findElementByName("engi_count").findElementByName("#count");
        Element milCountElement = shipYardTab.findElementByName("mil_count").findElementByName("#count");

        EngineUtils.setTextForGUIElement(sciCountElement, String.valueOf(world.getPlayer().getShip().getMaxScientists()));
        EngineUtils.setTextForGUIElement(engiCountElement, String.valueOf(world.getPlayer().getShip().getMaxEngineers()));
        EngineUtils.setTextForGUIElement(milCountElement, String.valueOf(world.getPlayer().getShip().getMaxMilitary()));

        List<ShipUpgrade> selected = new ArrayList<>();
        selected.addAll(inventoryList.getSelection());
        selected.addAll(storageList.getSelection());
        if (!selected.isEmpty()) {
            ShipUpgrade su = selected.get(0);
            EngineUtils.setTextForGUIElement(upgradeText, su.getLocalizedText(su.getLocalizationGroup()));
            EngineUtils.setTextForGUIElement(shipYardTab.findElementByName("upgrade_name"), su.getLocalizedName(su.getLocalizationGroup()));
            EngineUtils.setImageForGUIElement(upgradeImage, su.getIcon());
        } else {
            EngineUtils.setTextForGUIElement(upgradeText, "");
            EngineUtils.setTextForGUIElement(shipYardTab.findElementByName("upgrade_name"), "");
            EngineUtils.setImageForGUIElement(upgradeImage, "no_image");
        }
    }

    public void onShipToShipyardClicked()
    {
        ShipUpgrade su = inventoryList.getFocusItem();
        inventoryList.removeItemByIndex(inventoryList.getFocusItemIndex());
        world.getPlayer().getShip().removeUpgrade(world, su);
        updateShipyardLabels();
    }

    public void onShipyardToShipClicked()
    {
        ShipUpgrade su = storageList.getFocusItem();
        if (su.getSpace() > world.getPlayer().getShip().getFreeSpace()) {
            return;
        }
        inventoryList.addItem(su);
        world.getPlayer().getShip().addUpgrade(world, su);
        updateShipyardLabels();
    }

    private void deselectAll(ListBox<ShipUpgrade> l)
    {
        for (Integer i : l.getSelectedIndices()) {
            l.deselectItemByIndex(i);
        }
    }


    //это - очень сильное колдунство. onClicked занят ниже. Поэтому тут - onReleased. Костыль
    @NiftyEventSubscriber(pattern = ".*storage_to_inventory")
    public void onReleased(String id, ButtonClickedEvent event) {
        int numericId = Integer.parseInt(id.split("#")[0]);
        ListBox itemsList = storageList;
        numericId -= Integer.parseInt(itemsList.getElement().findElementByName("#child-root").getElements().get(0).getId());
        itemsList.setFocusItemByIndex(numericId);
        itemsList.selectItemByIndex(numericId);
        deselectAll(inventoryList);
    }

    //костыль к костылю. YO DAWG
    @NiftyEventSubscriber(pattern = ".*inventory_to_storage")
    public void onPrimaryReleased(String id, ButtonClickedEvent event) {
        int numericId = Integer.parseInt(id.split("#")[0]);
        ListBox itemsList = inventoryList;
        numericId -= Integer.parseInt(itemsList.getElement().findElementByName("#child-root").getElements().get(0).getId());
        itemsList.setFocusItemByIndex(numericId);
        itemsList.selectItemByIndex(numericId);
        deselectAll(storageList);
    }

    @NiftyEventSubscriber(pattern = ".*List")
    public void onListBoxSelectionChanged(final String id, final ListBoxSelectionChangedEvent event) {
        if (event.getSelectionIndices().isEmpty()) {
            return;
        }

        if (id.equals(inventoryList.getId()) || id.equals(storageList.getId())) {
            ShipUpgrade su = (ShipUpgrade) event.getSelection().get(0);
            EngineUtils.setTextForGUIElement(upgradeText, su.getLocalizedText(su.getLocalizationGroup()));
            EngineUtils.setTextForGUIElement(shipYardTab.findElementByName("upgrade_name"), su.getLocalizedName(su.getLocalizationGroup()));
            EngineUtils.setImageForGUIElement(upgradeImage, su.getIcon());
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
            imagePanel.getRenderer(ImageRenderer.class).setImage(new NiftyImage(GUI.getInstance().getNifty().getRenderEngine(), new ImageSlickRenderImage(ResourceManager.getInstance().getImage(pm.getIcon()))));
            messagesList.layoutElements();
        }

    }
}
