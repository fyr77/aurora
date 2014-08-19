/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 24.04.14
 * Time: 18:52
 */
package ru.game.aurora.music;

import ru.game.aurora.application.ResourceManager;
import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.dialog.DialogListener;
import ru.game.aurora.world.World;

import java.util.Map;


// switches music at the end of a dialog
public class MusicDialogListener implements DialogListener
{
    private static final long serialVersionUID = 2456396838423391188L;

    private final String id;

    public MusicDialogListener(String id) {
        this.id = id;
    }

    @Override
    public void onDialogEnded(World world, Dialog dialog, int returnCode, Map<String, String> flags) {
        ResourceManager.getInstance().getPlaylist(id).play();
        dialog.removeListener(this);
    }
}
