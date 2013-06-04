/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 27.12.12
 * Time: 13:30
 */

package ru.game.aurora.world.planet;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import ru.game.aurora.application.Camera;
import ru.game.aurora.application.GameLogger;
import ru.game.aurora.application.ResourceManager;
import ru.game.aurora.player.research.projects.ArtifactResearch;
import ru.game.aurora.world.BasePositionable;
import ru.game.aurora.world.World;

public class AlienArtifact extends BasePositionable implements PlanetObject {

    private String spriteName;

    private int remainingData = 10;

    private ArtifactResearch resultResearch;

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
            GameLogger.getInstance().logMessage("This artifact is already explored");
            return;
        }

        final int researchSpeed = world.getPlayer().getLandingParty().calcResearchPower();
        remainingData -= researchSpeed;
        if (remainingData <= 0) {
            GameLogger.getInstance().logMessage("Recovered all data from this artifact. Added research project.");
            world.getPlayer().getResearchState().addNewAvailableProject(resultResearch);
        } else {
            GameLogger.getInstance().logMessage(String.format("Excavated %d data from object, %d work remaining", researchSpeed, remainingData));
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
            GameLogger.getInstance().addStatusMessage("Press <enter> to examine artifact");
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
