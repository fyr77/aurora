/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 07.02.13
 * Time: 15:16
 */

package ru.game.aurora.world.space;


import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import ru.game.aurora.application.Camera;
import ru.game.aurora.application.ResourceManager;
import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.player.research.ResearchProjectDesc;
import ru.game.aurora.world.BasePositionable;
import ru.game.aurora.world.World;

/**
 * A space hulk drifting in space.
 * Does not move or react on player. Can not be shot at and destroyed.
 * Can contain something valuable, like resources, materials for research etc.
 * In future versions will contain enemies and location similar to planet
 */
public class SpaceHulk extends BasePositionable implements SpaceObject {
    private static final long serialVersionUID = 1122189215297259875L;

    private String name;

    private String image;

    // dialog that is shown to player when interacting with hulk
    private Dialog onInteractDialog;

    private boolean explored = false;

    // research that becomes available after interaction
    private ResearchProjectDesc[] researchProjectDescs;

    public SpaceHulk(int x, int y, String name, String image) {
        super(x, y);
        this.name = name;
        this.image = image;
    }

    @Override
    public void onContact(World world) {
        if (explored) {
            return;
        }

        if (onInteractDialog != null) {
            world.addOverlayWindow(onInteractDialog);
        }

        if (researchProjectDescs != null) {
            for (ResearchProjectDesc researchProjectDesc : researchProjectDescs) {
                world.getPlayer().getResearchState().addNewAvailableProject(researchProjectDesc);
            }
        }

        explored = true;
    }

    @Override
    public void onAttack(World world, SpaceObject attacker, int dmg) {
        // nothing
    }

    @Override
    public boolean isAlive() {
        return true;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void update(GameContainer container, World world) {
        // nothing
    }

    @Override
    public void draw(GameContainer container, Graphics graphics, Camera camera) {
        graphics.drawImage(ResourceManager.getInstance().getImage(image), camera.getXCoord(x), camera.getYCoord(y));
    }

    public void setOnInteractDialog(Dialog onInteractDialog) {
        this.onInteractDialog = onInteractDialog;
    }

    public void setResearchProjectDescs(ResearchProjectDesc... descs) {
        this.researchProjectDescs = descs;
    }
}
