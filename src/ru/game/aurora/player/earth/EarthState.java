/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 04.03.13
 * Time: 13:55
 */
package ru.game.aurora.player.earth;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

public class EarthState implements Serializable
{
    private static final long serialVersionUID = 7511734171340918376L;

    private List<PrivateMessage> messages = new LinkedList<PrivateMessage>();

    public List<PrivateMessage> getMessages() {
        return messages;
    }
}
