/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 05.02.13
 * Time: 14:47
 */
package ru.game.aurora.player.research;


import de.matthiasmann.twl.Widget;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.geom.Rectangle;
import ru.game.aurora.application.Camera;
import ru.game.aurora.application.GUIConstants;
import ru.game.aurora.application.GameLogger;
import ru.game.aurora.application.ResourceManager;
import ru.game.aurora.util.EngineUtils;
import ru.game.aurora.world.OverlayWindow;
import ru.game.aurora.world.World;

public class ResearchReportScreen implements OverlayWindow
{
    private static final long serialVersionUID = 8117170582835644516L;

    private boolean isOver = false;

    private ResearchReport report;

    private ResearchProjectDesc research;

    private static final Rectangle rect = new Rectangle(1, 1, 13, 13);

    public ResearchReportScreen(ResearchProjectDesc research, ResearchReport report)
    {
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
    public Widget getGUI() {
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
        EngineUtils.drawRectWithBorder(graphics, rect, camera, GUIConstants.borderColor, GUIConstants.backgroundColor);
        graphics.drawImage(ResourceManager.getInstance().getImage(report.icon), 5.5f * camera.getTileWidth(), 1.5f * camera.getTileHeight());
        graphics.drawString(research.getName() + " report", 5 * camera.getTileWidth(), 6 * camera.getTileHeight());
        int lines = EngineUtils.drawString(graphics, report.text, 2 * camera.getTileWidth(), 7 * camera.getTileHeight(), 11 * camera.getTileWidth());
        graphics.drawString("Press <enter> to continue", 2 * camera.getTileWidth(), 7 * camera.getTileHeight() + (lines + 1) *  GameLogger.getInstance().getFont().getHeight("l"));
    }
}
