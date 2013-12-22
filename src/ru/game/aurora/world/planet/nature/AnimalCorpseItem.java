package ru.game.aurora.world.planet.nature;

import org.newdawn.slick.Image;
import ru.game.aurora.application.GameLogger;
import ru.game.aurora.application.Localization;
import ru.game.aurora.player.research.projects.AnimalResearch;
import ru.game.aurora.world.World;
import ru.game.aurora.world.planet.InventoryItem;

/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 25.10.13
 * Time: 14:18
 * To change this template use File | Settings | File Templates.
 */
public class AnimalCorpseItem implements InventoryItem {

    private static final long serialVersionUID = 1L;

    AnimalSpeciesDesc desc;

    public AnimalCorpseItem(AnimalSpeciesDesc desc) {
        this.desc = desc;
    }

    @Override
    public String getName() {
        return desc.getName();
    }

    @Override
    public Image getImage() {
        return desc.getDeadImage();
    }

    @Override
    public void onReturnToShip(World world, int amount) {
        if (!desc.isOutopsyMade() && !world.getPlayer().getResearchState().containsResearchFor(desc)) {
            // this type of alien animal has never been seen before, add new research
            GameLogger.getInstance().logMessage(Localization.getText("gui", "surface.new_animal_research") + " " + desc.getName());
            world.getPlayer().getResearchState().addNewAvailableProject(new AnimalResearch(desc));
        }
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AnimalCorpseItem that = (AnimalCorpseItem) o;

        return !(desc != null ? !desc.equals(that.desc) : that.desc != null);

    }

    @Override
    public int hashCode() {
        return desc != null ? desc.hashCode() : 0;
    }
}
