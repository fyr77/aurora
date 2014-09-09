/**
 * Created with IntelliJ IDEA.
 * User: User
 * Date: 16.05.14
 * Time: 14:36
 */

package ru.game.aurora.world.quest;

import org.newdawn.slick.*;
import ru.game.aurora.application.*;
import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.dialog.DialogListener;
import ru.game.aurora.effects.Effect;
import ru.game.aurora.effects.ExplosionEffect;
import ru.game.aurora.player.earth.PrivateMessage;
import ru.game.aurora.player.research.projects.StarResearchProject;
import ru.game.aurora.util.EngineUtils;
import ru.game.aurora.world.*;
import ru.game.aurora.world.generation.WorldGenerator;
import ru.game.aurora.world.generation.WorldGeneratorPart;
import ru.game.aurora.world.space.GalaxyMapObject;
import ru.game.aurora.world.space.StarSystem;

import java.util.Map;


public class FasterThanLightQuestGenerator extends GameEventListener implements WorldGeneratorPart, DialogListener, IStateChangeListener<World> {
    private static final long serialVersionUID = 5277056878029046570L;

    private final int requiredTechLevel;

    private int state = 0;

    private StarSystem targetSystem;

    private ExplodingStar explodingStar;

    private class SolarWind extends BaseGameObject {
        private static final long serialVersionUID = 2;

        private transient Animation myAnim;

        private boolean alive = true;

        public SolarWind(int x, int y) {
            super(x, y);
        }

        private void loadAnim() {
            myAnim = ResourceManager.getInstance().getAnimation("solar_wind").copy();
            myAnim.setLooping(true);
            myAnim.setAutoUpdate(true);
        }

        @Override
        public void draw(GameContainer container, Graphics graphics, Camera camera, World world) {
            if (myAnim == null) {
                loadAnim();
            }
            graphics.drawAnimation(myAnim, camera.getXCoord(x), camera.getYCoord(y));
        }

        @Override
        public void update(GameContainer container, World world) {
            if (getDistance(explodingStar) < explodingStar.radius) {
                alive = false;
            }
        }

        @Override
        public boolean isAlive() {
            return alive;
        }

        @Override
        public String getScanDescription(World world) {
            return Localization.getText("journal", "ftl.solar_wind.desc");
        }

        @Override
        public void interact(World world) {
            world.getPlayer().getShip().setMovementSpeed(world.getPlayer().getShip().getMovementSpeed() + 1);
            GameLogger.getInstance().logMessage(Localization.getText("journal", "ftl.solar_wind_used"));
            alive = false;
        }

        @Override
        public Image getImage() {
            return myAnim.getCurrentFrame();
        }
    }

    private class ExplodingStar extends BaseGameObject {
        private static final long serialVersionUID = 1L;

        float radius = 0;

        float radiusIncrement = 0.25f;

        boolean growing = false;

        final Color color = new Color(255, 255, 255);

        public ExplodingStar() {
            super(0, 0);
        }

        @Override
        public String getScanDescription(World world) {
            return Localization.getText("journal", "ftl.star.desc");
        }

        @Override
        public void draw(GameContainer container, Graphics graphics, Camera camera, World world) {
            if (growing) {
                EngineUtils.drawCircleCentered(graphics, camera.getXCoord(0) + camera.getTileWidth() / 2, camera.getYCoord(0) + camera.getTileHeight() / 2, (int) (radius * camera.getTileWidth()), color, true);
            }
        }

        @Override
        public void update(GameContainer container, World world) {
            if (growing && world.isUpdatedThisFrame()) {
                radius += radiusIncrement;
                color.b = color.g = Math.max(0, color.g - (radiusIncrement * 5.0f) / 255.0f);
                if (Math.abs(radius - 1.5f) < 0.001) {
                    world.addOverlayWindow(Dialog.loadFromFile("dialogs/quest/faster_than_light/ftl_star_explode.json"));
                    ++state;
                }
                final Ship ship = world.getPlayer().getShip();

                if (Math.abs(radius - 3.0f) < 0.001) {
                    targetSystem.setRadius(targetSystem.getRadius() + 4);
                    radiusIncrement = 1;
                }

                if (Math.abs(radius - 6f) < 0.001) {
                    targetSystem.setRadius(targetSystem.getRadius() + 4);
                }

                if (Math.abs(radius - 7f) < 0.001) {
                    world.addOverlayWindow(Dialog.loadFromFile("dialogs/quest/faster_than_light/ftl_hyperspace_border_expand.json"));
                    world.getPlayer().getJournal().addQuestEntries("ftl", "run");
                    targetSystem.setRadius(targetSystem.getRadius() + 1);
                    radiusIncrement = 1.5f;

                    int solarWindCount = Configuration.getIntProperty("quest.ftl.solar_wind_count");
                    for (int i = 0; i < solarWindCount; ++i) {
                        SolarWind sw = new SolarWind(0, 0);
                        do {
                            targetSystem.setRandomEmptyPosition(sw);
                        } while (sw.getDistance(explodingStar) < explodingStar.radius);

                        targetSystem.getShips().add(sw);
                    }

                }

                if (radius > 8f) {
                    targetSystem.setRadius(targetSystem.getRadius() + 1);
                }


                if (ship.getDistance(this) < radius) {
                    Effect effect = new ExplosionEffect(ship.getX(), ship.getY(), "ship_explosion", false, true);
                    targetSystem.addEffect(effect);
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
            int idx = world.getGalaxyMap().getInternalMap()[targetSystem.getY()][targetSystem.getX()];
            world.getGalaxyMap().getGalaxyMapObjects().remove(idx);
            world.getGalaxyMap().setTileAt(targetSystem.getX(), targetSystem.getY(), -1);
            isAlive = false;
            world.getPlayer().getJournal().addQuestEntries("ftl", "escape");
            world.getPlayer().getEarthState().getMessages().add(new PrivateMessage("ftl", "news"));
            world.getPlayer().getShip().setPos(world.getPlayer().getShip().getX() - 10, world.getPlayer().getShip().getY() - 5);
            world.getPlayer().getShip().setMovementSpeed(1);
        }

        return false;
    }

    @Override
    public void updateWorld(World world) {
        world.addListener(this);

        targetSystem = WorldGenerator.generateRandomStarSystem(world, 0, 0, 0);
        targetSystem.setQuestLocation(true);
        targetSystem.setRadius(12);
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
    public String getLocalizedMessageForStarSystem(World world, GalaxyMapObject galaxyMapObject) {
        if (galaxyMapObject == targetSystem && state >= 2) {
            return Localization.getText("journal", "ftl.title");
        }
        return null;
    }

    @Override
    public void stateChanged(World world) {
        explodingStar.growing = true;
    }


}
