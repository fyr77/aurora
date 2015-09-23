package ru.game.aurora.world;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import ru.game.aurora.application.Camera;
import ru.game.aurora.application.ResourceManager;
import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.dialog.DialogListener;
import ru.game.aurora.gui.GUI;
import ru.game.aurora.world.planet.LandingParty;

import java.util.HashMap;
import java.util.Map;

/**
 * Dungeon is a location with a fixed tiled map, which can be explored by player landing party
 */
public class Dungeon implements Room, IDungeon {
    private static final long serialVersionUID = 2L;

    private final ITileMap map;

    private final DungeonController controller;

    private Dialog enterDialog;

    private BasePositionable landingPartyPrevCoords;

    /**
     * If set to true, and landing party is lost, this leads to a game over
     */
    private boolean isCommanderInParty = false;

    // custom background music that should be played in this dungeon
    private String playlistName;

    /**
     * Just a custom userData that can be used to store dungeon id or anything else
     */
    private Map<String, String> userData;

    public Dungeon(World world, ITileMap map, Room prevRoom) {
        this.map = map;
        this.controller = new DungeonController(world, prevRoom, this);
    }

    public void setEnterDialog(Dialog enterDialog) {
        this.enterDialog = enterDialog;
        this.enterDialog.addListener(new EnterDialogListener());
    }

    public void setSuccessDialog(Dialog successDialog) {
        controller.setSuccessDialog(successDialog);
    }

    public String getPlaylistName() {
        return playlistName;
    }

    public void setPlaylistName(String playlistName) {
        this.playlistName = playlistName;
    }

    @Override
    public void returnTo(World world) {
        GUI.getInstance().getNifty().gotoScreen("surface_gui");
        world.getCamera().resetViewPort();
        LandingParty landingParty = world.getPlayer().getLandingParty();
        world.getCamera().setTarget(landingParty);
        if (playlistName != null) {
            ResourceManager.getInstance().getPlaylist(playlistName).play();
        }
    }

    @Override
    public void enter(World world) {
        world.getCamera().resetViewPort();
        LandingParty landingParty = world.getPlayer().getLandingParty();
        if (landingParty == null || !landingParty.canBeLaunched(world) || world.getGlobalVariables().containsKey("tutorial.landing")) {
            // either this is first landing, or landing party can not be launched in current state and must be reconfigured. Show landing party screen
            if (GUI.getInstance().getNifty().getCurrentScreen().getScreenId().equals("landing_party_equip_screen")) {
                return;
            }
            GUI.getInstance().pushCurrentScreen();
            GUI.getInstance().getNifty().gotoScreen("landing_party_equip_screen");
            if (world.getGlobalVariables().containsKey("tutorial.landing")) {
                // this is first landing on a planet, show tutorial dialog
                Dialog d = Dialog.loadFromFile("dialogs/tutorials/planet_landing_tutorial.json");
                world.addOverlayWindow(d);
                world.getGlobalVariables().remove("tutorial.landing");
            }
        }

        if (enterDialog == null) {
            enterImpl(world);
        } else {
            world.addOverlayWindow(enterDialog);
        }
    }

    private void enterImpl(World world) {
        world.setCurrentRoom(this);
        GUI.getInstance().getNifty().gotoScreen("surface_gui");
        LandingParty landingParty = world.getPlayer().getLandingParty();
        landingPartyPrevCoords = new BasePositionable(landingParty.x, landingParty.y);
        landingParty.setPos(map.getEntryPoint().getX(), map.getEntryPoint().getY());
        landingParty.onLaunch(world);
        world.getCamera().setTarget(landingParty);

        int tilesExplored = map.updateVisibility(landingParty.getX(), landingParty.getY(), 2);
        landingParty.addCollectedGeodata(tilesExplored);

        if (playlistName != null) {
            ResourceManager.getInstance().getPlaylist(playlistName).play();
        }

        world.onPlayerEnteredDungeon(this);
    }

    @Override
    public void update(GameContainer container, World world) {
        controller.update(container, world);
    }

    @Override
    public void draw(GameContainer container, Graphics graphics, Camera camera, World world) {
        map.draw(container, graphics, camera);
        controller.draw(container, graphics, camera);
    }

    @Override
    public DungeonController getController() {
        return controller;
    }

    @Override
    public ITileMap getMap() {
        return map;
    }

    @Override
    public double getTurnToDayRelation() {
        return 0;
    }

    @Override
    public boolean isCommanderInParty() {
        return isCommanderInParty;
    }

    public void setCommanderInParty(boolean commanderInParty) {
        isCommanderInParty = commanderInParty;
    }

    @Override
    public boolean hasCustomMusic() {
        return playlistName != null;
    }

    public Map<String, String> getUserData() {
        if (userData == null) {
            userData = new HashMap<>();
        }
        return userData;
    }

    public BasePositionable getLandingPartyPrevCoords() {
        return landingPartyPrevCoords;
    }

    @Override
    public int getX() {
        return landingPartyPrevCoords.getX();
    }

    @Override
    public int getY() {
        return landingPartyPrevCoords.getY();
    }

    @Override
    public void setPos(int newX, int newY) {

    }

    /**
     * If dungeon has an enter dialog - first show that dialog, and only if it ends with return code 1 - actually
     * enter dungeon
     */
    private final class EnterDialogListener implements DialogListener {
        private static final long serialVersionUID = -8962566365128471357L;

        @Override
        public void onDialogEnded(World world, Dialog dialog, int returnCode, Map<String, String> flags) {
            if (returnCode == 1) {
                // pop prev screen, so that after dialog we will not return there
                GUI.getInstance().popScreen();
                enterImpl(world);
            }
        }
    }
}
