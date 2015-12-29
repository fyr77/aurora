package ru.game.aurora.world.generation.quest.heritage;

import org.newdawn.slick.Image;
import ru.game.aurora.application.Localization;
import ru.game.aurora.application.ResourceManager;
import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.npc.Faction;
import ru.game.aurora.world.BaseGameObject;
import ru.game.aurora.world.GameEventListener;
import ru.game.aurora.world.World;
import ru.game.aurora.world.planet.InventoryItem;
import ru.game.aurora.world.planet.Planet;

/**
 * Collectable body of a dead klisk mutant
 */
public class KliskMutantCorpseItem extends BaseGameObject implements InventoryItem {

    public KliskMutantCorpseItem(int x, int y) {
        super(x, y, "klisk_mutant_dead");
    }

    @Override
    public String getId() {
        return "klisk_mutant_dead";
    }

    @Override
    public String getName() {
        return Localization.getText("journal", "heritage.corpse_name");
    }

    @Override
    public String getDescription() {
        return Localization.getText("journal", "heritage.corpse_desc");
    }

    @Override
    public Image getImage() {
        return ResourceManager.getInstance().getImage("klisk_mutant_dead");
    }

    @Override
    public double getPrice() {
        return 0;
    }

    @Override
    public void onReceived(World world, int amount) {
        if (!world.getGlobalVariables().containsKey("heritage.monster_collected")) {
            world.addListener(new GameEventListener() {
                @Override
                public boolean onPlayerLeftPlanet(World world, Planet planet) {
                    world.addOverlayWindow(Dialog.loadFromFile("dialogs/encounters/heritage/heritage_gordon.json"));
                    isAlive = false;
                    return true;
                }
            });

            world.getGlobalVariables().put("heritage.monster_collected", true);
        }

        int count = (int) world.getGlobalVariable("heritage.monsters_killed", 0);
        switch (count) {
            case 1:
                world.getGlobalVariables().put("heritage.first_monster_killed", true);
                break;
            case 2:
                world.getGlobalVariables().put("heritage.second_monster_killed", true);
                break;
            case 3:
                world.getGlobalVariables().put("heritage.third_monster_killed", true);
                break;
            case 4:
                world.getGlobalVariables().put("heritage.fourth_monster_killed", true);
                break;
        }
    }

    @Override
    public void onLost(World world, int amount) {

    }

    @Override
    public boolean isDumpable() {
        return true;
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
    public int getWeight() {
        return 1;
    }

    @Override
    public boolean canBeSoldTo(World world, Faction faction) {
        return false;
    }

    @Override
    public boolean canBeInteracted(World world) {
        return true;
    }

    @Override
    public boolean interact(World world) {
        world.getPlayer().getLandingParty().getInventory().add(this);
        isAlive = false;
        return true;
    }
}
