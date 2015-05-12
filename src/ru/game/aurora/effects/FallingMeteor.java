package ru.game.aurora.effects;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Vector2f;
import ru.game.aurora.application.*;
import ru.game.aurora.common.Drawable;
import ru.game.aurora.util.EngineUtils;
import ru.game.aurora.world.BasePositionable;
import ru.game.aurora.world.World;
import ru.game.aurora.world.planet.LandingParty;
import ru.game.aurora.world.planet.Planet;
import ru.game.aurora.world.planet.SurfaceTileMap;
import ru.game.aurora.world.planet.SurfaceTypes;

/**
 * Falls from the bottom of screen to a given screen coordinate
 */
public class FallingMeteor extends Effect {

    protected final Vector2f currentPos;

    private final Vector2f movementDir;

    private final Vector2f target;

    public FallingMeteor(int targetScreenX, int targetScreenY) {
        super(CommonRandom.getRandom().nextInt(AuroraGame.tilesX * AuroraGame.tileSize), 0, HIGH_PRIORITY, DrawOrder.FRONT);
        this.drawable = new Drawable("meteor", true);
        this.currentPos = new Vector2f(x, y);
        this.target = new Vector2f(targetScreenX, targetScreenY);


        movementDir = new Vector2f(this.target.getX() - this.currentPos.getX(), this.target.getY() - this.currentPos.getY());
        float movementSpeed = movementDir.length() / 180; // should fly in approx 3 seconds, 180 frames
        movementDir.normalise();
        movementDir.scale(movementSpeed);
    }


    @Override
    public boolean isOver() {
        return !isAlive;
    }

    @Override
    public void draw(GameContainer container, Graphics g, Camera camera, World world) {
        drawable.draw(g, currentPos.getX(), currentPos.getY(), false);
    }

    @Override
    public void update(GameContainer container, World world) {
        currentPos.add(movementDir);
        if (currentPos.distance(target) < AuroraGame.tileSize) {
            isAlive = false;
            final Planet planet = (Planet) world.getCurrentRoom();
            planet.getController().addEffect(new ExplosionEffect(
                    (int) currentPos.getX()
                    , (int) currentPos.getY()
                    , "surface_explosion_large"
                    , true
                    , true
            ));

            final LandingParty landingParty = world.getPlayer().getLandingParty();
            final Camera cam = world.getCamera();
            final BasePositionable other = new BasePositionable(
                    cam.getPointTileX((int) currentPos.getX()) + world.getCamera().getTarget().getX() - world.getCamera().getNumTilesX() / 2
                    , cam.getPointTileY((int) currentPos.getY()) + world.getCamera().getTarget().getY() - world.getCamera().getNumTilesY() / 2
            );
            if (landingParty.getDistanceWrapped(
                    other, planet.getWidth(), planet.getHeight()
            ) < Configuration.getIntProperty("environment.meteor.area")) {
                landingParty.subtractHp(world, Configuration.getIntProperty("environment.meteor.damage"));
                GameLogger.getInstance().logMessage(Localization.getText("gui", "surface.meteor_strike"));

                // terraforming

                byte[][] surface = ((SurfaceTileMap) planet.getMap()).getSurface();
                int startX = (other.getX() - 2) - other.getX() % 2;
                int startY = (other.getY() - 2) - other.getY() % 2;

                for (int xx = startX; xx < startX + 6; xx += 2) {
                    for (int yy = startY; yy < startY + 6; yy += 2) {
                        if (CommonRandom.getRandom().nextBoolean()) {

                            surface[EngineUtils.wrap(yy, planet.getHeight())][EngineUtils.wrap(xx, planet.getWidth())] |= SurfaceTypes.MOUNTAINS_MASK | SurfaceTypes.OBSTACLE_MASK;
                            surface[EngineUtils.wrap(yy + 1, planet.getHeight())][EngineUtils.wrap(xx, planet.getWidth())] |= SurfaceTypes.MOUNTAINS_MASK | SurfaceTypes.OBSTACLE_MASK;
                            surface[EngineUtils.wrap(yy, planet.getHeight())][EngineUtils.wrap(xx + 1, planet.getWidth())] |= SurfaceTypes.MOUNTAINS_MASK | SurfaceTypes.OBSTACLE_MASK;
                            surface[EngineUtils.wrap(yy + 1, planet.getHeight())][EngineUtils.wrap(xx + 1, planet.getWidth())] |= SurfaceTypes.MOUNTAINS_MASK | SurfaceTypes.OBSTACLE_MASK;

                        }
                    }
                }

                planet.setNearestFreePoint(landingParty, landingParty.getX(), landingParty.getY());
            }
        }
    }
}
