/**
 * Created with IntelliJ IDEA.
 * User: User
 * Date: 07.07.13
 * Time: 20:04
 */

package ru.game.aurora.gui;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.NiftyEventSubscriber;
import de.lessvoid.nifty.controls.ScrollbarChangedEvent;
import de.lessvoid.nifty.elements.render.TextRenderer;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import ru.game.aurora.world.World;
import ru.game.aurora.world.equip.LandingPartyWeapon;
import ru.game.aurora.world.planet.LandingParty;


public class LandingPartyEquipScreenController implements ScreenController {
    private World world;

    private LandingParty landingParty;

    private Screen myScreen;

    public LandingPartyEquipScreenController(World world) {
        this.world = world;
        landingParty = world.getPlayer().getLandingParty();
        if (landingParty == null) {
            landingParty = new LandingParty(0, 0, new LandingPartyWeapon(1, 3, "Assault rifles"), 1, 1, 1);
            world.getPlayer().setLandingParty(landingParty);
        }
    }

    @Override
    public void bind(Nifty nifty, Screen screen) {
        myScreen = screen;
    }

    @Override
    public void onStartScreen() {
    }

    @Override
    public void onEndScreen() {
    }

    @NiftyEventSubscriber(pattern = ".*_count")
    public void onScrollbarMoved(final String id, final ScrollbarChangedEvent event) {
        String scrollbarId = event.getScrollbar().getId();
        switch (scrollbarId) {
            case "scientists_count": {
                int oldVal = landingParty.getScience();
                landingParty.setScience((int) event.getValue());
                if (landingParty.getTotalMembers() > 10) {
                    event.getScrollbar().setValue(oldVal);
                }
                break;
            }
            case "engineers_count": {
                int oldVal = landingParty.getEngineers();
                landingParty.setEngineers((int) event.getValue());
                if (landingParty.getTotalMembers() > 10) {
                    event.getScrollbar().setValue(oldVal);
                }
                break;
            }
            default:
                int oldVal = landingParty.getMilitary();
                landingParty.setMilitary((int) event.getValue());
                if (landingParty.getTotalMembers() > 10) {
                    event.getScrollbar().setValue(oldVal);
                }
                break;
        }

        myScreen.findElementByName("total_count").getRenderer(TextRenderer.class).setText("Total: " + landingParty.getTotalMembers() + " / 10");
    }

    public void closeScreen() {
        GUI.getInstance().popAndSetScreen();
    }
}
