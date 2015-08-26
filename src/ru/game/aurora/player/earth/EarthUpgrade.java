package ru.game.aurora.player.earth;

import com.sun.istack.internal.NotNull;
import ru.game.aurora.application.JsonConfigManager;
import ru.game.aurora.common.Drawable;
import ru.game.aurora.common.ItemWithTextAndImage;
import ru.game.aurora.player.engineering.ShipUpgrade;
import ru.game.aurora.world.World;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Base class for humanity upgrades
 */
public abstract class EarthUpgrade extends ItemWithTextAndImage implements Comparable<EarthUpgrade>
{

    public enum Type
    {
        SHIP,
        EARTH,
        SPACE
    }

    private static Map<Type, List<EarthUpgrade>> upgrades;

    static {
        JsonConfigManager<EarthUpgrade> m = new JsonConfigManager<>(EarthUpgrade.class, "items/earth_upgrades");
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

    public static final long serialVersionUID = 1L;

    protected int value;

    protected Type type;

    public EarthUpgrade(String id, Drawable drawable, int value) {
        super(id, drawable);
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public abstract void unlock(World world);

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
        // nothing
    }

    public Type getType() {
        return type;
    }

    @Override
    public int compareTo(EarthUpgrade o) {
        return Integer.compare(value, o.value);
    }

    public static List<EarthUpgrade> getUpgrades(Type t) {
        return upgrades.get(t);
    }

    /**
     * Upgrade can has more than one variant where player needs to change one
     */
    public String[] getVariants()
    {
        return null;
    }


}
