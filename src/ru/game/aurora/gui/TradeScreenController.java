package ru.game.aurora.gui;

import com.google.common.collect.Multiset;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.NiftyEventSubscriber;
import de.lessvoid.nifty.controls.ButtonClickedEvent;
import de.lessvoid.nifty.controls.ListBox;
import de.lessvoid.nifty.controls.ListBoxSelectionChangedEvent;
import de.lessvoid.nifty.controls.listbox.ListBoxControl;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Input;
import ru.game.aurora.application.InputBinding;
import ru.game.aurora.common.Drawable;
import ru.game.aurora.npc.Faction;
import ru.game.aurora.player.Resources;
import ru.game.aurora.util.EngineUtils;
import ru.game.aurora.world.Updatable;
import ru.game.aurora.world.World;
import ru.game.aurora.world.planet.InventoryItem;

import java.util.List;

public class TradeScreenController extends DefaultCloseableScreenController {

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

    private Faction merchantFaction;

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
            if (!e.getElement().isVisibleInInventory()) {
                continue;
            }
            // skip items with price of 0
            if (e.getElement().getPrice() < 0.00001) {
                continue;
            }
            if (!e.getElement().canBeSoldTo(world, merchantFaction)) {
                continue;
            }

            if (e.getElement().isUnique() && world.getPlayer().getUniqueItemsPurchased().contains(e.getElement().getId())) {
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
        updateLists();
    }

    private void updateLists() {
        inventoryList.clear();
        addWithFilter(inventoryList, world.getPlayer().getInventory());

        merchantList.clear();
        addWithFilter(merchantList, merchantInventory);
        EngineUtils.setTextForGUIElement(creditCount, String.valueOf(world.getPlayer().getCredits()));

        GUI.getInstance().getNifty().getCurrentScreen().layoutLayers();
    }

    @Override
    public void onEndScreen() {
        world.setPaused(false);
    }

    public void onSellClicked() {
        Multiset.Entry<InventoryItem> entry = inventoryList.getSelection().get(0);
        int amountToSell = 1;
        if (entry.getElement().getPrice() < 1) {
            // sold by stack
            amountToSell = entry.getCount();
        }

        entry.getElement().onLost(world, amountToSell);
        world.getPlayer().changeResource(world, Resources.CREDITS, (int) (amountToSell * entry.getElement().getPrice()));
        updateLists();
    }

    public void onBuyClicked() {
        Multiset.Entry<InventoryItem> entry = merchantList.getSelection().get(0);
        int amountToBuy = 1;
        if (entry.getElement().getPrice() < 1) {
            // sold by stack
            amountToBuy = entry.getCount();
        }

        final int totalPrice = (int) (amountToBuy * entry.getElement().getPrice());
        if (world.getPlayer().getCredits() < totalPrice) {
            return;
        }

        entry.getElement().onReceived(world, amountToBuy);
        world.getPlayer().changeResource(world, Resources.CREDITS, -(int) (amountToBuy * entry.getElement().getPrice()));
        world.getPlayer().getUniqueItemsPurchased().add(entry.getElement().getId());
        updateLists();
    }


    public static void openTrade(String merchantImage, Multiset<InventoryItem> merchantInventory, Faction merchantFaction) {
        GUI.getInstance().pushScreen("star_system_gui");
        final TradeScreenController tradeScreenController = (TradeScreenController) GUI.getInstance().getNifty().findScreenController(TradeScreenController.class.getCanonicalName());
        tradeScreenController.setMerchantImage(new Drawable(merchantImage));
        tradeScreenController.merchantInventory = merchantInventory;
        tradeScreenController.merchantFaction = merchantFaction;

        //hack: stoopid nifty-gui can ignore call to goToScreen(), as dialog screen has not yet closed
        GUI.getInstance().goToScreen("trade_screen");
    }

    public static void openTrade(String merchantImage, Multiset<InventoryItem> merchantInventory) {
        openTrade(merchantImage, merchantInventory, null);
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


    @NiftyEventSubscriber(pattern = ".*storage_to_inventory")
    public void onReleased(String id, ButtonClickedEvent event) {
        merchantList.selectItem((Multiset.Entry<InventoryItem>) event.getButton().getElement().getParent().getUserData());
    }

    @NiftyEventSubscriber(pattern = ".*inventory_to_storage")
    public void onPrimaryReleased(String id, ButtonClickedEvent event) {
        inventoryList.selectItem((Multiset.Entry<InventoryItem>) event.getButton().getElement().getParent().getUserData());
    }
}
