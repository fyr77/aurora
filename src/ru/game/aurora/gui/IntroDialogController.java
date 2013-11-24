package ru.game.aurora.gui;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.layout.align.HorizontalAlign;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import ru.game.aurora.application.Localization;
import ru.game.aurora.dialog.IntroDialog;
import ru.game.aurora.util.EngineUtils;
import ru.game.aurora.world.IStateChangeListener;
import ru.game.aurora.world.World;

public class IntroDialogController implements ScreenController {

    private World world;

    private Element leftPortrait;

    private Element rightPortrait;

    private Element imagePanel;

    private Element captionText;

    private Element mainText;

    private IntroDialog introDialog;

    private int statement;

    private boolean isLeft = true;

    private IStateChangeListener endListener;

    public IntroDialogController(World world) {
        this.world = world;
    }

    public void setIntroDialog(IntroDialog introDialog) {
        this.introDialog = introDialog;
        this.statement = 0;
    }

    public void setEndListener(IStateChangeListener endListener) {
        this.endListener = endListener;
    }

    @Override
    public void bind(Nifty nifty, Screen screen) {
        leftPortrait = screen.findElementByName("leftPortrait");
        rightPortrait = screen.findElementByName("rightPortrait");
        Element textPanel = screen.findElementByName("textPanel");
        imagePanel = screen.findElementByName("dialogImage");
        captionText = textPanel.findElementByName("caption");
        mainText = textPanel.findElementByName("npcText");
    }

    private void update() {
        IntroDialog.Statement currentStatement = introDialog.statements[statement];
        EngineUtils.setTextForGUIElement(captionText, Localization.getText("dialogs", currentStatement.captionId));
        EngineUtils.setTextForGUIElement(mainText, Localization.getText("dialogs", currentStatement.textId));
        if (isLeft) {
            captionText.setConstraintHorizontalAlign(HorizontalAlign.left);
            mainText.setConstraintHorizontalAlign(HorizontalAlign.left);
            EngineUtils.setImageForGUIElement(leftPortrait, currentStatement.iconName);
            leftPortrait.setVisible(true);
            rightPortrait.setVisible(false);
        } else {
            mainText.setConstraintHorizontalAlign(HorizontalAlign.right);
            captionText.setConstraintHorizontalAlign(HorizontalAlign.right);
            EngineUtils.setImageForGUIElement(rightPortrait, currentStatement.iconName);
            leftPortrait.setVisible(false);
            rightPortrait.setVisible(true);
        }
        GUI.getInstance().getNifty().getCurrentScreen().layoutLayers();
    }

    public void advance() {
        statement++;
        isLeft = !isLeft;
        if (statement >= introDialog.statements.length) {
            if (endListener != null) {
                endListener.stateChanged(world);
            }
            GUI.getInstance().popAndSetScreen();
        } else {
            update();
        }
    }

    @Override
    public void onStartScreen() {
        update();
        EngineUtils.setImageForGUIElement(imagePanel, introDialog.mainImageId);
    }

    @Override
    public void onEndScreen() {

    }
}
