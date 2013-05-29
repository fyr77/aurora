/**
 * User: jedi-philosopher
 * Date: 29.11.12
 * Time: 20:09
 */
package ru.game.aurora.world;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import ru.game.aurora.application.Camera;
import ru.game.aurora.gui.EngineeringScreen;
import ru.game.aurora.gui.GUI;
import ru.game.aurora.gui.HelpScreen;
import ru.game.aurora.npc.AlienRace;
import ru.game.aurora.player.Player;
import ru.game.aurora.player.earth.EvacuationState;
import ru.game.aurora.player.research.ResearchScreen;
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

    private GalaxyMap galaxyMap;

    private Player player;

    private transient boolean updatedThisFrame;

    private transient StarSystemNamesCollection starSystemNamesCollection = new StarSystemNamesCollection();

    private transient List<OverlayWindow> overlayWindows = new LinkedList<OverlayWindow>();

    private StarSystem currentStarSystem = null;

    private int turnCount = 0;

    private List<GameEventListener> listeners = new LinkedList<GameEventListener>();

    private Map<String, AlienRace> races = new HashMap<String, AlienRace>();

    private Map<String, Serializable> globalVariables = new HashMap<String, Serializable>();

    public World(int sizeX, int sizeY) {
        player = new Player();
        updatedThisFrame = false;
        currentRoom = galaxyMap = new GalaxyMap(this, sizeX, sizeY);
    }

    public Map<String, Serializable> getGlobalVariables() {
        return globalVariables;
    }

    public void update(GameContainer container) {
        updatedThisFrame = false;

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
        if (container.getInput().isKeyPressed(Input.KEY_R)) {
            // open research screen
            ResearchScreen researchScreen = new ResearchScreen();
            researchScreen.enter(this);
            currentRoom = researchScreen;
            return;
        }

        if (container.getInput().isKeyPressed(Input.KEY_E)) {
            // open research screen
            EngineeringScreen engineeringScreen = new EngineeringScreen();
            engineeringScreen.enter(this);
            currentRoom = engineeringScreen;
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

        if (player.getShip().getTotalCrew() <= 0) {
            if (currentRoom instanceof Planet && player.getLandingParty().getTotalMembers() <= 0) {
                container.exit();
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

    public GalaxyMap getGalaxyMap() {
        return galaxyMap;
    }

    public Player getPlayer() {
        return player;
    }

    public void setCurrentRoom(Room currentRoom) {
        this.currentRoom = currentRoom;
        //GUI.getInstance().setCurrentScreen(currentRoom.getGUI());
    }

    public void addOverlayWindow(OverlayWindow currentDialog) {
        if (overlayWindows == null) {
            overlayWindows = new LinkedList<OverlayWindow>();
        }
        overlayWindows.add(currentDialog);
        currentDialog.enter(this);
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
}
