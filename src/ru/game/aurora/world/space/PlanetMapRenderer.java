package ru.game.aurora.world.space;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Rectangle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.game.aurora.application.Camera;
import ru.game.aurora.application.ResourceManager;
import ru.game.aurora.world.GameObject;
import ru.game.aurora.world.Movable;
import ru.game.aurora.world.ScanGroup;
import ru.game.aurora.world.World;
import ru.game.aurora.world.planet.Planet;

import java.util.Random;

/**
 * Renders planet surface to an image, that can be used as a landscape map
 */
public class PlanetMapRenderer {
    private static final Color ANIMAL_COLOR = new Color(0, 255, 0, 200);
    private static final Color RESOURCE_COLOR = new Color(255, 255, 0, 200);
    private static final Color ANOMALY_COLOR = new Color(255, 0, 0, 200);
    private static final Color BACKGROUND_COLOR = new Color(0, 0, 0, 200);

    private static final Logger logger = LoggerFactory.getLogger(PlanetMapRenderer.class);

    public static Image createMap(World world, Planet planet, Rectangle container, boolean showOverlay, boolean showLandingParty) {
        try {
            Image result = new Image((int) container.getWidth(), (int) container.getHeight());
            Graphics g = result.getGraphics();

            final float newTileWidth = container.getWidth() / (float) planet.getWidth();
            final float newTileHeight = container.getHeight() / (float) planet.getHeight();
            Camera myCamera = new Camera(0, 0, planet.getWidth(), planet.getHeight(), newTileWidth, newTileHeight);

            myCamera.setTarget(new Movable(planet.getWidth() / 2, planet.getHeight() / 2));
            planet.getSurface().drawLandscapeMap(g, myCamera);
            g.flush();
            if (showOverlay) {
                // create overlay with info about life and anomalies
                Image overlay = new Image((int) container.getWidth(), (int) container.getHeight());
                Graphics overlayGraphics = overlay.getGraphics();
                overlayGraphics.setColor(BACKGROUND_COLOR);
                overlayGraphics.fillRect(0, 0, container.getWidth(), container.getHeight());
                int maxRadius = (int) (container.getHeight() / 10);
                Random r = new Random(planet.hashCode()); // fixed-seed, so that runs on same planet produce same results
                for (GameObject po : planet.getPlanetObjects()) {
                    float objectX = myCamera.getXCoord(po.getX());
                    float objectY = myCamera.getYCoord(po.getY());
                    if (po.getScanGroup() == ScanGroup.BIO) {
                        overlayGraphics.setColor(ANIMAL_COLOR);
                    } else if (po.getScanGroup() == ScanGroup.RESOURCE) {
                        overlayGraphics.setColor(RESOURCE_COLOR);
                    } else if (po.getScanGroup() == ScanGroup.OTHER) {
                        overlayGraphics.setColor(ANOMALY_COLOR);
                    } else {
                        continue;
                    }

                    float xRadius = r.nextInt(maxRadius) + maxRadius / 2;
                    float yRadius = r.nextInt(maxRadius) + maxRadius / 2;

                    float ovalX = objectX - xRadius + r.nextInt((int) (xRadius / 2));
                    float ovalY = objectY - yRadius + r.nextInt((int) (yRadius / 2));
                    overlayGraphics.fillOval(ovalX, ovalY, xRadius, yRadius);
                }
                overlayGraphics.flush();
                g.drawImage(overlay, 0, 0);
            }
            if (showLandingParty) {
                Image shuttle = ResourceManager.getInstance().getImage("shuttle").getScaledCopy(0.5f);
                g.drawImage(shuttle, myCamera.getXCoord(planet.getShuttle().getX()), myCamera.getYCoordWrapped((int) (planet.getShuttle().getY() - shuttle.getHeight() / newTileHeight), planet.getHeight()));
                Image landing_party = ResourceManager.getInstance().getImage("awayteam").getScaledCopy(0.5f);
                g.drawImage(landing_party
                        , myCamera.getXCoord(world.getPlayer().getLandingParty().getX())
                        , myCamera.getYCoordWrapped((int) (world.getPlayer().getLandingParty().getY() - landing_party.getHeight() / newTileHeight), planet.getHeight())); // -1 so that legs of sprite showed point on map. not its head
            }
            g.flush();
            return result;
        } catch (SlickException e) {
            logger.error("Exception while generating planet surface image", e);
            return ResourceManager.getInstance().getImage("no_image");
        }
    }
}
