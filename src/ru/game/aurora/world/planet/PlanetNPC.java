package ru.game.aurora.world.planet;

import ru.game.aurora.common.Drawable;
import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.world.BaseGameObject;
import ru.game.aurora.world.ScanGroup;
import ru.game.aurora.world.World;

/**
 * Created with IntelliJ IDEA.
 * User: User
 * Date: 22.05.14
 * Time: 21:14
 */
public class PlanetNPC extends BaseGameObject {
    private static final long serialVersionUID = 1L;

    private Dialog dialog;

    public PlanetNPC(int x, int y, String tileset, int tileX, int tileY) {
        super(x, y, new Drawable(tileset, tileX, tileY, false));
    }

    public PlanetNPC(int x, int y, String image) {
        super(x, y, image);
    }

    public Dialog getDialog() {
        return dialog;
    }

    public void setDialog(Dialog dialog) {
        this.dialog = dialog;
    }

    @Override
    public boolean interact(World world) {
        world.addOverlayWindow(dialog);
        return true;
    }

    @Override
    public boolean canBeInteracted() {
        return true;
    }

    @Override
    public ScanGroup getScanGroup() {
        return ScanGroup.OTHER;
    }
}
