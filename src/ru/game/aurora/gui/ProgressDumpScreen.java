package ru.game.aurora.gui;

import de.lessvoid.nifty.screen.Screen;
import org.newdawn.slick.*;
import org.newdawn.slick.geom.Rectangle;
import ru.game.aurora.application.Camera;
import ru.game.aurora.application.GUIConstants;
import ru.game.aurora.player.research.ResearchProjectDesc;
import ru.game.aurora.player.research.ResearchState;
import ru.game.aurora.util.EngineUtils;
import ru.game.aurora.world.OverlayWindow;
import ru.game.aurora.world.World;

//todo: add scrolling
public class ProgressDumpScreen implements OverlayWindow {
    private static final long serialVersionUID = 421626860785455868L;

    private ResearchState researchState;

    private final int lostCrewMembers;

    private boolean allDrawn = false;

    private int maxIdx = 0;

    private long lastTime;

    private boolean isOver;

    private static Rectangle rect = new Rectangle(1, 1, 13, 14);

    public ProgressDumpScreen(World world) {
        this.researchState = world.getPlayer().getResearchState();
        this.lostCrewMembers = world.getPlayer().getShip().getLostCrewMembers();
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
        if (lastTime == 0) {
            lastTime = container.getTime();
        }

        if (container.getTime() - lastTime > 800) {
            maxIdx++;
            lastTime = container.getTime();
        }

        if (container.getInput().isKeyPressed(Input.KEY_ENTER) || container.getInput().isKeyPressed(Input.KEY_ESCAPE)) {
            if (!allDrawn) {
                allDrawn = true;
                maxIdx = 100500;
            } else {
                isOver = true;
            }
        }

    }

    private void drawLine(int lineIdx, String text1, String text2, Graphics g, Camera camera) {
        final Font font = GUIConstants.dialogFont;
        int text1Width = font.getWidth(text1);
        int text2Width = font.getWidth(text2);
        int dotWidth = font.getWidth(".");

        int totalTextWidth = (int) (rect.getWidth() - 2) * camera.getTileWidth();

        int dotCount = (totalTextWidth - text1Width - text2Width) / dotWidth;

        StringBuilder sb = new StringBuilder(text1);
        for (int i = 0; i < dotCount; ++i) {
            sb.append('.');
        }
        sb.append(text2);
        g.drawString(sb.toString(), (rect.getX() + 1) * camera.getTileWidth(), (rect.getY() + 2 + lineIdx) * camera.getTileHeight());
    }

    @Override
    public void draw(GameContainer container, Graphics graphics, Camera camera) {

        EngineUtils.drawRectWithBorder(graphics, rect, camera, GUIConstants.borderColor, GUIConstants.backgroundColor);
        EngineUtils.drawSingleStringAligned(
                graphics
                , "Exploration results:"
                , GUIConstants.captionFont
                , Color.yellow
                , (int) rect.getX() * camera.getTileWidth()
                , (int) rect.getY() * camera.getTileHeight()
                , (int) rect.getWidth() * camera.getTileWidth()
                , camera.getTileHeight());

        int line = 0;
        int totalScore = 0;
        graphics.setFont(GUIConstants.dialogFont);
        if (maxIdx > 0) {
            final int i = researchState.getGeodata().getRaw();
            totalScore += i;
            drawLine(line++, "Raw geodata", String.valueOf(i), graphics, camera);
        }

        if (maxIdx > 1) {
            final int i = researchState.getGeodata().getProcessed() * 2;
            totalScore += i;
            drawLine(line++, "Processed geodata", String.valueOf(i), graphics, camera);
        }

        if (maxIdx > 2) {
            drawLine(line++, "Astro data", String.valueOf(researchState.getProcessedAstroData()), graphics, camera);
        }

        for (ResearchProjectDesc desc : researchState.getCompletedProjects()) {
            if (maxIdx < line + 1) {
                break;
            }
            totalScore += desc.getScore();
            drawLine(line++, desc.getName(), String.valueOf(desc.getScore()), graphics, camera);
        }

        int tmp = lostCrewMembers > 0 ? 1 : 0;

        if (lostCrewMembers > 0 && maxIdx >= 4 + researchState.getCompletedProjects().size()) {
            drawLine(line++, "Crew members lost", String.valueOf(lostCrewMembers) + " (" + String.valueOf(-10 * lostCrewMembers) + ")", graphics, camera);
            totalScore -= 10 * lostCrewMembers;
        }

        if (maxIdx >= tmp + 4 + researchState.getCompletedProjects().size()) {
            graphics.drawLine((rect.getX() + 1) * camera.getTileWidth(), (rect.getY() + 2 + line) * camera.getTileHeight(), (rect.getX() + rect.getWidth() - 1) * camera.getTileWidth(), (rect.getY() + 2 + line) * camera.getTileHeight());
        }
        if (maxIdx >= tmp + 5 + researchState.getCompletedProjects().size()) {
            graphics.setFont(GUIConstants.captionFont);
            drawLine(line + 1, "Total: ", String.valueOf(totalScore), graphics, camera);
            allDrawn = true;
        }
    }

    @Override
    public boolean isOver() {
        return isOver;
    }
}
