/**
 * Created with IntelliJ IDEA.
 * User: User
 * Date: 16.05.14
 * Time: 14:36
 */

package ru.game.aurora.world.quest;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import ru.game.aurora.application.Camera;
import ru.game.aurora.application.Configuration;
import ru.game.aurora.application.Localization;
import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.dialog.DialogListener;
import ru.game.aurora.effects.Effect;
import ru.game.aurora.effects.ExplosionEffect;
import ru.game.aurora.player.research.projects.StarResearchProject;
import ru.game.aurora.util.EngineUtils;
import ru.game.aurora.world.*;
import ru.game.aurora.world.generation.WorldGenerator;
import ru.game.aurora.world.generation.WorldGeneratorPart;
import ru.game.aurora.world.space.BaseSpaceObject;
import ru.game.aurora.world.space.GalaxyMapObject;
import ru.game.aurora.world.space.StarSystem;

import java.util.Map;


public class FasterThanLightQuestGenerator extends GameEventListener implements WorldGeneratorPart, DialogListener, IStateChangeListener
{
    private static final long serialVersionUID = 5277056878029046570L;

    private final int requiredTechLevel;

    private int state = 0;

    private StarSystem targetSystem;

    private ExplodingStar explodingStar;

    private class ExplodingStar extends BaseSpaceObject
    {
        private static final long serialVersionUID = 1621176923244251636L;

        int radius = 0;

        int radiusIncrement = 1;

        boolean growing = false;

        Color color = Color.red;

        public ExplodingStar() {
            super(0, 0);
        }

        @Override
        public void draw(GameContainer container, Graphics graphics, Camera camera) {
            if (growing) {
                EngineUtils.drawCircleCentered(graphics, camera.getXCoord(0), camera.getYCoord(0), radius, color, true);
            }
        }

        @Override
        public void update(GameContainer container, World world) {
            if (growing && world.isUpdatedThisFrame()) {
                radius += radiusIncrement;

                if (radius == 3) {
                    world.addOverlayWindow(Dialog.loadFromFile("dialogs/quest/faster_than_light/ftl_star_explode.json"));
                    ++state;
                }
                final Ship ship = world.getPlayer().getShip();

                if ((targetSystem.getRadius() - Math.max(Math.abs(ship.getX()), ship.getY()) < 4) && radiusIncrement == 1) {
                    targetSystem.setRadius(targetSystem.getRadius() + 10);
                    world.addOverlayWindow(Dialog.loadFromFile("dialogs/quest/faster_than_light/ftl_hyperspace_border_expand.json"));
                    world.getPlayer().getJournal().addQuestEntries("ftl", "run");
                    radiusIncrement = 3;
                }

                if (ship.getDistance(this) < radius) {
                    Effect effect = new ExplosionEffect(ship.getX(), ship.getY(), "ship_explosion", false, true);
                    effect.setEndListener(new GameOverEffectListener());
                }
            }
        }
    }

    public FasterThanLightQuestGenerator() {
        requiredTechLevel = Configuration.getIntProperty("quest.ftl.start_tech_level");
    }

    @Override
    public boolean onReturnToEarth(World world) {
        if (state == 0 && world.getPlayer().getEarthState().getTechnologyLevel() > requiredTechLevel) {
            ++state;
            Dialog startDialog = Dialog.loadFromFile("dialogs/quest/faster_than_light/ftl_start.json");
            startDialog.addListener(this);
            world.getPlayer().getEarthState().getEarthSpecialDialogs().add(startDialog);
            world.getPlayer().getJournal().addQuestEntries("ftl", "start");
            return false;
        }
        return false;
    }

    @Override
    public boolean onPlayerEnterStarSystem(World world, StarSystem ss) {
        if (state == 2 && ss == targetSystem) {
            world.addOverlayWindow(Dialog.loadFromFile("dialogs/quest/faster_than_light/ftl_enter.json"));
        }

        return false;
    }

    @Override
    public boolean onPlayerLeftStarSystem(World world, StarSystem ss) {
        if (state == 3 && ss == targetSystem) {
            world.addOverlayWindow(Dialog.loadFromFile("dialogs/quest/faster_than_light/ftl_escaped.json"));
            int idx = world.getGalaxyMap().getMap()[targetSystem.getY()][ targetSystem.getX()];
            world.getGalaxyMap().getObjects().remove(idx);
            world.getGalaxyMap().setTileAt(targetSystem.getX(), targetSystem.getY(), -1);
            isAlive = false;
            world.getPlayer().getJournal().addQuestEntries("ftl", "escape");
        }

        return false;
    }

    @Override
    public void updateWorld(World world) {
        world.addListener(this);

        targetSystem = WorldGenerator.generateRandomStarSystem(world, 0, 0, 0);
        targetSystem.setQuestLocation(true);
        explodingStar = new ExplodingStar();
        targetSystem.getShips().add(explodingStar);
        world.getGalaxyMap().addObjectAtDistance(targetSystem, (Positionable) world.getGlobalVariables().get("solar_system"), 50);

        world.getGlobalVariables().put("ftl.coords", targetSystem.getCoordsString());
    }

    @Override
    public void onDialogEnded(World world, Dialog dialog, int returnCode, Map<String, String> flags) {
        if (dialog.getId().equals("ftl_start")) {
            ++state;
            final StarResearchProject desc = new StarResearchProject(targetSystem);
            desc.addListener(this);
            world.getPlayer().getResearchState().addNewAvailableProject(desc);
        }
    }

    @Override
    public String getLocalizedMessageForStarSystem(GalaxyMapObject galaxyMapObject) {
        if (galaxyMapObject == targetSystem) {
            return Localization.getText("journal", "ftl.title");
        }
        return null;
    }

    @Override
    public void stateChanged(World world) {
        explodingStar.growing = true;
    }


}
