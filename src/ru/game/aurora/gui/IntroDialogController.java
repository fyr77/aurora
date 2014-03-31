package ru.game.aurora.gui;

import de.lessvoid.nifty.EndNotify;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.effects.EffectEventId;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.elements.render.TextRenderer;
import de.lessvoid.nifty.layout.align.HorizontalAlign;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Input;
import ru.game.aurora.application.AuroraGame;
import ru.game.aurora.application.Localization;
import ru.game.aurora.dialog.IntroDialog;
import ru.game.aurora.util.EngineUtils;
import ru.game.aurora.world.IStateChangeListener;
import ru.game.aurora.world.Updatable;
import ru.game.aurora.world.World;

public class IntroDialogController implements ScreenController, Updatable {

    private World world;

    private long lastLetterTime;

    private Element leftPortrait;

    private Element rightPortrait;

    private Element imagePanel;

    private Element captionText;

    private Element mainText;

    private Element mainPanel;

    private IntroDialog introDialog;

    private int statement;

    private boolean isLeft = true;

    private IStateChangeListener endListener;

    private String desiredString;

    private StringBuilder actualStringBuilder = new StringBuilder();

    private boolean isTyping = true;

    public IntroDialogController(World world) {
        this.world = world;
    }

    public void setIntroDialog(IntroDialog introDialog) {
        this.introDialog = introDialog;
        this.statement = 0;
        this.isLeft = true;
        this.isTyping = true;
        this.actualStringBuilder = new StringBuilder();
    }

    public void setEndListener(IStateChangeListener endListener) {
        this.endListener = endListener;
    }

    @Override
    public void bind(Nifty nifty, Screen screen) {
        mainPanel = screen.findElementByName("mainPanel");
        leftPortrait = mainPanel.findElementByName("leftPortrait");
        rightPortrait = mainPanel.findElementByName("rightPortrait");
        Element textPanel = mainPanel.findElementByName("textPanel");
        imagePanel = screen.findElementByName("dialogImage");
        captionText = textPanel.findElementByName("caption");
        mainText = textPanel.findElementByName("npcText");
    }

    private void update() {
        final String bundleId = "intro/" + introDialog.id;
        IntroDialog.Statement currentStatement = introDialog.statements[statement];
        EngineUtils.setTextForGUIElement(captionText, Localization.getText(bundleId, currentStatement.captionId));
        EngineUtils.setTextForGUIElement(mainText, "");

        desiredString = Localization.getText(bundleId, currentStatement.textId);
        if (isLeft) {
            captionText.getRenderer(TextRenderer.class).setTextHAlign(HorizontalAlign.left);
            mainText.getRenderer(TextRenderer.class).setTextHAlign(HorizontalAlign.left);
            EngineUtils.setImageForGUIElement(leftPortrait, currentStatement.iconName);
            leftPortrait.setVisible(true);
            rightPortrait.setVisible(false);
        } else {
            captionText.getRenderer(TextRenderer.class).setTextHAlign(HorizontalAlign.right);
            mainText.getRenderer(TextRenderer.class).setTextHAlign(HorizontalAlign.right);
            EngineUtils.setImageForGUIElement(rightPortrait, currentStatement.iconName);
            leftPortrait.setVisible(false);
            rightPortrait.setVisible(true);
        }
        GUI.getInstance().getNifty().getCurrentScreen().layoutLayers();
    }

    private class FadeOutEndListener implements EndNotify {
        @Override
        public void perform() {
            actualStringBuilder = new StringBuilder();
            statement++;
            isLeft = !isLeft;
            if (statement >= introDialog.statements.length) {
                GUI.getInstance().popAndSetScreen();
                if (endListener != null) {
                    endListener.stateChanged(world);
                }
            } else {
                update();
                mainPanel.startEffect(EffectEventId.onCustom, new FadeInEndListener(), "fadeIn");
            }
        }
    }

    private class FadeInEndListener implements EndNotify {

        @Override
        public void perform() {
            isTyping = true;

        }
    }

    public void advance() {
        if (isTyping) {
            EngineUtils.setTextForGUIElement(mainText, desiredString);
            isTyping = false;
        } else {
            mainPanel.startEffect(EffectEventId.onCustom, new FadeOutEndListener(), "fadeOut");
        }
    }

    @Override
    public void onStartScreen() {
        update();
        EngineUtils.setImageForGUIElement(imagePanel, introDialog.mainImageId);
        AuroraGame.getUpdatables().add(this);
    }

    @Override
    public void onEndScreen() {
        mainPanel.stopEffect(EffectEventId.onCustom);
        AuroraGame.getUpdatables().remove(this);
    }

    @Override
    public void update(GameContainer container, World world) {
        if (container.getInput().isKeyPressed(Input.KEY_ESCAPE)) {
            GUI.getInstance().popAndSetScreen();
            if (endListener != null) {
                endListener.stateChanged(world);
            }
            return;
        }
        if (isTyping) {
            if (actualStringBuilder.length() == desiredString.length()) {
                isTyping = false;
                return;
            }
            if (container.getTime() - lastLetterTime > 30) {
                actualStringBuilder.append(desiredString.charAt(actualStringBuilder.length()));
                EngineUtils.setTextForGUIElement(mainText, actualStringBuilder.toString());
                lastLetterTime = container.getTime();
            }
        }
    }
}
