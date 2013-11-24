package ru.game.aurora.dialog;

/**
 * Non-interactive dialog shown on intro
 */
public class IntroDialog {
    public static class Statement {
        public final String iconName;

        public final String captionId;

        public final String textId;

        public Statement(String captionId, String iconName, String textId) {
            this.captionId = captionId;
            this.iconName = iconName;
            this.textId = textId;
        }
    }

    public final String mainImageId;

    public final Statement[] statements;

    public IntroDialog(String imageId, Statement... statements) {
        this.mainImageId = imageId;
        this.statements = statements;
    }
}
