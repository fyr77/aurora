/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 07.02.13
 * Time: 15:16
 */

package ru.game.aurora.world.space;


import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.player.research.ResearchProjectDesc;
import ru.game.aurora.world.BaseGameObject;
import ru.game.aurora.world.Dungeon;
import ru.game.aurora.world.IStateChangeListener;
import ru.game.aurora.world.World;

/**
 * A space hulk drifting in space.
 * Does not move or react on player. Can not be shot at and destroyed.
 * Can contain something valuable, like resources, materials for research etc.
 * In future versions will contain enemies and location similar to planet
 */
public class SpaceHulk extends BaseGameObject {
    private static final long serialVersionUID = 2L;

    private final String name;

    // dialog that is shown to player when interacting with hulk
    private Dialog onInteractDialog;

    private boolean explored = false;

    // research that becomes available after interaction
    private ResearchProjectDesc[] researchProjectDescs;

    // space hulk can contain explorable location
    private Dungeon dungeon;

    public SpaceHulk(int x, int y, String name, String image) {
        super(x, y, image);
        this.name = name;
    }

    public SpaceHulk(int x, int y, String name, String image, Dungeon dungeon) {
        super(x, y, image);
        this.name = name;
        this.dungeon = dungeon;
        this.dungeon.getController().addListener(new IStateChangeListener<World>() {

            private static final long serialVersionUID = 4039714175367179410L;

            @Override
            public void stateChanged(World world) {
                if (researchProjectDescs != null) {
                    for (ResearchProjectDesc researchProjectDesc : researchProjectDescs) {
                        world.getPlayer().getResearchState().addNewAvailableProject(researchProjectDesc);
                    }
                }

                explored = true;
            }
        });
    }

    @Override
    public void interact(World world) {
        if (explored) {
            return;
        }

        if (onInteractDialog != null) {
            world.addOverlayWindow(onInteractDialog);
        }

        if (dungeon != null) {
            dungeon.enter(world);
        }

    }

    @Override
    public String getName() {
        return name;
    }

    public void setOnInteractDialog(Dialog onInteractDialog) {
        this.onInteractDialog = onInteractDialog;
    }

    public void setResearchProjectDescs(ResearchProjectDesc... descs) {
        this.researchProjectDescs = descs;
    }
}
