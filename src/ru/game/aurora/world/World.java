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
import ru.game.aurora.player.Player;
import ru.game.aurora.player.research.ResearchScreen;
import ru.game.aurora.world.planet.Planet;
import ru.game.aurora.world.space.GalaxyMap;
import ru.game.aurora.world.space.StarSystem;

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class World implements Serializable {
    private static final long serialVersionUID = 8351730882236794281L;

    private Camera camera;

    /**
     * Set by game logic, shows that game is over and on next update this world will be deallocated and main menu will be shown
     */
    private boolean isGameOver;

    private Room currentRoom;

    private GalaxyMap galaxyMap;

    private Player player;

    private transient boolean updatedThisFrame;

    private transient OverlayWindow currentDialog;

    private StarSystem currentStarSystem = null;

    private int turnCount = 0;

    private List<GameEventListener> listeners = new LinkedList<GameEventListener>();

    public World(int sizeX, int sizeY) {
        player = new Player();
        updatedThisFrame = false;
        currentRoom = galaxyMap = new GalaxyMap(this, sizeX, sizeY);
    }

    public void update(GameContainer container) {
        updatedThisFrame = false;

        if (currentDialog != null) {
            currentDialog.update(container, this);
            if (currentDialog.isOver()) {
                currentDialog = null;
            }
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
            turnCount++;
        }

        for (Iterator<GameEventListener> listenerIterator = listeners.iterator(); listenerIterator.hasNext();) {
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
            GameMenu menu = new GameMenu();
            menu.enter(this);
            currentRoom = menu;
        }

        if (container.getInput().isKeyPressed(Input.KEY_F1)) {
            setCurrentDialog(new HelpScreen());
        }

    }

    public void draw(GameContainer container, Graphics graphics) {
        currentRoom.draw(container, graphics, camera);
        if (currentDialog != null) {
            currentDialog.draw(container, graphics, camera);
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
        GUI.getInstance().setCurrentScreen(currentRoom.getGUI());
    }

    public void setCurrentDialog(OverlayWindow currentDialog) {
        this.currentDialog = currentDialog;
        currentDialog.enter(this);
    }

    public int getTurnCount() {
        return turnCount;
    }

    public void onPlayerEnteredSystem(StarSystem ss) {
        for (GameEventListener l : listeners) {
            l.onPlayerEnterStarSystem(this, ss);
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
}
