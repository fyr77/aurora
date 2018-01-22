package ru.game.aurora.npc;

import ru.game.aurora.application.CommonRandom;
import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.npc.shipai.FollowAI;
import ru.game.aurora.world.GameEventListener;
import ru.game.aurora.world.World;
import ru.game.aurora.world.equip.WeaponInstance;

import ru.game.aurora.world.space.NPCShip;
import ru.game.aurora.world.space.StarSystem;

import java.util.ArrayList;


public class ZorsanFightEvent extends GameEventListener {

    private static final long serialVersionUID = 1L;

    protected final double chance;

    protected ArrayList<NPCShip> ships = new ArrayList<>();

    protected Dialog starsystemEnterDialog = null;

    public ZorsanFightEvent(double chance, ArrayList<NPCShip> ships) {
        this.chance = chance;
        this.ships = ships;
    }

    public ZorsanFightEvent(double chance, ArrayList<NPCShip> ships, Dialog starsystemEnterDialog) {
        this.chance = chance;
        this.ships = ships;
        this.starsystemEnterDialog = starsystemEnterDialog;
    }

    @Override
    public boolean onPlayerEnterStarSystem(World world, StarSystem ss) {
        // do not spawn in quest star systems
        if (ss.isQuestLocation()) {
            return false;
        }

        if (CommonRandom.getRandom().nextDouble() < chance) {

            if (starsystemEnterDialog != null) {
                world.addOverlayWindow(starsystemEnterDialog);
            }

            for (int i = 0; i < 2; i++) {
                ships.get(i).setPos(-1 + i, 1 + i);
            }

            for (int i = 2; i < 5; i++) {
                ships.get(i).setPos(i, 1 + i);
                ships.get(i).setAi(new FollowAI(world.getPlayer().getShip()));
            }

            for (int i = 0; i < ships.size(); i++) {
                ss.getShips().add(ships.get(i));
            }

            isAlive = false;

            return true;
        }
        return false;
    }
}