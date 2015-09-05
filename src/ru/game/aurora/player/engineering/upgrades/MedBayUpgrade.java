package ru.game.aurora.player.engineering.upgrades;

import ru.game.aurora.application.Configuration;
import ru.game.aurora.player.engineering.ShipUpgrade;
import ru.game.aurora.world.Ship;
import ru.game.aurora.world.World;

import java.util.List;

public class MedBayUpgrade extends ShipUpgrade {
    
    private static final long serialVersionUID = 1L;
    private static final int reduceCrewDeath = Configuration.getIntProperty("upgrades.medbay.reduceCrewDeath");
    private static final int reduceCrewDeathEnhanced = Configuration.getIntProperty("upgrades.medbay.enhanced.reduceCrewDeath");
    
    private final boolean enhanced;

    public MedBayUpgrade(boolean enhanced) {
        super(
                enhanced ? "medbay_upgrade_enhanced" : "medbay_upgrade", 
                enhanced ? "medbay_module_enhanced" : "medbay_module", 
                "upgrades"
        );
        
        this.enhanced = enhanced;
    }
    
    public MedBayUpgrade() {
        this(false);
    }

    public static int getCrewDeathReduceValue() {
        List<ShipUpgrade> upgrades = World.getWorld().getPlayer().getShip().getUpgrades();

        int reduce = 0;
        for (ShipUpgrade upgrade : upgrades) {
            if (upgrade instanceof MedBayUpgrade) {
                reduce += ((MedBayUpgrade) upgrade).enhanced
                        ? reduceCrewDeathEnhanced : reduceCrewDeath;
            }
        }

        return reduce;
    }

    @Override
    public void onInstalled(World world, Ship ship) {


    }

    @Override
    public void onRemoved(World world, Ship ship) {


    }

    @Override
    public int getSpace() {
        return Configuration.getIntProperty("upgrades.medbay.size");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        MedBayUpgrade that = (MedBayUpgrade) o;

        return enhanced == that.enhanced;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (enhanced ? 1 : 0);
        return result;
    }

    public boolean isEnhanced() {
        return enhanced;
    }
}
