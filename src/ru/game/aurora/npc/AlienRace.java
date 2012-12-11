/**
 * User: jedi-philosopher
 * Date: 09.12.12
 * Time: 18:34
 */
package ru.game.aurora.npc;

import ru.game.aurora.world.space.StarSystem;

public class AlienRace {
    private String name;

    /**
     * 0-3 - hate: will attack on sight
     * 4-6 - dislike: will not attack, but also will not communicate
     * 7-9 - neutral: will communicate, can occasionally help
     * 10-12 - like: will help and easily share information
     */
    private int relationToPlayer;

    private StarSystem homeworld;

    public AlienRace(String name, int relationToPlayer, StarSystem homeworld) {
        this.name = name;
        this.relationToPlayer = relationToPlayer;
        this.homeworld = homeworld;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getRelationToPlayer() {
        return relationToPlayer;
    }

    public void setRelationToPlayer(int relationToPlayer) {
        this.relationToPlayer = relationToPlayer;
    }

    public StarSystem getHomeworld() {
        return homeworld;
    }

    public void setHomeworld(StarSystem homeworld) {
        this.homeworld = homeworld;
    }
}
