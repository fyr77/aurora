package ru.game.aurora.world.planet.nature;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import ru.game.aurora.application.Camera;
import ru.game.aurora.application.GameLogger;
import ru.game.aurora.player.research.projects.AnimalResearch;
import ru.game.aurora.world.BasePositionable;
import ru.game.aurora.world.World;
import ru.game.aurora.world.planet.*;

import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 04.12.12
 * Time: 17:00
 */
public class Animal extends BasePositionable implements PlanetObject {

    public static class AnimalCorpseItem implements InventoryItem {
        AnimalSpeciesDesc desc;

        public AnimalCorpseItem(AnimalSpeciesDesc desc) {
            this.desc = desc;
        }

        @Override
        public String getName() {
            return desc.getName();
        }

        @Override
        public void onReturnToShip(World world, int amount) {
            if (!desc.isOutopsyMade() && !world.getPlayer().getResearchState().containsResearchFor(desc)) {
                // this type of alien animal has never been seen before, add new research
                GameLogger.getInstance().logMessage("Added biology research for new alien animal species " + desc.getName());
                world.getPlayer().getResearchState().addNewAvailableProject(new AnimalResearch(desc));
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            AnimalCorpseItem that = (AnimalCorpseItem) o;

            if (desc != null ? !desc.equals(that.desc) : that.desc != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return desc != null ? desc.hashCode() : 0;
        }
    }

    private int hp;

    private AnimalSpeciesDesc desc;

    private Planet myPlanet;

    private static final Random r = new Random();

    private boolean pickedUp = false;

    private int turnsBeforeMove;

    public Animal(Planet p, int x, int y, AnimalSpeciesDesc desc) {
        super(x, y);
        this.desc = desc;
        this.myPlanet = p;
        this.hp = desc.getHp();
        turnsBeforeMove = desc.getSpeed();
    }

    @Override
    public void update(GameContainer container, World world) {
        if (hp <= 0) {
            return;
        }
        if (!world.isUpdatedThisFrame()) {
            return;
        }
        if (--turnsBeforeMove == 0) {
            turnsBeforeMove = desc.getSpeed();
            int newX = x + r.nextInt(2) - 1;
            int newY = y + r.nextInt(2) - 1;
            // if we want to attack landing party and it is close enough, move closer
            if (desc.getBehaviour() == AnimalSpeciesDesc.Behaviour.AGGRESSIVE) {
                LandingParty party = world.getPlayer().getLandingParty();


                final double distance = this.getDistance(party);
                if (distance < 1.5) { //1.5 because of diagonal cells
                    party.subtractHp(desc.getDamage());
                    GameLogger.getInstance().logMessage(getName() + " attacks! " + desc.getDamage() + " damage done, " + party.getHp() + " hp remaining");
                    newX = x;
                    newY = y;
                } else if (distance < 5) {
                    if (x < party.getX() - 1) {
                        newX = x + 1;
                    } else if (x > party.getX() + 1) {
                        newX = x - 1;
                    }

                    if (y < party.getY() - 1) {
                        newY = y + 1;
                    } else if (y > party.getY() + 1) {
                        newY = y - 1;
                    }
                }
            }


            newX = myPlanet.wrapX(newX);
            newY = myPlanet.wrapY(newY);

            if (SurfaceTypes.isPassible(this, myPlanet.getTileTypeAt(newX, newY))) {
                // change position, reset 'passible' flag on old tile and set on new one
                myPlanet.setTileTypeAt(x, y, (byte) ((~SurfaceTypes.OBSTACLE_MASK) & myPlanet.getTileTypeAt(x, y)));
                x = newX;
                y = newY;
                myPlanet.setTileTypeAt(x, y, (byte) (myPlanet.getTileTypeAt(x, y) | SurfaceTypes.OBSTACLE_MASK));
            }
        }
    }

    @Override
    public void draw(GameContainer container, Graphics graphics, Camera camera) {
        if (desc.getImage()== null || desc.getDeadImage() == null) {
            AnimalGenerator.getInstance().getImageForAnimal(desc);
        }
        final Image image = hp > 0 ? desc.getImage() : desc.getDeadImage();
        graphics.drawImage(image, camera.getXCoordWrapped(x, myPlanet.getWidth()), camera.getYCoordWrapped(y, myPlanet.getHeight()));
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    @Override
    public void setPos(int newX, int newY) {
        x = newX;
        y = newY;
    }

    public AnimalSpeciesDesc getDesc() {
        return desc;
    }

    public int getHp() {
        return hp;
    }

    public void setHp(int hp) {
        this.hp = hp;
    }

    @Override
    public boolean canBePickedUp() {
        return hp <= 0;
    }

    @Override
    public boolean canBeShotAt() {
        return hp > 0;
    }

    @Override
    public void onShotAt(int damage) {
        hp -= damage;
        if (hp <= 0) {
            // clean obstacle flag
            myPlanet.setTileTypeAt(x, y, (byte) ((~SurfaceTypes.OBSTACLE_MASK) & myPlanet.getTileTypeAt(x, y)));
        }
    }

    @Override
    public void onPickedUp(World world) {
        pickedUp = true;
        GameLogger.getInstance().logMessage("Picked up " + getName());
        world.getPlayer().getLandingParty().pickUp(world, new AnimalCorpseItem(desc));
    }

    @Override
    public boolean isAlive() {
        // object is alive untill picked up, even if animal is actually dead
        return !pickedUp;
    }

    @Override
    public String getName() {
        if (hp > 0) {
            return desc.getName();
        } else {
            return desc.getName() + " corpse";
        }
    }

    @Override
    public void printStatusInfo() {
        if (hp <= 0) {
        }
    }


}
