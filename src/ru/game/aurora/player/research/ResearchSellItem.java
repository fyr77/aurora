package ru.game.aurora.player.research;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.game.aurora.npc.Faction;
import ru.game.aurora.player.SellOnlyInventoryItem;
import ru.game.aurora.world.World;

/**
 * Research that can be purchased from a trader
 */
public class ResearchSellItem extends SellOnlyInventoryItem
{

    private static final Logger logger = LoggerFactory.getLogger(ResearchSellItem.class);

    public ResearchSellItem(String id, String image, double price, boolean isUnique, String... factionFilter) {
        super("research", id, image, price, isUnique, factionFilter);
    }

    @Override
    public boolean canBeSoldTo(World world, Faction faction) {
        return world.getResearchAndDevelopmentProjects().getResearchProjects().get(id) != null;
    }

    @Override
    public void onReceived(World world, int amount) {
        ResearchProjectDesc desc = world.getResearchAndDevelopmentProjects().getResearchProjects().remove(id);
        if (desc == null) {
            logger.warn("No research with id {}, can not add it after purchasing a sell item", id);
            return;
        }
        logger.info("Adding research " + id);
        world.getPlayer().getResearchState().addNewAvailableProject(desc);
    }
}
