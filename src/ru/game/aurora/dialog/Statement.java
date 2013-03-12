package ru.game.aurora.dialog;

import ru.game.aurora.world.World;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
* Created with IntelliJ IDEA.
* User: Egor.Smirnov
* Date: 12.03.13
* Time: 15:32
* To change this template use File | Settings | File Templates.
*/
public class Statement implements Serializable {

    private static final long serialVersionUID = -9058694068037621906L;

    public final int id;

    public final String npcText;

    public final Reply[] replies;

    public Statement(int id, String npcText, Reply... replies) {
        this.id = id;
        this.npcText = npcText;
        this.replies = replies;
    }

    public List<Reply> getAvailableReplies(World world)
    {
        List<Reply> rz = new ArrayList<Reply>(replies.length);
        for (Reply r : replies) {
            if (r.isVisible(world)) {
                rz.add(r);
            }
        }

        return rz;
    }
}
