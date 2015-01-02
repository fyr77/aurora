/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 27.12.12
 * Time: 13:30
 */

package ru.game.aurora.world.planet;

import org.newdawn.slick.Image;
import ru.game.aurora.application.GameLogger;
import ru.game.aurora.application.Localization;
import ru.game.aurora.application.ResourceManager;
import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.npc.Faction;
import ru.game.aurora.player.research.projects.ArtifactResearch;
import ru.game.aurora.world.BaseGameObject;
import ru.game.aurora.world.ScanGroup;
import ru.game.aurora.world.World;

public class AlienArtifact extends BaseGameObject {

    private static final long serialVersionUID = 1533202973059805452L;

    private int remainingData = 10;

    private final ArtifactResearch resultResearch;

    // optional dialog is shown when this artifact is used for first time
    private Dialog firstUseDialog = null;

    private final ArtifactSamples samples = new ArtifactSamples();

    public final class ArtifactSamples implements InventoryItem {
        private static final long serialVersionUID = 4883589185683400708L;

        @Override
        public String getName() {
            return Localization.getText("gui", "surface.artifact_samples");
        }

        @Override
        public String getDescription() {
            return null;
        }

        @Override
        public Image getImage() {
            return ResourceManager.getInstance().getImage("artifact_boxes");
        }

        @Override
        public double getPrice() {
            return 1;
        }

        @Override
        public void onReturnToShip(World world, int amount) {
            resultResearch.setSpeedModifier(amount / 10.0); // research speed depends on how many data had been collected
            world.getPlayer().getResearchState().addNewAvailableProject(resultResearch);
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
        public int getWeight() {
            return 0;
        }

        @Override
        public boolean canBeSoldTo(Faction faction) {
            return false;
        }
    }

    public AlienArtifact(int x, int y, String spriteName, ArtifactResearch resultResearch) {
        super(x, y, spriteName);
        this.resultResearch = resultResearch;
    }

    @Override
    public ScanGroup getScanGroup() {
        return ScanGroup.OTHER;
    }

    @Override
    public boolean canBeInteracted() {
        return true;
    }

    @Override
    public boolean canBeAttacked() {
        return false;
    }

    @Override
    public void interact(World world) {
        if (firstUseDialog != null) {
            world.addOverlayWindow(firstUseDialog);
            firstUseDialog = null;
            return;
        }

        if (remainingData <= 0) {
            GameLogger.getInstance().logMessage(Localization.getText("gui", "surface.artifact.already_explored"));
            return;
        }

        final int researchSpeed = world.getPlayer().getLandingParty().calcResearchPower();
        int amount = Math.min(researchSpeed, remainingData);
        world.getPlayer().getLandingParty().pickUp(samples, amount);
        remainingData -= researchSpeed;

        if (remainingData <= 0) {
            GameLogger.getInstance().logMessage(Localization.getText("gui", "surface.artifact.excavated"));
        } else {
            GameLogger.getInstance().logMessage(String.format(Localization.getText("gui", "surface.artifact.progress"), researchSpeed, remainingData));
        }
    }

    @Override
    public String getName() {
        return null;
    }

    public void setFirstUseDialog(Dialog firstUseDialog) {
        this.firstUseDialog = firstUseDialog;
    }
}
