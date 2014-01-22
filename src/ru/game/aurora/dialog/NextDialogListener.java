package ru.game.aurora.dialog;

import ru.game.aurora.world.World;

import java.util.Map;

/**
 * Simple listener that just shows next dialog when this one is over
 */
public class NextDialogListener implements DialogListener
{
    private static final long serialVersionUID = 6141684215868408536L;

    private Dialog nextDialog;

    public NextDialogListener(Dialog nextDialog) {
        this.nextDialog = nextDialog;
    }

    public NextDialogListener(String nextDialog) {
        this.nextDialog = Dialog.loadFromFile(nextDialog);
    }

    @Override
    public void onDialogEnded(World world, Dialog dialog, int returnCode, Map<String, String> flags) {
        world.addOverlayWindow(nextDialog);
    }
}
