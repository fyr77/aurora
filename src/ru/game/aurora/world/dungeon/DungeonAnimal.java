package ru.game.aurora.world.dungeon;

import ru.game.aurora.util.CollectionUtils;
import ru.game.aurora.world.AuroraTiledMap;
import ru.game.aurora.world.planet.Planet;
import ru.game.aurora.world.planet.nature.Animal;

/**
 * Random animal from a planet where dungeon is located.
 * AuroraTiledMap.getUserData() should return a planet object
 */
public class DungeonAnimal extends Animal
{
    public DungeonAnimal(AuroraTiledMap map, int groupId, int objectId) {
        super((Planet)map.getUserData()
                , map.getMap().getObjectX(groupId, objectId)
                , map.getMap().getObjectY(groupId, objectId)
                , CollectionUtils.selectRandomElement(((Planet)map.getUserData()).getFloraAndFauna().getAnimalSpecies()));
    }
}
