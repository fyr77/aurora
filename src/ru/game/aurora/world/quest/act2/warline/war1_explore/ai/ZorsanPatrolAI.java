package ru.game.aurora.world.quest.act2.warline.war1_explore.ai;

import ru.game.aurora.npc.shipai.FollowAI;
import ru.game.aurora.world.World;
import ru.game.aurora.world.quest.act2.warline.war1_explore.QuestStarSystemEncounter;
import ru.game.aurora.world.space.NPCShip;
import ru.game.aurora.world.space.StarSystem;

/**
 * Created by di Grigio on 04.09.2017.
 * FollowAI with player ship detection and notify zorsan fleet logic
 */
public class ZorsanPatrolAI extends FollowAI {

    private static final long serialVersionUID = -8440604339605337544L;

    private final QuestStarSystemEncounter encounter;

    public ZorsanPatrolAI(QuestStarSystemEncounter encounter, final NPCShip targetStation){
        super(targetStation);
        this.encounter = encounter;
    }

    @Override
    public void update(NPCShip ship, World world, StarSystem currentSystem) {
        // update movement
        super.update(ship, world, currentSystem);

        if(encounter.detectPlayerShip(world, ship, currentSystem)){
            return;
        }

        // if player ship not found near - despawn patrol ship
        if(target.getX() == ship.getX() || target.getY() == ship.getY()){
            if(ship.getDistance(target) == 1){
                // if ship near the station and on straight line
                currentSystem.getShips().remove(ship);
            }
        }
        else{
            if(Math.ceil(ship.getDistance(target)) == 2){
                // if ship near the station and on diagonal line
                currentSystem.getShips().remove(ship);
            }
        }
    }
}