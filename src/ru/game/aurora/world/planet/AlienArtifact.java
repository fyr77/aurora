/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 27.12.12
 * Time: 13:30
 */

package ru.game.aurora.world.planet;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import ru.game.aurora.application.Camera;
import ru.game.aurora.application.GameLogger;
import ru.game.aurora.application.Localization;
import ru.game.aurora.application.ResourceManager;
import ru.game.aurora.player.research.projects.ArtifactResearch;
import ru.game.aurora.world.BasePositionable;
import ru.game.aurora.world.World;

public class AlienArtifact extends BasePositionable implements PlanetObject
{

    private static final long serialVersionUID = 1533202973059805452L;
    private String spriteName;

    private int remainingData = 10;

    private ArtifactResearch resultResearch;

    private ArtifactSamples samples = new ArtifactSamples();

    public final class ArtifactSamples implements InventoryItem
    {
        private static final long serialVersionUID = 4883589185683400708L;

        @Override
        public String getName() {
            return Localization.getText("gui", "surface.artifact_samples");
        }

        @Override
        public Image getImage() {
            return ResourceManager.getInstance().getImage("artifact_boxes");
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
    }

    public AlienArtifact(int x, int y, String spriteName, ArtifactResearch resultResearch) {
        super(x, y);
        this.spriteName = spriteName;
        this.resultResearch = resultResearch;
    }

    @Override
    public boolean canBePickedUp() {
        return true;
    }

    @Override
    public boolean canBeShotAt() {
        return false;
    }

    @Override
    public void onShotAt(int damage) {

    }

    @Override
    public void onPickedUp(World world) {
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
    public boolean isAlive() {
        return true;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public void printStatusInfo() {
        if (remainingData > 0) {
            // GameLogger.getInstance().addStatusMessage("Press <enter> to examine artifact");
        }
    }

    @Override
    public void update(GameContainer container, World world) {

    }

    @Override
    public void draw(GameContainer container, Graphics g, Camera camera) {
        g.drawImage(ResourceManager.getInstance().getImage(spriteName), camera.getXCoord(x), camera.getYCoord(y));
    }

}
