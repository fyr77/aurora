package ru.game.aurora.player.earth;

import ru.game.aurora.application.JsonConfigManager;
import ru.game.aurora.common.Drawable;
import ru.game.aurora.common.ItemWithTextAndImage;
import ru.game.aurora.world.World;

import java.util.*;

/**
 * Base class for humanity upgrades
 */
public class EarthUpgrade extends ItemWithTextAndImage implements Comparable<EarthUpgrade>
{

    public static final long serialVersionUID = 1L;
    private static Map<Type, List<EarthUpgrade>> upgrades;

    static {
        JsonConfigManager<EarthUpgrade> m = new JsonConfigManager<>(EarthUpgrade.class, "resources/items/earth_upgrades");
        upgrades = new HashMap<>();
        upgrades.put(Type.EARTH, new ArrayList<EarthUpgrade>());
        upgrades.put(Type.SHIP, new ArrayList<EarthUpgrade>());
        upgrades.put(Type.SPACE, new ArrayList<EarthUpgrade>());
        for (EarthUpgrade e : m.getEntities().values()) {
            upgrades.get(e.type).add(e);
        }

        Collections.sort(upgrades.get(Type.EARTH));
        Collections.sort(upgrades.get(Type.SHIP));
        Collections.sort(upgrades.get(Type.SPACE));
    }

    protected int value;
    protected Type type;
    protected boolean used = false;
    protected boolean unlocked = false;

    public EarthUpgrade() {
        super(null, null);
    }

    public EarthUpgrade(String id, Drawable drawable, int value) {
        super(id, drawable);
        this.value = value;
    }

    public static List<EarthUpgrade> getUpgrades(Type t) {
        return upgrades.get(t);
    }

    public static int getMax(Type t) {
        final List<EarthUpgrade> earthUpgrades = upgrades.get(t);
        return earthUpgrades.get(earthUpgrades.size() - 1).getValue();
    }

    public int getValue() {
        return value;
    }

    public void unlock(World world) {
        unlocked = true;
    }

    public boolean canBeUsed()
    {
        return false;
    }

    /**
     * This upgrade price has been reached and it is applied
     * @param world Game state
     * @param variant If this upgrade had variants (getVariants() returned non-null) then this is the index of a selected variant
     */
    public void use(World world, int variant)
    {
        used = true;
    }

    public Type getType() {
        return type;
    }

    @Override
    public int compareTo(EarthUpgrade o) {
        return Integer.compare(value, o.value);
    }

    /**
     * Upgrade can has more than one variant where player needs to change one
     */
    public String[] getVariants()
    {
        return null;
    }

    public boolean isUsed() {
        return used;
    }

    public boolean isUnlocked() {
        return unlocked;
    }

    public enum Type {
        SHIP,
        EARTH,
        SPACE
    }
}
