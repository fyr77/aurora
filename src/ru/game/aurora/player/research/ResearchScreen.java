/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 06.12.12
 * Time: 17:04
 */

package ru.game.aurora.player.research;

import jgame.JGColor;
import jgame.JGRectangle;
import jgame.impl.JGEngineInterface;
import jgame.platform.JGEngine;
import ru.game.aurora.application.Camera;
import ru.game.aurora.application.GUIConstants;
import ru.game.aurora.application.GameLogger;
import ru.game.aurora.util.JGEngineUtils;
import ru.game.aurora.world.Room;
import ru.game.aurora.world.World;

import java.util.ArrayList;
import java.util.List;

public class ResearchScreen implements Room {
    private static final int ACTIVE_TAB = 0;
    private static final int AVAILABLE_TAB = 1;
    private static final int COMPLETED_TAB = 2;

    private int currentTab;

    private int currentIdx;

    private World world;

    private ResearchState researchState;

    private Room previousRoom;

    private List<String> strings = new ArrayList<String>(24);

    private static final JGRectangle captionRect = new JGRectangle(1, 1, 12, 1);

    private static final JGRectangle listRect = new JGRectangle(1, 3, 7, 10);

    private static final JGRectangle imageRect = new JGRectangle(9, 3, 4, 4);

    private static final JGRectangle descriptionRect = new JGRectangle(9, 8, 4, 5);

    private static final JGColor backgroundColor = new JGColor(4, 7, 125);

    @Override
    public void enter(World world) {
        this.world = world;
        this.previousRoom = world.getCurrentRoom();
        researchState = world.getPlayer().getResearchState();
    }


    private void drawTab(JGEngine engine, Camera camera, String caption, String image, String description) {
        JGEngineUtils.drawRectWithBorder(engine, captionRect, camera, JGColor.yellow, backgroundColor);
        JGEngineUtils.drawRectWithBorder(engine, listRect, camera, JGColor.yellow, backgroundColor);
        JGEngineUtils.drawRectWithBorder(engine, imageRect, camera, JGColor.yellow, backgroundColor);
        JGEngineUtils.drawRectWithBorder(engine, descriptionRect, camera, JGColor.yellow, backgroundColor);

        engine.drawString(caption, camera.getRelativeX(captionRect.x) + 200, camera.getRelativeY(captionRect.y) + 20, -1, GUIConstants.dialogFont, JGColor.yellow);

        if (image != null) {
            engine.drawImage(image, camera.getRelativeX(imageRect.x), camera.getRelativeY(imageRect.y));
        }

        if (description == null) {
            description = "<Select a research project>";
        }
        JGEngineUtils.drawString(engine, description, camera.getRelativeX(descriptionRect.x) + 10, camera.getRelativeY(descriptionRect.y) + 10, camera.getTileWidth() * descriptionRect.width - 20, GUIConstants.dialogFont, JGColor.yellow);

        if (strings.isEmpty()) {
            engine.drawString("<No projects in this category>", camera.getRelativeX(listRect.x) + 10, camera.getRelativeY(listRect.y) + 10, -1, GUIConstants.dialogFont, JGColor.yellow);
        } else {
            for (int i = 0; i < strings.size(); ++i) {
                engine.drawString(strings.get(i), camera.getRelativeX(listRect.x) + 20, camera.getRelativeY(listRect.y + i) + 20, -1, GUIConstants.dialogFont, i == currentIdx ? JGColor.green : JGColor.yellow);
            }
        }
    }


    private void drawActiveTasksTab(JGEngine engine, Camera camera) {
        strings.clear();

        for (ResearchProjectState res : researchState.getCurrentProjects()) {
            strings.add(res.desc.name + ": " + res.scientists + " scientists");
        }

        String iconName = null;
        String descr = null;
        if (!researchState.getCurrentProjects().isEmpty()) {
            engine.setColor(JGColor.white);
            final ResearchProjectState researchProjectState = researchState.getCurrentProjects().get(currentIdx);
            iconName = "cartography_research";
            descr = researchProjectState.desc.getDescription() + "\n" + researchProjectState.desc.getStatusString(world.getPlayer(), researchProjectState.scientists);
        }

        drawTab(engine, camera, "Active projects", iconName, descr);
        GameLogger.getInstance().addStatusMessage("Available scientists: " + researchState.getIdleScientists());
    }

    private void drawAvailableTasksTab(JGEngine engine, Camera camera) {
        strings.clear();
        for (ResearchProjectDesc res : researchState.getAvailableProjects()) {
            strings.add(res.name);
        }

        String icon = null;
        String descr = null;
        if (!researchState.getAvailableProjects().isEmpty()) {
            ResearchProjectDesc desc = researchState.getAvailableProjects().get(currentIdx);
            icon = "cartography_research";
            descr = desc.getDescription();
        }

        drawTab(engine, camera, "Available research projects", icon, descr);
        GameLogger.getInstance().addStatusMessage("Press <enter> to start selected research");
    }

    private void drawCompletedTasksTab(JGEngine engine, Camera camera) {
        strings.clear();
        for (ResearchProjectDesc res : researchState.getCompletedProjects()) {
            strings.add(res.name);
        }

        String icon = null;
        String descr = null;
        if (!researchState.getCompletedProjects().isEmpty()) {
            ResearchProjectDesc desc = researchState.getCompletedProjects().get(currentIdx);
            icon = "cartography_research";
            descr = desc.getDescription();
        }

        drawTab(engine, camera, "Completed research projects", icon, descr);
    }


    @Override
    public void draw(JGEngine engine, Camera camera) {
        switch (currentTab) {
            case ACTIVE_TAB:
                drawActiveTasksTab(engine, camera);
                break;
            case AVAILABLE_TAB:
                drawAvailableTasksTab(engine, camera);
                break;
            case COMPLETED_TAB:
                drawCompletedTasksTab(engine, camera);
                break;
        }

        GameLogger.getInstance().addStatusMessage("Up/down to select research project");
        GameLogger.getInstance().addStatusMessage("Left/right to change number of assigned scientists");
        GameLogger.getInstance().addStatusMessage("Space to switch tabs (active > available > completed)");
    }


    @Override
    public void update(JGEngine engine, World world) {
        if (engine.getLastKeyChar() == ' ') {
            currentTab++;
            if (currentTab > COMPLETED_TAB) {
                currentTab = 0;
            }
        }

        int maxIdx = 0;

        switch (currentTab) {
            case ACTIVE_TAB:
                maxIdx = researchState.getCurrentProjects().size();
                if (maxIdx == 0) {
                    break;
                }
                ResearchProjectState state = researchState.getCurrentProjects().get(currentIdx);
                if (engine.getKey(JGEngineInterface.KeyLeft) && state.scientists > 0) {
                    state.scientists--;
                    researchState.setIdleScientists(researchState.getIdleScientists() + 1);
                }

                if (engine.getKey(JGEngineInterface.KeyRight) && researchState.getIdleScientists() > 0) {
                    state.scientists++;
                    researchState.setIdleScientists(researchState.getIdleScientists() - 1);
                }
                break;
            case AVAILABLE_TAB:
                maxIdx = researchState.getAvailableProjects().size();
                if (engine.getKey(JGEngineInterface.KeyEnter)) {
                    GameLogger.getInstance().logMessage("Starting research " + researchState.getAvailableProjects().get(currentIdx).getName());
                    ResearchProjectDesc desc = researchState.getAvailableProjects().remove(currentIdx);
                    researchState.getCurrentProjects().add(new ResearchProjectState(desc, 0));
                    currentIdx--;
                }
                break;
            case COMPLETED_TAB:
                maxIdx = researchState.getCompletedProjects().size();
        }

        if (engine.getKey(JGEngine.KeyUp)) {
            currentIdx--;
            if (currentIdx < 0) {
                currentIdx = maxIdx - 1;
            }
        }

        if (engine.getKey(JGEngine.KeyDown)) {
            currentIdx++;
            if (currentIdx == maxIdx) {
                currentIdx = 0;
            }
        }

        if (engine.getKey(JGEngine.KeyEsc)) {
            world.setCurrentRoom(previousRoom);
            //previousRoom.enter(world); do not call here, as it is not reall room entry
        }
    }
}
