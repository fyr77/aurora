package ru.game.aurora.dialog;

import ru.game.aurora.world.World;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 12.03.13
 * Time: 15:32
 */
public class Statement implements Serializable {

    private static final long serialVersionUID = 1L;

    public final int id;

    public final String npcText;

    public final Reply[] replies;

    public final String customIcon;

    public Statement(int id, String npcText, Reply... replies) {
        this.id = id;
        this.npcText = npcText;
        this.replies = replies;
        this.customIcon = null;
    }

    public Statement(int id, String customIcon, String npcText, Reply... replies) {
        this.id = id;
        this.npcText = npcText;
        this.replies = replies;
        this.customIcon = customIcon;
    }

    public List<Reply> getAvailableReplies(World world, Map<String, String> flags) {
        List<Reply> rz = new ArrayList<>(replies.length);
        for (Reply r : replies) {
            if (r.isVisible(world, flags)) {
                rz.add(r);
            }
        }

        return rz;
    }
}
