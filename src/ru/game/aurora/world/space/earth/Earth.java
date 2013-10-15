/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 20.12.12
 * Time: 14:22
 */
package ru.game.aurora.world.space.earth;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import ru.game.aurora.application.Camera;
import ru.game.aurora.application.Localization;
import ru.game.aurora.application.ResourceManager;
import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.dialog.DialogListener;
import ru.game.aurora.gui.StoryScreen;
import ru.game.aurora.player.earth.EarthResearch;
import ru.game.aurora.player.earth.PrivateMessage;
import ru.game.aurora.player.research.ResearchProjectDesc;
import ru.game.aurora.player.research.ResearchState;
import ru.game.aurora.world.World;
import ru.game.aurora.world.planet.Planet;
import ru.game.aurora.world.planet.PlanetAtmosphere;
import ru.game.aurora.world.planet.PlanetCategory;
import ru.game.aurora.world.space.StarSystem;

public class Earth extends Planet {

    private static final long serialVersionUID = 3431652617342589266L;

    private Dialog earthDialog;

    private Dialog progressDialog;

    private int lastVisitTurn = 0;

    public Earth(World world, StarSystem owner, PlanetCategory cat, PlanetAtmosphere atmosphere, int size, int x, int y) {
        super(world, owner, cat, atmosphere, size, x, y);
        earthDialog = Dialog.loadFromFile(Earth.class.getClassLoader().getResourceAsStream("dialogs/earth_dialog.json"));
        progressDialog = Dialog.loadFromFile(Earth.class.getClassLoader().getResourceAsStream("dialogs/earth_progress_dialog.json"));
        progressDialog.setListener(new EarthProgressDialogListener(this));
        earthDialog.setListener(new EarthDialogListener(this));
    }

    public Dialog getProgressDialog() {
        return progressDialog;
    }

    @Override
    public void drawOnGlobalMap(GameContainer container, Graphics g, Camera camera, int tileX, int tileY) {
        if (!camera.isInViewport(globalX, globalY)) {
            return;
        }

        final Image earth = ResourceManager.getInstance().getImage("earth");
        g.drawImage(earth, camera.getXCoord(globalX) - earth.getWidth() / 2, camera.getYCoord(globalY) - earth.getHeight() / 2);
    }

    @Override
    public boolean canBeEntered() {
        return true;
    }

    public Dialog getEarthDialog() {
        return earthDialog;
    }

    @Override
    public void enter(World world) {
        String specialDialog = (String) world.getGlobalVariables().get("earth.special_dialog");
        if (specialDialog != null) {
            Dialog d = Dialog.loadFromFile(specialDialog);
            world.addOverlayWindow(d);
            d.setListener(new DialogListener() {

                private static final long serialVersionUID = -374777902752182404L;

                @Override
                public void onDialogEnded(World world, int returnCode) {
                    world.getGlobalVariables().remove("earth.special_dialog");
                }
            });
            //todo: maybe not return? what if both variables are set?
            return;
        }
        if (world.getGlobalVariables().containsKey("quest.main.show_earth_dialog")) {
            showObliteratorThreatDialog(world);
        } else {
            world.addOverlayWindow(earthDialog);
        }
    }

    private void showObliteratorThreatDialog(World world) {
        world.addOverlayWindow(Dialog.loadFromFile("dialogs/quest/main/earth_obliterator_warning_1.json"));
        world.addOverlayWindow(new StoryScreen("story/obliterator.json"));
        Dialog last = Dialog.loadFromFile("dialogs/quest/main/earth_obliterator_warning_2.json");
        last.setListener(new DialogListener() {

            private static final long serialVersionUID = -374777902752182404L;

            @Override
            public void onDialogEnded(World world, int returnCode) {
                world.getGlobalVariables().remove("quest.main.show_earth_dialog");
            }
        });
        world.addOverlayWindow(last);
        // starting slow evacuation
        world.getPlayer().getEarthState().getEvacuationState().changeEvacuationSpeed(10);

        // after some time evacuation will go faster
        world.addListener(new EarthResearch("evac_announce", 50) {

            private static final long serialVersionUID = -3699793069471281672L;

            @Override
            protected void onCompleted(World world) {
                world.getPlayer().getEarthState().getEvacuationState().changeEvacuationSpeed(100);
                world.getPlayer().getEarthState().getMessages().add(new PrivateMessage(
                        "evac_start",
                        "news"
                ));

                world.getPlayer().getEarthState().getMessages().add(new PrivateMessage(
                        "evac_start_2",
                        "message"
                ));
            }
        });

        world.getGlobalVariables().put("quest.main.evacuation_started", null);
    }

    @Override
    public void update(GameContainer container, World world) {

    }

    @Override
    public void draw(GameContainer container, Graphics g, Camera camera) {
        owner.draw(container, g, camera);
    }

    public int getLastVisitTurn() {
        return lastVisitTurn;
    }

    public void setLastVisitTurn(int lastVisitTurn) {
        this.lastVisitTurn = lastVisitTurn;
    }

    int dumpResearch(World world) {
        world.onPlayerReturnToEarth();
        int result = 0;
        final ResearchState researchState = world.getPlayer().getResearchState();
        result += researchState.getGeodata().dumpAndGetVictoryPoints();
        for (ResearchProjectDesc desc : researchState.getCompletedProjects()) {
            result += desc.getScore();
            if (desc.getEarthProgress() != null) {
                for (String resId : desc.getEarthProgress()) {
                    EarthResearch res = world.getResearchAndDevelopmentProjects().getEarthResearchProjects().get(resId);
                    world.addListener(res);
                    res.onStarted(world);
                }
            }
        }
        researchState.getCompletedProjects().clear();
        result += researchState.dumpAstroData();
        world.getPlayer().getEarthState().updateTechnologyLevel(result);
        // subtracting points for loosing crew members
        result -= 10 * (world.getPlayer().getShip().getLostCrewMembers());
        return result;
    }

    @Override
    public boolean canBeLanded() {
        return false;
    }

    @Override
    public StringBuilder getScanText(){
        StringBuilder sb = new StringBuilder(Localization.getText("races", "Humans.homeworld.description"));
        return sb;
    }
}
