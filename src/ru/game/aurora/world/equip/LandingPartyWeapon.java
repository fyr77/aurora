/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 06.12.12
 * Time: 14:01
 */

package ru.game.aurora.world.equip;

import org.newdawn.slick.Image;
import ru.game.aurora.application.JsonConfigManager;
import ru.game.aurora.application.Localization;
import ru.game.aurora.application.ResourceManager;
import ru.game.aurora.world.World;
import ru.game.aurora.world.planet.InventoryItem;

import java.io.Serializable;

/**
 * Weapon used by landing party.
 * Weapon specifies damage and shooting range
 * Damage is calculated as [party combat strength] * [weapon damage],
 * where combat strength is 1 * number of military + 1/3 * (number of engineers and scientists)
 */
public class LandingPartyWeapon implements Serializable, JsonConfigManager.EntityWithId, InventoryItem {

    private static final long serialVersionUID = 3L;

    private final String id;

    private final int damage;

    private final int range;

    private final String name;

    private final String image;

    private final String shotImage;

    private final String shotSound;

    public LandingPartyWeapon(String id, int damage, int range, String name, String image, String shotImage, String shotSound) {
        this.id = id;
        this.damage = damage;
        this.range = range;
        this.name = name;
        this.image = image;
        this.shotImage = shotImage;
        this.shotSound = shotSound;
    }

    public int getDamage() {
        return damage;
    }

    public int getRange() {
        return range;
    }

    public String getName() {
        return Localization.getText("weapons", name);
    }

    @Override
    public void onReturnToShip(World world, int amount) {
    }

    @Override
    public boolean isDumpable() {
        return false;
    }

    @Override
    public boolean isUsable() {
        return false;
    }

    @Override
    public int getWeight() {
        return 0;
    }

    @Override
    public String getId() {
        return id;
    }

    public Image getImage() {
        return ResourceManager.getInstance().getImage(image);
    }

    public String getShotImage() {
        return shotImage;
    }

    public String getShotSound() {
        return shotSound;
    }

    @Override
    public String toString() {
        return getName() + " " + damage + " DMG, " + range + " RNG";
    }
}
