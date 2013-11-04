/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 13.09.13
 * Time: 14:12
 */
package ru.game.aurora.world.dungeon;


import ru.game.aurora.world.AuroraTiledMap;
import ru.game.aurora.world.World;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class KillAllMonstersCondition implements IVictoryCondition {
    private static final long serialVersionUID = 4371002710032440409L;

    // if set, must kill all monsters with these tags
    private Set<String> tags = null;

    public KillAllMonstersCondition(AuroraTiledMap map, int groupId, int objectId) {
        final String tagString = map.getMap().getObjectProperty(groupId, objectId, "tags", null);
        if (tagString != null) {
            tags = new HashSet<>();
            Collections.addAll(tags, tagString.split(","));
        }
    }

    @Override
    public boolean isSatisfied(World world) {
        return true;
        /*for (PlanetObject po : world.getCurrentDungeon().getMap().getObjects()) {
            if (DungeonMonster.class.isAssignableFrom(po.getClass())) {
                if (tags == null) {
                    return false;
                } else {
                    DungeonMonster dm = (DungeonMonster) po;
                    if (dm.getTags() == null) {
                        continue;
                    }
                    for (String tag: tags) {
                        if (dm.getTags().contains(tag)) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;*/
    }
}
