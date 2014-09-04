package ru.game.aurora.world;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 17.02.14
 * Time: 17:11
 */
public class Listenable implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<IStateChangeListener<World>> listeners = null;

    public void addListener(IStateChangeListener<World> listener) {
        if (listeners == null) {
            listeners = new ArrayList<>();
        }
        listeners.add(listener);
    }

    public void removeListener(IStateChangeListener<World> listener) {
        if (listeners != null) {
            listeners.remove(listener);
        }
    }

    public void fireEvent(World world) {
        if (listeners == null) {
            return;
        }
        for (IStateChangeListener<World> listener : listeners) {
            listener.stateChanged(world);
        }
    }
}
