/**
 * Created with IntelliJ IDEA.
 * User: User
 * Date: 09.04.14
 * Time: 13:28
 */

package ru.game.aurora.dialog;

import ru.game.aurora.world.World;

import java.util.Map;

public class SaveFlagsDialogListener implements DialogListener {
    private static final long serialVersionUID = 1L;

    private final Map<String, String> target;

    public SaveFlagsDialogListener(Map<String, String> target) {
        this.target = target;
    }

    @Override
    public void onDialogEnded(World world, Dialog dialog, int returnCode, Map<String, String> flags) {
        target.putAll(flags);
    }
}
