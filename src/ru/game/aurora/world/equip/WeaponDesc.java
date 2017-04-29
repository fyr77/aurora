/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 06.12.12
 * Time: 14:01
 */

package ru.game.aurora.world.equip;

import org.newdawn.slick.Image;
import ru.game.aurora.application.Camera;
import ru.game.aurora.application.CommonRandom;
import ru.game.aurora.application.JsonConfigManager;
import ru.game.aurora.common.Drawable;
import ru.game.aurora.common.ItemWithTextAndImage;
import ru.game.aurora.effects.BlasterShotEffect;
import ru.game.aurora.effects.Effect;
import ru.game.aurora.npc.Faction;
import ru.game.aurora.world.GameObject;
import ru.game.aurora.world.ITileMap;
import ru.game.aurora.world.Positionable;
import ru.game.aurora.world.World;
import ru.game.aurora.world.planet.InventoryItem;

import java.io.Serializable;

/**
 * Weapon used by landing party.
 * Weapon specifies damage and shooting range
 * Damage is calculated as [party combat strength] * [weapon damage],
 * where combat strength is 1 * number of military + 1/3 * (number of engineers and scientists)
 *
 * for v0.6.0:
 * 1) damage without deviation = damage
 * 2) damage with deviation = [rand(damage - damage*deviation; damage + damage*deviation)]
 * this calculation method for support backward compatibility with old versions
 */
// todo: need change to simple [damageMin; damageMax] params
public class WeaponDesc extends ItemWithTextAndImage implements Serializable, JsonConfigManager.EntityWithId, InventoryItem {

    private static final long serialVersionUID = 3L;
    public final String shotSound;
    public final String explosionAnimation;
    public final String particlesAnimation;
    public final int size;
    private final int damage;
    private final float damageDeviation;
    private final int range;
    private final int price;
    private final String shotImage;
    private final int reloadTurns;

    public WeaponDesc(String id, Drawable drawable, int damage, float damageDeviation, int range, int price, String shotImage, String shotSound, int reloadTurns, String explosionAnimation, String particlesAnimation, int size) {
        super(id, drawable);
        this.damage = damage;
        this.damageDeviation = damageDeviation;
        this.range = range;
        this.price = price;
        this.shotImage = shotImage;
        this.shotSound = shotSound;
        this.reloadTurns = reloadTurns;
        this.explosionAnimation = explosionAnimation;
        this.particlesAnimation = particlesAnimation;
        this.size = size;
    }

    public int getDamage(){
        return damage;
    }

    public int getDeviationDamage() {
        final float min = getDamageMin();
        final float max = getDamageMax();
        return Math.round(CommonRandom.getRandom().nextFloat() * (max - min) + min);
    }

    public String getDamageInfo(){
        if(Float.compare(damageDeviation, 0.0f) == 0){
            return Integer.toString(damage);
        }
        else{
            if(Float.compare(Math.round(getDamageMin()), Math.round(getDamageMax())) == 0){
                return Integer.toString(damage);
            }
            else{
                return Math.round(getDamageMin()) + "-" + Math.round(getDamageMax());
            }
        }
    }

    private float getDamageMin(){
        return Math.min(damage - damage*damageDeviation, damage + damageDeviation*damageDeviation);
    }
    private float getDamageMax(){
        return Math.max(damage - damage*damageDeviation, damage + damageDeviation*damageDeviation);
    }

    protected float getDamageDeviation(){
        return damageDeviation;
    }

    public int getRange() {
        return range;
    }

    public boolean canTargetEmptySpace() {
        return false;
    }

    @Override
    public void onReceived(World world, int amount) {

    }

    public Effect createShotEffect(World world, GameObject shooter, GameObject target, Camera camera, int moveSpeed)
    {
        return new BlasterShotEffect(shooter, target, camera, moveSpeed, this);
    }

    public Effect createShotEffect(World world, GameObject shooter, Positionable source, int targetTileX, int targetTileY, Camera camera, int moveSpeed, ITileMap map){
        return new BlasterShotEffect(source, targetTileX, targetTileY, camera, moveSpeed, this, map);
    }

    public Effect createShotEffect(World world, GameObject shooter, Positionable source, float targetScreenX, float targetScreenY, Camera camera, int moveSpeed, ITileMap map) {
        return new BlasterShotEffect(source, targetScreenX, targetScreenY, camera, moveSpeed, this, map);
    }

    @Override
    public void onLost(World world, int amount) {

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
    public boolean isUnique() {
        return false;
    }

    @Override
    public boolean isVisibleInInventory() {
        return false;
    }

    @Override
    public int getWeight() {
        return size;
    }

    @Override
    public boolean canBeSoldTo(World world, Faction faction) {
        return false;
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

    @Override
    public String getName() {
        return getLocalizedName("weapons");
    }

    @Override
    public String getDescription() {
        return getLocalizedText("weapons");
    }

    @Override
    public Image getImage() {
        return drawable.getImage();
    }

    @Override
    public double getPrice() {
        return price;
    }

    public int getReloadTurns() {
        return reloadTurns;
    }

}
