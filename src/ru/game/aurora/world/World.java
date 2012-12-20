/**
 * User: jedi-philosopher
 * Date: 29.11.12
 * Time: 20:09
 */
package ru.game.aurora.world;

import jgame.platform.JGEngine;
import ru.game.aurora.application.Camera;
import ru.game.aurora.npc.Dialog;
import ru.game.aurora.player.Player;
import ru.game.aurora.player.research.ResearchScreen;
import ru.game.aurora.world.space.GalaxyMap;

public class World {
    private Camera camera;

    private Room currentRoom;

    private GalaxyMap galaxyMap;

    private Player player;

    private boolean updatedThisFrame;

    private Dialog currentDialog;

    private int turnCount = 0;

    public World(JGEngine engine, Camera camera, int sizeX, int sizeY) {
        player = new Player();
        this.camera = camera;
        camera.setTarget(player.getShip());
        currentRoom = galaxyMap = new GalaxyMap(sizeX, sizeY, camera.getNumTilesX(), camera.getNumTilesY());
        currentRoom.enter(this);
        updatedThisFrame = false;
    }

    public void update(JGEngine engine) {
        updatedThisFrame = false;

        if (currentDialog != null) {
            currentDialog.update(engine, this);
            if (currentDialog.isOver()) {
                currentDialog = null;
            }
            return;
        }
        if (engine.getLastKeyChar() == 'r') {
            // open research screen
            ResearchScreen researchScreen = new ResearchScreen();
            researchScreen.enter(this);
            currentRoom = researchScreen;
            return;
        }
        currentRoom.update(engine, this);
        if (isUpdatedThisFrame()) {
            player.getResearchState().update(this);
            turnCount++;
        }
    }

    public void draw(JGEngine engine) {
        currentRoom.draw(engine, camera);
        if (currentDialog != null) {
            currentDialog.draw(engine, camera);
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
    }

    public void setCurrentDialog(Dialog currentDialog) {
        this.currentDialog = currentDialog;
        currentDialog.enter(this);
    }

    public int getTurnCount() {
        return turnCount;
    }
}
