/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 06.12.12
 * Time: 17:04
 */

package ru.game.aurora.player.research;

import de.lessvoid.nifty.screen.Screen;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import ru.game.aurora.application.Camera;
import ru.game.aurora.application.GameLogger;
import ru.game.aurora.ui.ListWithIconAndDescrScreen;
import ru.game.aurora.world.Room;
import ru.game.aurora.world.World;

public class ResearchScreen extends ListWithIconAndDescrScreen implements Room
{
    private static final int ACTIVE_TAB = 0;

    private static final int AVAILABLE_TAB = 1;

    private static final int COMPLETED_TAB = 2;

    private static final long serialVersionUID = 7529297861702546424L;

    private int currentTab;

    private ResearchState researchState;

    private static final String[] tabs = {"Active projects", "Available projects", "Completed projects"};

    @Override
    public void enter(World world) {
        super.enter(world);
        researchState = world.getPlayer().getResearchState();
    }

    @Override
    public Screen getGUI() {
        return null;
    }

    private void drawActiveTasksTab(Graphics graphics, Camera camera) {
        strings.clear();

        for (ResearchProjectState res : researchState.getCurrentProjects()) {
            strings.add(res.desc.name + ": " + res.scientists + " scientists");
        }

        String iconName = null;
        String descr = null;
        if (!researchState.getCurrentProjects().isEmpty()) {
            graphics.setColor(Color.white);
            final ResearchProjectState researchProjectState = researchState.getCurrentProjects().get(currentIdx);
            iconName = researchProjectState.desc.getIcon();
            descr = researchProjectState.desc.getDescription() + " \n \n " + researchProjectState.desc.getStatusString(world, researchProjectState.scientists);
        }

        drawTabbed(graphics, camera, tabs, currentTab, iconName, descr);
        GameLogger.getInstance().addStatusMessage("Available scientists: " + researchState.getIdleScientists());
    }

    private void drawAvailableTasksTab(Graphics graphics, Camera camera) {
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

        drawTabbed(graphics, camera, tabs, currentTab, icon, descr);
        GameLogger.getInstance().addStatusMessage("Press <enter> to start selected research");
    }

    private void drawCompletedTasksTab(Graphics graphics, Camera camera) {
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

        drawTabbed(graphics, camera, tabs, currentTab, icon, descr);
    }


    @Override
    public void draw(GameContainer container, Graphics graphics, Camera camera) {
        switch (currentTab) {
            case ACTIVE_TAB:
                drawActiveTasksTab(graphics, camera);
                break;
            case AVAILABLE_TAB:
                drawAvailableTasksTab(graphics, camera);
                break;
            case COMPLETED_TAB:
                drawCompletedTasksTab(graphics, camera);
                break;
        }

        GameLogger.getInstance().addStatusMessage("Up/down to select research project");
        GameLogger.getInstance().addStatusMessage("Left/right to change number of assigned scientists");
        GameLogger.getInstance().addStatusMessage("TAB to switch tabs (active > available > completed)");
    }


    @Override
    public void update(GameContainer container, World world) {
        if (container.getInput().isKeyPressed(Input.KEY_TAB)) {
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
                if (container.getInput().isKeyPressed(Input.KEY_LEFT) && state.scientists > 0) {
                    state.scientists--;
                    researchState.setIdleScientists(researchState.getIdleScientists() + 1);
                }

                if (container.getInput().isKeyPressed(Input.KEY_RIGHT) && researchState.getIdleScientists() > 0) {
                    state.scientists++;
                    researchState.setIdleScientists(researchState.getIdleScientists() - 1);
                }
                break;
            case AVAILABLE_TAB:
                maxIdx = researchState.getAvailableProjects().size();
                if (currentIdx >= maxIdx) {
                    currentIdx = 0;
                }
                if (container.getInput().isKeyPressed(Input.KEY_ENTER)) {
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

        if (container.getInput().isKeyPressed(Input.KEY_UP)) {
            currentIdx--;
            if (currentIdx < 0) {
                currentIdx = maxIdx - 1;
            }
        }

        if (container.getInput().isKeyPressed(Input.KEY_DOWN)) {
            currentIdx++;
            if (currentIdx == maxIdx) {
                currentIdx = 0;
            }
        }

        if (container.getInput().isKeyPressed(Input.KEY_ESCAPE)) {
            world.setCurrentRoom(previousRoom);
            //previousRoom.enter(world); do not call here, as it is not real room entry
        }
    }

    public Room getPreviousRoom() {
        return previousRoom;
    }
}
