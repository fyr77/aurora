package ru.game.aurora.world.generation.quest.heritage;

import com.google.common.collect.Lists;
import org.newdawn.slick.GameContainer;
import ru.game.aurora.application.ResourceManager;
import ru.game.aurora.common.Drawable;
import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.world.AuroraTiledMap;
import ru.game.aurora.world.ITileMap;
import ru.game.aurora.world.MonsterController;
import ru.game.aurora.world.World;
import ru.game.aurora.world.dungeon.DungeonMonster;
import ru.game.aurora.world.equip.WeaponInstance;
import ru.game.aurora.world.planet.MonsterBehaviour;
import ru.game.aurora.world.planet.MonsterDesc;

import java.util.List;
import java.util.Set;

/**
 * Klisk mutant, increases counter when killed
 */
public class KliskMutant extends DungeonMonster
{
    private static final long serialVersionUID = 1L;

    private Dialog dialog;

    public KliskMutant(AuroraTiledMap map, int groupId, int objectId) {
        super(map, groupId, objectId);
    }

    public KliskMutant(int x, int y, ITileMap owner,Dialog killDialog) {
        super("Klisk Mutant"
                , x
                , y
                , null
                , 30
                , null
                , owner
                , new MonsterDesc("klisk_mutant", new Drawable("klisk_mutant"))
                , MonsterBehaviour.AGGRESSIVE
                , Lists.newArrayList(new WeaponInstance(ResourceManager.getInstance().getWeapons().getEntity("acid"))));
        controller = new MonsterController(owner, this);
        dialog = killDialog;
    }

    @Override
    public void update(GameContainer container, World world) {
        super.update(container, world);
        if (!isAlive()) {
            Integer killCount = (Integer) world.getGlobalVariable("heritage.monsters_killed", 0);
            logger.info("Killed {}th quest monster for heritage quest", killCount);
            world.getGlobalVariables().put("heritage.monsters_killed", killCount + 1);

            KliskMutantCorpseItem corpse = new KliskMutantCorpseItem();
            corpse.setPos(x, y);
            world.getCurrentDungeon().getMap().getObjects().add(corpse);

            world.addOverlayWindow(dialog);
        }
    }
}
