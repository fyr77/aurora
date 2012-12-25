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
import ru.game.aurora.ui.ListWithIconAndDescrScreen;
import ru.game.aurora.world.Room;
import ru.game.aurora.world.World;

public class ResearchScreen extends ListWithIconAndDescrScreen implements Room {
    private static final int ACTIVE_TAB = 0;
    private static final int AVAILABLE_TAB = 1;
    private static final int COMPLETED_TAB = 2;

    private int currentTab;

    private int currentIdx;

    private ResearchState researchState;

    @Override
    public void enter(World world) {
        super.enter(world);
        researchState = world.getPlayer().getResearchState();
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
            iconName = researchProjectState.desc.getIcon();
            descr = researchProjectState.desc.getDescription() + " \n" + researchProjectState.desc.getStatusString(world, researchProjectState.scientists);
        }

        draw(engine, camera, "Active projects", iconName, descr);
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
            icon = desc.getIcon();
            descr = desc.getDescription();
        }

        draw(engine, camera, "Available research projects", icon, descr);
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
            icon = desc.getIcon();
            descr = desc.getDescription();
        }

        draw(engine, camera, "Completed research projects", icon, descr);
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
                if (currentIdx >= maxIdx) {
                    currentIdx = 0;
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
                if (currentIdx >= maxIdx) {
                    currentIdx = 0;
                }
                if (engine.getKey(JGEngineInterface.KeyEnter)) {
                    GameLogger.getInstance().logMessage("Starting research " + researchState.getAvailableProjects().get(currentIdx).getName());
                    ResearchProjectDesc desc = researchState.getAvailableProjects().remove(currentIdx);
                    researchState.getCurrentProjects().add(new ResearchProjectState(desc, 0));
                    if (currentIdx > 0) {
                        currentIdx--;
                    }
                }
                break;
            case COMPLETED_TAB:
                maxIdx = researchState.getCompletedProjects().size();
                if (currentIdx >= maxIdx) {
                    currentIdx = 0;
                }
                break;
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
            //previousRoom.enter(world); do not call here, as it is not real room entry
        }
    }

    public Room getPreviousRoom() {
        return previousRoom;
    }
}
