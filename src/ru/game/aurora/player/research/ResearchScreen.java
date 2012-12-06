/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 06.12.12
 * Time: 17:04
 */

package ru.game.aurora.player.research;

import jgame.JGColor;
import jgame.impl.JGEngineInterface;
import jgame.platform.JGEngine;
import ru.game.aurora.application.Camera;
import ru.game.aurora.application.GameLogger;
import ru.game.aurora.util.JGEngineUtils;
import ru.game.aurora.world.Room;
import ru.game.aurora.world.World;

public class ResearchScreen implements Room {
    private static final int ACTIVE_TAB = 0;
    private static final int AVAILABLE_TAB = 1;
    private static final int COMPLETED_TAB = 2;

    private int currentTab;

    private int currentIdx;

    private World world;

    private ResearchState researchState;

    private Room previousRoom;

    @Override
    public void enter(World world) {
        this.world = world;
        this.previousRoom = world.getCurrentRoom();
        researchState = world.getPlayer().getResearchState();
    }

    private void drawActiveTasksTab(JGEngine engine, Camera camera) {
        engine.drawString("Active research projects:", 50, 50, -1, GameLogger.getInstance().getFont(), JGColor.white);
        int i = 1;
        for (ResearchProjectState res : researchState.getCurrentProjects()) {
            JGColor color = (i == currentIdx + 1 ? JGColor.green : JGColor.white);
            engine.drawString(res.desc.name + ": " + res.scientists + " scientists", 50, 50 + i * GameLogger.getInstance().getFont().getSize(), -1, GameLogger.getInstance().getFont(), color);
            ++i;
        }

        if (!researchState.getCurrentProjects().isEmpty()) {
            engine.setColor(JGColor.white);
            final ResearchProjectState researchProjectState = researchState.getCurrentProjects().get(currentIdx);
            JGEngineUtils.drawString(engine, researchProjectState.desc.getDescription() + "\n"  + researchProjectState.desc.getStatusString(world.getPlayer(), researchProjectState.scientists), 250, 50, 600);
        }
        GameLogger.getInstance().addStatusMessage("Available scientists: " + researchState.getIdleScientists());
    }

    private void drawAvailableTasksTab(JGEngine engine, Camera camera) {
        engine.drawString("Available research projects:", 50, 50, -1, GameLogger.getInstance().getFont(), JGColor.white);
        int i = 1;
        for (ResearchProjectDesc res : researchState.getAvailableProjects()) {
            JGColor color = (i == currentIdx + 1 ? JGColor.green : JGColor.white);
            engine.drawString(res.name, 50, 50 + i * GameLogger.getInstance().getFont().getSize(), -1, GameLogger.getInstance().getFont(), color);
            ++i;
        }

        if (!researchState.getAvailableProjects().isEmpty()) {
            engine.drawString(researchState.getAvailableProjects().get(currentIdx).getDescription(), 250, 50, -1, GameLogger.getInstance().getFont(), JGColor.white);
        }
        GameLogger.getInstance().addStatusMessage("Press <enter> to start selected research");
    }

    private void drawCompletedTasksTab(JGEngine engine, Camera camera) {
        engine.drawString("Completed research projects:", 50, 50, -1, GameLogger.getInstance().getFont(), JGColor.white);
        int i = 1;
        for (ResearchProjectDesc res : researchState.getCompletedProjects()) {
            JGColor color = (i == currentIdx + 1 ? JGColor.green : JGColor.white);
            engine.drawString(res.name, 50, 50 + i * GameLogger.getInstance().getFont().getSize(), -1, GameLogger.getInstance().getFont(), color);
            ++i;
        }
        if (!researchState.getCompletedProjects().isEmpty()) {
            engine.drawString(researchState.getCompletedProjects().get(currentIdx).getDescription(), 250, 50, -1, GameLogger.getInstance().getFont(), JGColor.white);
        }
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
