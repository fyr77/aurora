package ru.game.aurora.world.planet.nature;

import jgame.platform.JGEngine;
import ru.game.aurora.application.Camera;
import ru.game.aurora.world.GameObject;
import ru.game.aurora.world.Positionable;
import ru.game.aurora.world.World;
import ru.game.aurora.world.planet.Planet;

import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 04.12.12
 * Time: 17:00
 */
public class Animal implements GameObject, Positionable {
    private int x;

    private int y;

    private int hp;

    private AnimalSpeciesDesc desc;

    private Planet myPlanet;

    private static final Random r = new Random();

    public Animal(Planet p, int x, int y, AnimalSpeciesDesc desc) {
        this.x = x;
        this.y = y;
        this.desc = desc;
        this.myPlanet = p;
        this.hp = desc.getHp();
    }

    @Override
    public void update(JGEngine engine, World world) {
        if (!world.isUpdatedThisFrame()) {
            return;
        }
        if (r.nextInt(5) == 0) {
            x += r.nextInt(2) - 1;
            y += r.nextInt(2) - 1;
            x = myPlanet.wrapX(x);
            y = myPlanet.wrapY(y);
        }
    }

    @Override
    public void draw(JGEngine engine, Camera camera) {
        engine.drawImage(desc.getSpriteName(), camera.getXCoordWrapped(x, myPlanet.getWidth()), camera.getYCoordWrapped(y, myPlanet.getHeight()));
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
}
