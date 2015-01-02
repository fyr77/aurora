package ru.game.aurora.gui;

import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.NiftyEventSubscriber;
import de.lessvoid.nifty.controls.ListBox;
import de.lessvoid.nifty.controls.ListBoxSelectionChangedEvent;
import de.lessvoid.nifty.controls.listbox.ListBoxControl;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.elements.render.ImageRenderer;
import de.lessvoid.nifty.elements.render.TextRenderer;
import de.lessvoid.nifty.render.NiftyImage;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import de.lessvoid.nifty.slick2d.render.image.ImageSlickRenderImage;
import ru.game.aurora.application.ResourceManager;
import ru.game.aurora.common.Drawable;
import ru.game.aurora.common.ItemWithTextAndImage;
import ru.game.aurora.player.earth.PrivateMessage;
import ru.game.aurora.player.engineering.ShipUpgrade;
import ru.game.aurora.util.EngineUtils;
import ru.game.aurora.world.World;
import ru.game.aurora.world.planet.InventoryItem;

import java.util.ArrayList;
import java.util.List;

public class TradeScreenController implements ScreenController
{

    private World world;

    private ListBox<Multiset.Entry<InventoryItem>> inventoryList;

    private ListBox<Multiset.Entry<InventoryItem>> merchantList;

    private Element itemImage;

    private Element merchantPortrait;

    private Element itemName;

    private Element itemDesc;

    private Element creditCount;

    private Drawable merchantImage;

    private Multiset<InventoryItem> merchantInventory;

    public TradeScreenController(World world) {
        this.world = world;
    }

    public void setMerchantImage(Drawable merchantImage) {
        this.merchantImage = merchantImage;
    }

    @Override
    public void bind(Nifty nifty, Screen screen) {
        inventoryList = screen.findNiftyControl("inventoryList", ListBox.class);
        (((InventoryViewConverter)((ListBoxControl)inventoryList).getViewConverter())).setShowPrice(true);
        merchantList = screen.findNiftyControl("merchantList", ListBox.class);
        (((InventoryViewConverter)((ListBoxControl)merchantList).getViewConverter())).setShowPrice(true);
        itemImage = screen.findElementByName("itemImage");
        itemName = screen.findElementByName("itemName");
        itemDesc = screen.findElementByName("itemDescription");
        merchantPortrait= screen.findElementByName("trader_image");
        creditCount = screen.findElementByName("credits_count").findElementByName("#count");
    }

    private void addWithFilter(ListBox<Multiset.Entry<InventoryItem>> listBox, Multiset<InventoryItem> items)
    {
        for (Multiset.Entry<InventoryItem> e : items.entrySet()) {
            // skip items with price of 0
            if (e.getElement().getPrice() < 0.00001) {
                continue;
            }
            listBox.addItem(e);
        }
    }

    @Override
    public void onStartScreen() {
        world.setPaused(true);
        EngineUtils.setImageForGUIElement(merchantPortrait, merchantImage.getImage());
        EngineUtils.setImageForGUIElement(itemImage, "no_image");
        EngineUtils.setTextForGUIElement(creditCount, String.valueOf(world.getPlayer().getCredits()));
        inventoryList.clear();
        addWithFilter(inventoryList, world.getPlayer().getInventory());

        merchantList.clear();
        addWithFilter(merchantList, merchantInventory);
        GUI.getInstance().getNifty().getCurrentScreen().layoutLayers();
    }

    @Override
    public void onEndScreen() {
        world.setPaused(false);
    }

    public void closeScreen() {
        GUI.getInstance().popAndSetScreen();
    }

    public void onSellClicked() {

    }

    public void onBuyClicked() {

    }


    public static void openTrade(String merchantImage, Multiset<InventoryItem> merchantInventory) {
        GUI.getInstance().pushCurrentScreen();
        final TradeScreenController tradeScreenController = (TradeScreenController) GUI.getInstance().getNifty().findScreenController(TradeScreenController.class.getCanonicalName());
        tradeScreenController.setMerchantImage(new Drawable(merchantImage));
        tradeScreenController.merchantInventory = merchantInventory;
        GUI.getInstance().getNifty().gotoScreen("trade_screen");
    }

    @NiftyEventSubscriber(pattern = ".*List")
    public void onListBoxSelectionChanged(final String id, final ListBoxSelectionChangedEvent event) {
        if (event.getSelectionIndices().isEmpty()) {
            return;
        }

        if (event.getListBox().equals(inventoryList)){
            final List<Integer> selectedIndices = merchantList.getSelectedIndices();
            if (!selectedIndices.isEmpty()) {
                merchantList.deselectItemByIndex(selectedIndices.get(0));
            }
        } else {
            final List<Integer> selectedIndices = inventoryList.getSelectedIndices();
            if (!selectedIndices.isEmpty()) {
                inventoryList.deselectItemByIndex(selectedIndices.get(0));
            }
        }

        Multiset.Entry<InventoryItem> su = (Multiset.Entry<InventoryItem>) event.getSelection().get(0);
        EngineUtils.setTextForGUIElement(itemName, su.getElement().getName());
        if (su.getElement().getDescription() != null) {
            EngineUtils.setTextForGUIElement(itemDesc, su.getElement().getDescription());
        } else {
            EngineUtils.setTextForGUIElement(itemDesc, "");
        }
        EngineUtils.setImageForGUIElement(itemImage, su.getElement().getImage());
    }
}
