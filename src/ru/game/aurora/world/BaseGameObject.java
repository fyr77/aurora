/**
 * Created with IntelliJ IDEA.
 * User: User
 * Date: 18.07.14
 * Time: 22:19
 */
package ru.game.aurora.world;


import ru.game.aurora.application.GameLogger;
import ru.game.aurora.application.Localization;
import ru.game.aurora.common.Drawable;
import ru.game.aurora.npc.Faction;

public class BaseGameObject extends MovableSprite implements GameObject {
    private static final long serialVersionUID = 3321667334477000255L;

    protected boolean isAlive = true;

    protected String name;

    protected ScanGroup scanGroup;

    protected String scanTextId;

    protected String scanTextBundle;

    protected Faction faction;

    public BaseGameObject() {
        super(0, 0, null);
    }

    public BaseGameObject(int x, int y) {
        super(x, y, null);
    }

    public BaseGameObject(int x, int y, String imageId) {
        this(x, y, new Drawable(imageId));
    }

    public BaseGameObject(int x, int y, Drawable drawable) {
        super(x, y, drawable);
    }

    public BaseGameObject(int x, int y, Drawable d, String name, ScanGroup scanGroup) {
        super(x, y, d);
        this.name = name;
        this.scanGroup = scanGroup;
    }

    @Override
    public boolean canBeAttacked() {
        return false;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean canBeInteracted() {
        return false;
    }

    @Override
    public boolean interact(World world) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getInteractMessage() {
        return Localization.getText("gui", "surface.interact");
    }

    @Override
    public void onAttack(World world, GameObject attacker, int damaged) {
        world.onGameObjectAttacked(attacker, this, damaged);
        GameLogger.getInstance().logMessage(String.format(Localization.getText("gui", "surface.damage_message"), damaged, getName()));
    }

    @Override
    public boolean isAlive() {
        return isAlive;
    }

    @Override
    public String getScanDescription(World world) {
        return Localization.getText(scanTextBundle, scanTextId);
    }

    public void setSprite(String sprite) {
        drawable = new Drawable(sprite);
    }

    public void setScanDescription(String bundle, String id) {
        scanTextBundle = bundle;
        scanTextId = id;
    }

    @Override
    public ScanGroup getScanGroup() {
        return scanGroup;
    }

    @Override
    public Faction getFaction() {
        return faction;
    }

    public void setFaction(Faction faction) {
        this.faction = faction;
    }
}
