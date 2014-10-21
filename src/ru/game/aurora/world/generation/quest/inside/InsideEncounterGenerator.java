package ru.game.aurora.world.generation.quest.inside;

import org.newdawn.slick.Animation;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import ru.game.aurora.application.*;
import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.dialog.DialogListener;
import ru.game.aurora.npc.SingleShipEvent;
import ru.game.aurora.player.Resources;
import ru.game.aurora.player.earth.PrivateMessage;
import ru.game.aurora.player.research.ResearchProjectDesc;
import ru.game.aurora.world.*;
import ru.game.aurora.world.generation.WorldGeneratorPart;
import ru.game.aurora.world.planet.BasePlanet;
import ru.game.aurora.world.space.StarSystem;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 */
public class InsideEncounterGenerator implements WorldGeneratorPart {

    private static final long serialVersionUID = -6641862933214448810L;

    private class ParallelWorld extends StarSystem {

        private static final long serialVersionUID = 1946685308754087835L;

        private int planetsDestroyed = 0;

        private StarSystem entranceLocation;

        private boolean firstAttackMessageShown = false;

        private boolean firstDestroyMessageShown = false;

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
                    if (!firstDestroyMessageShown) {
                        world.addOverlayWindow(Dialog.loadFromFile("dialogs/encounters/inside_first_destroyed.json"));
                        firstDestroyMessageShown = true;
                    }
                    if (planetsDestroyed > Configuration.getIntProperty("quest.inside.planets_to_destroy")) {
                        world.setCurrentRoom(entranceLocation);
                        entranceLocation.returnTo(world);
                        world.addOverlayWindow(Dialog.loadFromFile("dialogs/encounters/inside_leave.json"));
                        if (world.getPlayer().getInventory().count(Resources.CELLS_FROM_PARALLEL_WORLD) > 0) {
                            world.getPlayer().getJournal().addQuestEntries("inside", "end_good");
                        } else {
                            world.getPlayer().getJournal().addQuestEntries("inside", "end_bad");
                        }
                        world.getPlayer().getJournal().questCompleted("inside");
                        world.getGlobalVariables().remove("inside.in_parallel_universe");
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
                } else if (lastCall > (container.getTime() - 700)) {
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
                    if (!firstAttackMessageShown) {
                        world.addOverlayWindow(Dialog.loadFromFile("dialogs/encounters/inside_first_damage.json"));
                        world.getPlayer().getJournal().addQuestEntries("inside", "attacked");
                        firstAttackMessageShown = true;
                    }
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
            setRadius(50);
            this.entranceLocation = entranceLocation;
            setQuestLocation(true);

            final int planetCount = Configuration.getIntProperty("quest.inside.planets");
            for (int i = 0; i < planetCount; ++i) {
                final ParallelWorldPlanet pp = new ParallelWorldPlanet(world);
                setRandomEmptyPosition(pp, 0.2, 1.5);
                getObjects().add(pp);
            }
            setCanBeLeft(false);
        }

        @Override
        public boolean turnIsADay() {
            return false;
        }

        @Override
        public void draw(GameContainer container, Graphics g, Camera camera, World world) {
            g.setColor(backgroundColor);
            g.fillRect(0, 0, container.getWidth(), container.getHeight());
            super.draw(container, g, camera, world);
        }
    }

    public static class BioCellsItem extends BaseGameObject {
        private static final long serialVersionUID = 8752939890229707029L;

        @Override
        public void interact(World world) {
            world.getPlayer().changeResource(world, Resources.CELLS_FROM_PARALLEL_WORLD, CommonRandom.getRandom().nextInt(3));
            ResearchProjectDesc research = world.getResearchAndDevelopmentProjects().getResearchProjects().remove("parallel_world_bio_data");
            if (research != null) {
                world.getPlayer().getResearchState().addNewAvailableProject(research);
            }
        }
    }

    private class Entrance extends BaseGameObject {
        private static final long serialVersionUID = 4517557057799335126L;

        private transient Animation myAnim;

        private void loadAnim() {
            myAnim = ResourceManager.getInstance().getAnimation("black_hole").copy();
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
        public String getScanDescription(World world) {
            return Localization.getText("journal", "inside.scan_desc");
        }

        @Override
        public void interact(World world) {
            ParallelWorld pw = new ParallelWorld(world, world.getCurrentStarSystem());
            pw.enter(world);
            world.getPlayer().getShip().setPos(0, 0);
            world.setCurrentRoom(pw);
            isAlive = false;
            world.getPlayer().getJournal().addQuestEntries("inside", "enter");
            world.addOverlayWindow(Dialog.loadFromFile("dialogs/encounters/inside_entered.json"));
            world.getGlobalVariables().put("inside.in_parallel_universe", true);
        }

        @Override
        public boolean canBeInteracted() {
            return true;
        }
    }


    @Override
    public void updateWorld(World world) {
        final Dialog starsystemEnterDialog = Dialog.loadFromFile("dialogs/encounters/inside_entrance_detected.json");
        starsystemEnterDialog.addListener(new DialogListener() {
            private static final long serialVersionUID = -4264540383261831865L;

            @Override
            public void onDialogEnded(World world, Dialog dialog, int returnCode, Map<String, String> flags) {
                world.getPlayer().getJournal().addQuestEntries("inside", "start");
            }
        });
        world.addListener(new SingleShipEvent(
                Configuration.getDoubleProperty("quest.inside.chance")
                , new Entrance()
                , starsystemEnterDialog
        ));

        world.getResearchAndDevelopmentProjects().getEngineeringProjects().put(
                "super_medpack"
                , new SuperMedpack.SuperMedpackCraftProject("super_medpack_craft", "super_medpack", 10));

        world.getResearchAndDevelopmentProjects().getResearchProjects().get("parallel_world_bio_data").addListener(new IStateChangeListener<World>() {
            private static final long serialVersionUID = -3766979340652869635L;

            @Override
            public void stateChanged(World param) {
                param.getPlayer().getEarthState().getMessages().add(new PrivateMessage("inside", "news"));
            }
        });
    }
}
