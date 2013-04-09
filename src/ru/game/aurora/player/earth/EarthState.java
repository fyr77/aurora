/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 04.03.13
 * Time: 13:55
 */
package ru.game.aurora.player.earth;

import ru.game.aurora.world.World;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

public class EarthState implements Serializable {
    private static final long serialVersionUID = 7511734171340918376L;

    private List<PrivateMessage> messages = new LinkedList<PrivateMessage>();

    private int technologyLevel = 0;

    private EvacuationState evacuationState;

    public void updateTechnologyLevel(int value) {
        technologyLevel += value;
    }

    public List<PrivateMessage> getMessages() {
        return messages;
    }

    public EvacuationState getEvacuationState() {
        return evacuationState;
    }

    public void setEvacuationState(EvacuationState evacuationState) {
        this.evacuationState = evacuationState;
    }

    public void update(World world) {
        if (evacuationState != null) {
            evacuationState.update(world);
        }
    }
}
