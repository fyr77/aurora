/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 20.12.12
 * Time: 14:22
 */
package ru.game.aurora.world.space.earth;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import ru.game.aurora.application.Camera;
import ru.game.aurora.application.ResourceManager;
import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.dialog.DialogListener;
import ru.game.aurora.dialog.Reply;
import ru.game.aurora.dialog.Statement;
import ru.game.aurora.gui.FailScreen;
import ru.game.aurora.gui.ProgressDumpScreen;
import ru.game.aurora.gui.StoryScreen;
import ru.game.aurora.player.earth.EarthResearch;
import ru.game.aurora.player.earth.EarthScreen;
import ru.game.aurora.player.earth.EvacuationState;
import ru.game.aurora.player.research.ResearchProjectDesc;
import ru.game.aurora.player.research.ResearchState;
import ru.game.aurora.world.World;
import ru.game.aurora.world.planet.Planet;
import ru.game.aurora.world.planet.PlanetAtmosphere;
import ru.game.aurora.world.planet.PlanetCategory;
import ru.game.aurora.world.space.StarSystem;

public class Earth extends Planet {

    private static final long serialVersionUID = 3431652617342589266L;

    private ProgressDumpScreen dumpScreen = null;

    private Dialog earthDialog;

    private Dialog progressDialog;

    private int lastVisitTurn = 0;

    public Earth(StarSystem owner, PlanetCategory cat, PlanetAtmosphere atmosphere, int size, int x, int y, boolean hasLife) {
        super(owner, cat, atmosphere, size, x, y, hasLife);
        earthDialog = Dialog.loadFromFile(Earth.class.getClassLoader().getResourceAsStream("dialogs/earth_dialog.json"));
        progressDialog = Dialog.loadFromFile(Earth.class.getClassLoader().getResourceAsStream("dialogs/earth_progress_dialog.json"));
    }

    @Override
    public void drawOnGlobalMap(GameContainer container, Graphics g, Camera camera, int tileX, int tileY) {
        if (!camera.isInViewport(globalX, globalY)) {
            return;
        }

        g.drawImage(ResourceManager.getInstance().getImage("earth"), camera.getXCoord(globalX), camera.getYCoord(globalY));
    }

    @Override
    public boolean canBeEntered() {
        return true;
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
        dumpScreen = null;
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

        // setting main quest timer
        world.getPlayer().getEarthState().setEvacuationState(new EvacuationState(world, world.getTurnCount() + 365 * 10));
    }

    @Override
    public void update(GameContainer container, World world) {
        if (world.getGlobalVariables().containsKey("quest.main.show_earth_dialog")) {
            return;
        }

        if (earthDialog.isOver()) {
            if (earthDialog.getReturnValue() == 1) {
                // player has chosen to dump research info

                int daysPassed = world.getTurnCount() - lastVisitTurn;
                Statement stmt;

                if (daysPassed > 50) {
                    // show research screen
                    if (dumpScreen == null) {
                        dumpScreen = new ProgressDumpScreen(world.getPlayer().getResearchState());
                        world.addOverlayWindow(dumpScreen);
                        return;
                    }
                    if (!dumpScreen.isOver()) {
                        return;
                    }

                    int totalScore = dumpResearch(world);
                    double scorePerTurn = (double) totalScore / (daysPassed);
                    stmt = new Statement(0, String.format("Let us see. You have brought us new %d points of data, giving %f points/day", totalScore, scorePerTurn), new Reply(0, 0, ""));

                    if (scorePerTurn < 0.01) {
                        world.getPlayer().increaseFailCount();
                        if (world.getPlayer().getFailCount() > 3) {
                            // unsatisfactory
                            stmt.replies[0] = new Reply(0, 3, "=continue=");
                        } else {
                            // poor
                            stmt.replies[0] = new Reply(0, 2, "=continue=");
                        }
                    } else {
                        // ok
                        stmt.replies[0] = new Reply(0, 1, "=continue=");
                    }
                    lastVisitTurn = world.getTurnCount();
                } else {
                    stmt = new Statement(0, "We are pleased to see you come back, but your flight was too short to judge your perfomance. Come back later after you have acquired more data", new Reply(0, -1, "Ok"));
                }
                progressDialog.putStatement(stmt);
                world.addOverlayWindow(progressDialog);
            }
            // just reset state
            earthDialog.enter(world);
        }
        if (progressDialog.isOver()) {
            if (progressDialog.getReturnValue() == -1) {
                world.setCurrentRoom(FailScreen.createRetirementFailScreen());
                return;
            }
            world.setCurrentRoom(owner);

            EarthScreen es = new EarthScreen();
            es.enter(world);
            world.setCurrentRoom(es);

        }

    }

    @Override
    public void draw(GameContainer container, Graphics g, Camera camera) {
        owner.draw(container, g, camera);
    }

    private int dumpResearch(World world) {
        world.onPlayerReturnToEarth();
        int result = 0;
        final ResearchState researchState = world.getPlayer().getResearchState();
        result += researchState.getGeodata().dumpAndGetVictoryPoints();
        for (ResearchProjectDesc desc : researchState.getCompletedProjects()) {
            result += desc.getScore();
            if (desc.getEarthProgress() != null) {
                for (EarthResearch res : desc.getEarthProgress()) {
                    world.addListener(res);
                    res.onStarted(world);
                }
            }
        }
        researchState.getCompletedProjects().clear();
        result += researchState.dumpAstroData();
        world.getPlayer().getEarthState().updateTechnologyLevel(result);
        return result;
    }

    @Override
    public boolean canBeLanded() {
        return false;
    }
}
