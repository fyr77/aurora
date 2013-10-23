/**
 * Created with IntelliJ IDEA.
 * User: User
 * Date: 07.07.13
 * Time: 20:04
 */

package ru.game.aurora.gui;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.NiftyEventSubscriber;
import de.lessvoid.nifty.controls.*;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.elements.render.TextRenderer;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import ru.game.aurora.application.Localization;
import ru.game.aurora.application.ResourceManager;
import ru.game.aurora.util.EngineUtils;
import ru.game.aurora.world.Ship;
import ru.game.aurora.world.World;
import ru.game.aurora.world.equip.LandingPartyWeapon;
import ru.game.aurora.world.planet.InventoryItem;
import ru.game.aurora.world.planet.LandingParty;


public class LandingPartyEquipScreenController implements ScreenController {
    private World world;

    private LandingParty landingParty;

    private Screen myScreen;

    private Element myWindow;

    public LandingPartyEquipScreenController(World world) {
        this.world = world;
        landingParty = world.getPlayer().getLandingParty();
        if (landingParty == null) {
            landingParty = new LandingParty(0, 0, ResourceManager.getInstance().getLandingPartyWeapons().getEntity("assault"), 1, 1, 1);
            world.getPlayer().setLandingParty(landingParty);
        }
    }

    @Override
    public void bind(Nifty nifty, Screen screen) {
        myScreen = screen;
        myWindow = myScreen.findElementByName("equip_window");
    }

    @Override
    public void onStartScreen() {
        myWindow.setVisible(true);
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
    }

    private void updateLabels() {
        myScreen.findElementByName("scientists_count_text").getRenderer(TextRenderer.class).setText(Localization.getText("gui", "landing_party.scientists") + " " + landingParty.getScience());
        myScreen.findElementByName("engineers_count_text").getRenderer(TextRenderer.class).setText(Localization.getText("gui", "landing_party.engineers") + " " + landingParty.getEngineers());
        myScreen.findElementByName("military_count_text").getRenderer(TextRenderer.class).setText(Localization.getText("gui", "landing_party.military") + " " + landingParty.getMilitary());
        myScreen.findElementByName("total_count").getRenderer(TextRenderer.class).setText(Localization.getText("gui", "landing_party.total") + " " + landingParty.getTotalMembers() + " / 10");

    }

    @Override
    public void onEndScreen() {
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
        updateLabels();
    }

    public void closeScreen() {
        GUI.getInstance().popAndSetScreen();
    }

    @NiftyEventSubscriber(id = "equip_window")
    public void onClose(final String id, final WindowClosedEvent event) {
        closeScreen();
    }
}
