package ru.game.aurora.dialog;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * Non-interactive dialog shown on intro
 */
public class IntroDialog {
    public static class Statement {
        public final String mainImageId;

        public final String iconName;

        public final String captionId;

        public final String textId;

        public Statement(String mainImageId, String captionId, String iconName, String textId) {
            this.mainImageId = mainImageId;
            this.captionId = captionId;
            this.iconName = iconName;
            this.textId = textId;
        }
    }

    public final String id;

    public final String mainImageId;

    public final Statement[] statements;

    public IntroDialog(String id, String imageId, Statement... statements) {
        this.id = id;
        this.mainImageId = imageId;
        this.statements = statements;
    }

    public static IntroDialog load(String path) {
        InputStream is = IntroDialog.class.getClassLoader().getResourceAsStream(path);
        if (is == null) {
            throw new IllegalArgumentException("Can not load intro from null stream");
        }
        Gson gson = new Gson();
        Reader reader = new InputStreamReader(is);
        IntroDialog d = gson.fromJson(reader, IntroDialog.class);
        try {
            reader.close();
        } catch (IOException e) {
            throw new RuntimeException("Failed to read intro", e);
        }
        return d;
    }
}
