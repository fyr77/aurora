package ru.game.aurora.world.planet.nature;

import ru.game.aurora.world.planet.Planet;

/**
 * Lava fields damage player if he steps on them, and also they work like a cellular automata in a 'life' game
 */
public class Lava {
    private boolean[][] lavaLocations;

    private Planet myPlanet;

    public Lava(Planet p) {
        this.lavaLocations = new boolean[p.getHeight()][p.getWidth()];
        this.myPlanet = p;
    }

    public void update() {

    }
}
