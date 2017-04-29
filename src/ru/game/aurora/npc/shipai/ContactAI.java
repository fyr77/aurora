package ru.game.aurora.npc.shipai;

import org.slf4j.LoggerFactory;
import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.world.Ship;
import ru.game.aurora.world.World;
import ru.game.aurora.world.space.NPCShip;
import ru.game.aurora.world.space.StarSystem;

/**
 * Created by di Grigio on 04.04.2017.
 */
public class ContactAI extends FollowAI {

    private static final long serialVersionUID = -9056563926892731497L;
    protected Dialog dialog;

    public ContactAI(Ship target, Dialog dialog) {
        super(target);
        this.dialog = dialog;
    }

    @Override
    public void update(NPCShip ship, World world, StarSystem currentSystem) {
        super.update(ship, world, currentSystem);

        if(ship.getDistance(target) < 4){
            // try to contact
            if(target instanceof Ship){
                if(dialog != null){
                    world.addOverlayWindow(dialog);
                }
                else{
                    LoggerFactory.getLogger(ContactAI.class).error("Contact failed. Dialog is null");
                }
            }
            else{
                LoggerFactory.getLogger(ContactAI.class).error("Contact failed. Target is not a player ship");
            }
        }
    }
}