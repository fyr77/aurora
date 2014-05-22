package ru.game.aurora.world.planet;

import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.world.World;

/**
 * Created with IntelliJ IDEA.
 * User: User
 * Date: 22.05.14
 * Time: 21:14
 */
public class PlanetNPC extends BasePlanetObject {
    private static final long serialVersionUID = -6116517352597757135L;

    private Dialog dialog;

    public PlanetNPC(int x, int y, String tileset, int tileX, int tileY, Planet myPlanet) {
        super(x, y, tileset, tileX, tileY, myPlanet);
    }

    public PlanetNPC(int x, int y, String image, Planet myPlanet) {
        super(x, y, image, myPlanet);
    }

    public Dialog getDialog() {
        return dialog;
    }

    public void setDialog(Dialog dialog) {
        this.dialog = dialog;
    }

    @Override
    public void onPickedUp(World world) {
        world.addOverlayWindow(dialog);
    }

    @Override
    public boolean canBePickedUp() {
        return true;
    }
}
