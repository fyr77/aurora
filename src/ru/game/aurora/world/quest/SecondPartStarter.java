package ru.game.aurora.world.quest;

import org.slf4j.LoggerFactory;
import ru.game.aurora.dialog.IntroDialog;
import ru.game.aurora.gui.FadeOutScreenController;
import ru.game.aurora.gui.GUI;
import ru.game.aurora.gui.IntroDialogController;
import ru.game.aurora.world.IStateChangeListener;
import ru.game.aurora.world.World;

/**
 * Created by User on 07.01.2017.
 * This code starts second part of a global plot, after Obliterator visits Solar system
 */
public class SecondPartStarter {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(SecondPartStarter.class);

    private void showIntro(World world) {
        IntroDialog dialog;
        if (world.getGlobalVariables().containsKey("colony_established")) {
            dialog = IntroDialog.load("story/intro_2_with_colony.json");
        } else {
            dialog = IntroDialog.load("story/intro_2_no_colony.json");
        }
        IntroDialogController introDialogController = (IntroDialogController) GUI.getInstance().getNifty().findScreenController(IntroDialogController.class.getName());
        introDialogController.pushDialog(dialog);
        introDialogController.setEndListener(new IStateChangeListener<World>() {

            private static final long serialVersionUID = 1L;

            @Override
            public void stateChanged(World world) {
                movePlayerShipToEarth(world);
            }
        });
        GUI.getInstance().getNifty().gotoScreen("intro_dialog");
    }

    private void movePlayerShipToEarth(World world) {

    }

    private void startQuests(World world) {
        world.addListener(new UnityQuest());
        world.getPlayer().getJournal().addQuestEntries("unity", "start");
        world.getPlayer().getJournal().addQuestEntries("colony_negotiation", "start");
    }

    /**
     * Replaces colony planet with a dialog planet like Earth (which can not be landed)
     */
    private void modifyColony(World world) {

    }

    public void start(final World world) {

        world.getGlobalVariables().put("2nd_part", true);

        logger.info("Starting 2nd story part");

        // fade out screen, show new intro and dialogs after that
        FadeOutScreenController.makeFade(new IStateChangeListener() {
            @Override
            public void stateChanged(Object param) {
                showIntro(world);
                startQuests(world);
                modifyColony(world);
            }
        });
    }
}
