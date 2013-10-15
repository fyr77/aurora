/**
 * User: jedi-philosopher
 * Date: 29.11.12
 * Time: 20:09
 */
package ru.game.aurora.world;

import de.lessvoid.nifty.Nifty;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import ru.game.aurora.application.Camera;
import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.gui.*;
import ru.game.aurora.npc.AlienRace;
import ru.game.aurora.player.Player;
import ru.game.aurora.player.earth.EvacuationState;
import ru.game.aurora.player.research.ResearchProjectDesc;
import ru.game.aurora.player.research.RnDSet;
import ru.game.aurora.world.generation.StarSystemNamesCollection;
import ru.game.aurora.world.planet.Planet;
import ru.game.aurora.world.space.GalaxyMap;
import ru.game.aurora.world.space.StarSystem;

import java.io.Serializable;
import java.util.*;

public class World implements Serializable {

    private static final long serialVersionUID = 1L;

    private Camera camera;

    /**
     * Set by game logic, shows that game is over and on next update this world will be deallocated and main menu will be shown
     */
    private boolean isGameOver;

    private Room currentRoom;

    // if true, current room is not updated
    private boolean isPaused;

    private GalaxyMap galaxyMap;

    private Player player;

    private transient boolean updatedThisFrame;

    private transient boolean updatedNextFrame;

    private transient StarSystemNamesCollection starSystemNamesCollection = new StarSystemNamesCollection();

    private transient List<OverlayWindow> overlayWindows = new LinkedList<OverlayWindow>();

    private StarSystem currentStarSystem = null;

    private int turnCount = 0;

    private List<GameEventListener> listeners = new LinkedList<GameEventListener>();

    private Map<String, AlienRace> races = new HashMap<String, AlienRace>();

    private Map<String, Serializable> globalVariables = new HashMap<String, Serializable>();

    private RnDSet researchAndDevelopmentProjects;


    public World(int sizeX, int sizeY) {
        player = new Player();
        updatedThisFrame = false;
        currentRoom = galaxyMap = new GalaxyMap(this, sizeX, sizeY);
        researchAndDevelopmentProjects = new RnDSet();
    }

    public Map<String, Serializable> getGlobalVariables() {
        return globalVariables;
    }

    public void update(GameContainer container) {
        if (overlayWindows != null && !overlayWindows.isEmpty()) {
            Iterator<OverlayWindow> iter = overlayWindows.iterator();
            OverlayWindow w = iter.next();
            w.update(container, this);
            if (w.isOver()) {
                iter.remove();
            }
            // only one active overlay window
            return;
        }

        if (isPaused) {
            return;
        }

        updatedThisFrame = updatedNextFrame;
        updatedNextFrame = false;

        if (container.getInput().isKeyPressed(Input.KEY_R)) {
            GUI.getInstance().pushCurrentScreen();
            GUI.getInstance().getNifty().gotoScreen("research_screen");
            return;
        }

        if (container.getInput().isKeyPressed(Input.KEY_E)) {
            GUI.getInstance().pushCurrentScreen();
            GUI.getInstance().getNifty().gotoScreen("engineering_screen");
            return;
        }

        currentRoom.update(container, this);
        if (isUpdatedThisFrame()) {
            player.getResearchState().update(this);
            player.getEngineeringState().update(this);
            player.getEarthState().update(this);
            turnCount++;
            EvacuationState es = player.getEarthState().getEvacuationState();
            if (es != null && es.isGameOver(this)) {
                es.showEndGameScreen(this);
                return;
            }
            // to prevent concurrent modification if some of listeners adds new ones
            List<GameEventListener> oldListeners = new LinkedList<GameEventListener>(listeners);
            for (GameEventListener listener : oldListeners) {
                listener.onTurnEnded(this);
            }
        }

        for (Iterator<GameEventListener> listenerIterator = listeners.iterator(); listenerIterator.hasNext(); ) {
            GameEventListener l = listenerIterator.next();
            if (!l.isAlive()) {
                listenerIterator.remove();
            }
        }

        // should be the last so that ESC event is not consumed
        if (container.getInput().isKeyPressed(Input.KEY_ESCAPE) && (currentRoom instanceof GalaxyMap || currentRoom instanceof Planet || currentRoom instanceof StarSystem)) {
            GUI.getInstance().showIngameMenu();
            return;
        }

        if (container.getInput().isKeyPressed(Input.KEY_F1)) {
            addOverlayWindow(new HelpScreen());
        }

    }

    public void draw(GameContainer container, Graphics graphics) {
        currentRoom.draw(container, graphics, camera);
        if (overlayWindows != null && !overlayWindows.isEmpty()) {
            overlayWindows.get(0).draw(container, graphics, camera);
        }
    }

    public Camera getCamera() {
        return camera;
    }

    public boolean isUpdatedThisFrame() {
        return updatedThisFrame;
    }

    public void setUpdatedThisFrame(boolean updatedThisFrame) {
        this.updatedThisFrame = updatedThisFrame;
    }

    public Room getCurrentRoom() {
        return currentRoom;
    }

    public IDungeon getCurrentDungeon() {
        return (currentRoom instanceof IDungeon) ? (IDungeon) currentRoom : null;
    }

    public GalaxyMap getGalaxyMap() {
        return galaxyMap;
    }

    public Player getPlayer() {
        return player;
    }

    public void setCurrentRoom(Room currentRoom) {
        this.currentRoom = currentRoom;
    }

    public void addOverlayWindow(OverlayWindow currentDialog) {
        if (overlayWindows == null) {
            overlayWindows = new LinkedList<>();
        }
        overlayWindows.add(currentDialog);
        currentDialog.enter(this);
    }

    public void addOverlayWindow(Dialog d) {
        GUI.getInstance().pushCurrentScreen();
        d.enter(this);
        Nifty nifty = GUI.getInstance().getNifty();
        ((DialogController) nifty.getScreen("dialog_screen").getScreenController()).pushDialog(d);
        nifty.gotoScreen("dialog_screen");
    }

    public void addOverlayWindow(StoryScreen s) {
        GUI.getInstance().pushCurrentScreen();
        Nifty nifty = GUI.getInstance().getNifty();
        ((StoryScreenController) nifty.getScreen("story_screen").getScreenController()).setStory(s);
        nifty.gotoScreen("story_screen");
    }

    public void addOverlayWindow(ResearchProjectDesc s) {
        GUI.getInstance().pushCurrentScreen();
        Nifty nifty = GUI.getInstance().getNifty();
        ((ResearchReportScreenController) nifty.getScreen("research_report_screen").getScreenController()).setResearch(s);
        nifty.gotoScreen("research_report_screen");
    }

    public int getTurnCount() {
        return turnCount;
    }

    public void onPlayerEnteredSystem(StarSystem ss) {
        List<GameEventListener> newList = new LinkedList<GameEventListener>(listeners);
        for (GameEventListener l : newList) {
            l.onPlayerEnterStarSystem(this, ss);
        }
    }

    public void onPlayerShipDamaged() {
        List<GameEventListener> newList = new LinkedList<GameEventListener>(listeners);
        for (GameEventListener l : newList) {
            l.onPlayerShipDamaged(this);
        }
    }

    public void onCrewChanged() {
        List<GameEventListener> newList = new LinkedList<GameEventListener>(listeners);
        for (GameEventListener l : newList) {
            l.onCrewChanged(this);
        }
    }

    public void onPlayerReturnToEarth() {
        // to avoid concurrent modification exception
        List<GameEventListener> newList = new LinkedList<GameEventListener>(listeners);
        for (GameEventListener l : newList) {
            l.onReturnToEarth(this);
        }
    }

    public void addListener(GameEventListener listener) {
        listeners.add(listener);
    }

    public List<GameEventListener> getListeners() {
        return listeners;
    }

    public void setCamera(Camera camera) {
        this.camera = camera;
    }

    public boolean isGameOver() {
        return isGameOver;
    }

    public void setGameOver(boolean gameOver) {
        isGameOver = gameOver;
    }

    public StarSystem getCurrentStarSystem() {
        return currentStarSystem;
    }

    public void setCurrentStarSystem(StarSystem currentStarSystem) {
        this.currentStarSystem = currentStarSystem;
    }

    public Map<String, AlienRace> getRaces() {
        return races;
    }

    public StarSystemNamesCollection getStarSystemNamesCollection() {
        return starSystemNamesCollection;
    }

    public RnDSet getResearchAndDevelopmentProjects() {
        return researchAndDevelopmentProjects;
    }

    public void setUpdatedNextFrame(boolean updatedNextFrame) {
        this.updatedNextFrame = updatedNextFrame;
    }

    public void setPaused(boolean paused) {
        isPaused = paused;
    }
}
