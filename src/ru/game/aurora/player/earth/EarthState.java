/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 04.03.13
 * Time: 13:55
 */
package ru.game.aurora.player.earth;

import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.world.World;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class EarthState implements Serializable
{
    private static final long serialVersionUID = 1L;

    private List<PrivateMessage> messages = new LinkedList<>();

    private int technologyLevel = 0;

    private EvacuationState evacuationState;

    // quest dialogs that override default earth dialog
    // ordered in a queue, as there may be more than one at a time
    private Queue<Dialog> earthSpecialDialogs = new LinkedList<>();

    public void updateTechnologyLevel(int value) {
        technologyLevel += value;
    }

    public List<PrivateMessage> getMessages() {
        return messages;
    }

    public Queue<Dialog> getEarthSpecialDialogs() {
        return earthSpecialDialogs;
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
