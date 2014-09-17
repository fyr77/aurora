package ru.game.aurora.world.generation.quest.inside;

import org.newdawn.slick.Animation;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import ru.game.aurora.application.Camera;
import ru.game.aurora.application.CommonRandom;
import ru.game.aurora.application.Configuration;
import ru.game.aurora.application.ResourceManager;
import ru.game.aurora.npc.SingleShipEvent;
import ru.game.aurora.world.BaseGameObject;
import ru.game.aurora.world.GameObject;
import ru.game.aurora.world.Ship;
import ru.game.aurora.world.World;
import ru.game.aurora.world.generation.WorldGeneratorPart;
import ru.game.aurora.world.planet.BasePlanet;
import ru.game.aurora.world.space.StarSystem;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 */
public class InsideEncounterGenerator implements WorldGeneratorPart {

    private static final long serialVersionUID = -6641862933214448810L;

    private class ParallelWorld extends StarSystem {

        private static final long serialVersionUID = 1946685308754087835L;

        private int planetsDestroyed = 0;

        private StarSystem entranceLocation;

        private final Color backgroundColor = new Color(125, 32, 34);

        private class ParallelWorldPlanet extends BaseParallelWorldObject {
            private int cells;

            private List<ParallelWorldCell> myCells;

            private static final long serialVersionUID = 5859875096919217802L;

            private ParallelWorldPlanet(World world) {
                super(Color.red, 3);
                cells = CommonRandom.getRandom().nextInt(Configuration.getIntProperty("quest.inside.max_cells_per_planet")) + 1;
                myCells = new ArrayList<>(cells);
            }

            private void checkCells(World world) {
                for (Iterator<ParallelWorldCell> iterator = myCells.iterator(); iterator.hasNext(); ) {
                    ParallelWorldCell c = iterator.next();
                    if (!c.isAlive()) {
                        iterator.remove();
                    }
                }
                for (int i = 0; i < cells - myCells.size(); ++i) {
                    final ParallelWorldCell e = new ParallelWorldCell(world);
                    myCells.add(e);
                    e.setPos(x, y);
                    getObjects().add(e);
                }
            }


            @Override
            public void update(GameContainer container, World world) {
                super.update(container, world);
                checkCells(world);
                if (!isAlive) {
                    ++planetsDestroyed;
                    if (planetsDestroyed > Configuration.getIntProperty("quest.inside.planets_to_destroy")) {
                        world.setCurrentRoom(entranceLocation);
                        entranceLocation.returnTo(world);
                    }
                }
            }
        }

        private class ParallelWorldCell extends BaseParallelWorldObject {
            private static final long serialVersionUID = 2073909416097753886L;

            private long lastCall;

            private boolean agressive = false;

            public ParallelWorldCell(World world) {
                super(Color.orange, 1);
            }

            @Override
            public void update(GameContainer container, World world) {
                super.update(container, world);
                final Ship target = world.getPlayer().getShip();
                if (target.getDistance(this) > 10 && !agressive) {
                    return;
                }
                agressive = true;
                if (lastCall == 0) {
                    lastCall = container.getTime();
                } else if (lastCall > (container.getTime() - 1000)) {
                    return;
                }

                lastCall = container.getTime();
                int dx = 0;
                int dy = 0;
                if (target.getX() < x) {
                    dx = -1;
                } else if (target.getX() > x) {
                    dx = 1;
                }
                if (target.getY() < y) {
                    dy = 1;
                } else if (target.getY() > y) {
                    dy = -1;
                }

                if (dx != 0 && dy != 0) {
                    if (CommonRandom.getRandom().nextBoolean()) {
                        dx = 0;
                    } else {
                        dy = 0;
                    }
                }

                if (dx == -1) {
                    moveLeft();
                } else if (dx == 1) {
                    moveRight();
                }

                if (dy == -1) {
                    moveDown();
                } else if (dy == 1) {
                    moveUp();
                }


                if (target.getDistance(this) <= 1) {
                    target.onAttack(world, this, Configuration.getIntProperty("quest.inside.cell_damage"));
                    onAttack(world, this, Integer.MAX_VALUE);
                    isAlive = false;
                }

            }

            @Override
            public void onAttack(World world, GameObject attacker, int damaged) {
                super.onAttack(world, attacker, damaged);
                agressive = true;
            }
        }


        public ParallelWorld(World world, StarSystem entranceLocation) {
            super(null, null, 0, 0);
            setPlanets(new BasePlanet[0]);
            setRadius(40);
            this.entranceLocation = entranceLocation;
            setQuestLocation(true);

            final int planetCount = Configuration.getIntProperty("quest.inside.planets");
            for (int i = 0; i < planetCount; ++i) {
                final ParallelWorldPlanet pp = new ParallelWorldPlanet(world);
                setRandomEmptyPosition(pp);
                getObjects().add(pp);
            }
            setCanBeLeft(false);
        }

        @Override
        public void draw(GameContainer container, Graphics g, Camera camera, World world) {
            g.setColor(backgroundColor);
            g.fillRect(0, 0, container.getWidth(), container.getHeight());
            super.draw(container, g, camera, world);
        }
    }

    private class Entrance extends BaseGameObject {
        private static final long serialVersionUID = 4517557057799335126L;

        private transient Animation myAnim;

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
        public void interact(World world) {
            ParallelWorld pw = new ParallelWorld(world, world.getCurrentStarSystem());
            pw.enter(world);
            world.setCurrentRoom(pw);
            isAlive = false;
        }

        @Override
        public boolean canBeInteracted() {
            return true;
        }
    }


    @Override
    public void updateWorld(World world) {
        world.addListener(new SingleShipEvent(Configuration.getDoubleProperty("quest.inside.chance"), new Entrance()));
    }
}
