/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 05.02.13
 * Time: 14:47
 */
package ru.game.aurora.player.research;


import de.lessvoid.nifty.screen.Screen;
import org.newdawn.slick.*;
import org.newdawn.slick.geom.Rectangle;
import ru.game.aurora.application.Camera;
import ru.game.aurora.application.GUIConstants;
import ru.game.aurora.application.GameLogger;
import ru.game.aurora.application.ResourceManager;
import ru.game.aurora.util.EngineUtils;
import ru.game.aurora.world.OverlayWindow;
import ru.game.aurora.world.World;

public class ResearchReportScreen implements OverlayWindow {
    private static final long serialVersionUID = 8117170582835644516L;

    private boolean isOver = false;

    private ResearchReport report;

    private ResearchProjectDesc research;

    private static final Rectangle rect = new Rectangle(1, 1, 13, 13);

    public ResearchReportScreen(ResearchProjectDesc research, ResearchReport report) {
        this.research = research;
        this.report = report;
    }

    @Override
    public boolean isOver() {
        return isOver;
    }

    @Override
    public void enter(World world) {

    }

    @Override
    public Screen getGUI() {
        return null;
    }

    @Override
    public void update(GameContainer container, World world) {
        if (container.getInput().isKeyPressed(Input.KEY_ENTER) || container.getInput().isKeyPressed(Input.KEY_ESCAPE)) {
            isOver = true;
        }
    }

    @Override
    public void draw(GameContainer container, Graphics graphics, Camera camera) {

        final Font textFont = GUIConstants.dialogFont;
        final Font captionFont = GameLogger.getInstance().getFont();

        EngineUtils.drawRectWithBorder(graphics, rect, camera, GUIConstants.borderColor, GUIConstants.backgroundColor);


        final Image image = ResourceManager.getInstance().getImage(report.icon);
        final float scale = Math.min(256.0f / image.getWidth(), 256.0f / image.getHeight());
        image.draw(5.5f * camera.getTileWidth(), 1.5f * camera.getTileHeight(), scale);

        graphics.setFont(captionFont);
        graphics.drawString(research.getName() + " report", 5 * camera.getTileWidth(), 6 * camera.getTileHeight());

        int lines = EngineUtils.drawString(graphics, report.text, 2 * camera.getTileWidth(), 7 * camera.getTileHeight(), 11 * camera.getTileWidth(), textFont, Color.white);

        if (research.getMakesAvailable() != null && research.getMakesAvailable().size() > 0) {
            lines++;
            graphics.setFont(captionFont);
            graphics.drawString("New research available: ", 2 * camera.getTileWidth(), 7 * camera.getTileHeight() + (lines + 1) * textFont.getHeight("l"));
            graphics.setFont(textFont);
            lines++;
            for (ResearchProjectDesc newResearch : research.getMakesAvailable()) {
                lines++;
                graphics.drawString(newResearch.getName(), 2.5f * camera.getTileWidth(), 7 * camera.getTileHeight() + (lines + 1) * textFont.getHeight("l"));
            }
        }
        lines += 2;
        graphics.drawString("Press <enter> to continue", 2 * camera.getTileWidth(), 7 * camera.getTileHeight() + (lines + 1) * textFont.getHeight("l"));
    }
}
