package ru.game.aurora.gui.niffy;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.controls.Controller;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.input.NiftyInputEvent;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.xml.xpp3.Attributes;
import ru.game.aurora.gui.GUI;
import ru.game.aurora.world.GameObject;
import ru.game.aurora.world.World;
import ru.game.aurora.world.equip.WeaponInstance;

import java.util.Properties;

/**
 * Control that is shown when player attempts to attack a friendly ship
 */
public class FriendlyAttackConfirmationController implements Controller
{
    private static String popupId;

    private World world;

    private GameObject target;

    private WeaponInstance weapon;

    private int dmg;

    public static void open(World world, GameObject target, WeaponInstance weapon, int dmg) {
        final Nifty nifty = GUI.getInstance().getNifty();
        Element target_selection_popup = nifty.createPopup("attack_friendly_popup");
        popupId = target_selection_popup.getId();
        FriendlyAttackConfirmationController controller = target_selection_popup.findControl("attack_friendly_confirm_window", FriendlyAttackConfirmationController.class);
        controller.setData(world, target, weapon, dmg);
        nifty.showPopup(nifty.getCurrentScreen(), popupId, null);
    }

    public void setData(World world, GameObject target, WeaponInstance weapon, int dmg)
    {
        this.world = world;
        this.target = target;
        this.weapon = weapon;
        this.dmg = dmg;
    }

    @Override
    public void bind(Nifty nifty, Screen screen, Element element, Properties properties, Attributes attributes) {

    }

    @Override
    public void init(Properties properties, Attributes attributes) {

    }

    @Override
    public void onStartScreen() {
        world.setPaused(true);
    }

    @Override
    public void onFocus(boolean b) {

    }

    @Override
    public boolean inputEvent(NiftyInputEvent niftyInputEvent) {
        return false;
    }

    public void close()
    {
        world.setPaused(false);
        GUI.getInstance().getNifty().closePopup(popupId);
    }

    public void doAttack()
    {
        close();
        world.getCurrentStarSystem().doFire(world, target, world.getPlayer().getShip(), weapon, dmg);
    }
}
