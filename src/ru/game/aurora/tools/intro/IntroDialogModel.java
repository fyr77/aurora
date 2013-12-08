package ru.game.aurora.tools.intro;

import com.google.gson.Gson;
import ru.game.aurora.dialog.IntroDialog;

import javax.swing.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: User
 * Date: 07.12.13
 * Time: 16:18
 */
@SuppressWarnings("NonSerializableFieldInSerializableClass")
public class IntroDialogModel extends AbstractListModel<IntroDialog.Statement> {
    private String dialogId = "alala";

    private String imageId = "";

    /**
     * mapping (locale name) -> ((text id) -> (text))
     */
    private Map<String, Map<String, String>> localizedTexts = new HashMap<>();

    private List<IntroDialog.Statement> statements = new ArrayList<>();

    private static final Gson gson = new Gson();

    public IntroDialogModel() {
        localizedTexts.put("ru", new HashMap<String, String>());
        localizedTexts.put("en", new HashMap<String, String>());
    }

    public static IntroDialogModel load(File file) throws FileNotFoundException {
        return gson.fromJson(new FileReader(file), IntroDialogModel.class);
    }

    public void save(File file) throws IOException {
        FileWriter writer = new FileWriter(file);
        gson.toJson(this, writer);
        writer.close();
    }

    public String getDialogId() {
        return dialogId;
    }

    public String getImageId() {
        return imageId;
    }

    public void addStatement() {
        statements.add(new IntroDialog.Statement("", "", ""));
        fireIntervalAdded(this, statements.size() - 1, statements.size() - 1);
    }

    public void deleteStatement(int idx) {
        statements.remove(idx);
        fireIntervalRemoved(this, idx, idx);
    }

    public String getText(String locale, String id) {
        return localizedTexts.get(locale).get(id);
    }

    public void setText(String locale, String id, String value) {
        localizedTexts.get(locale).put(id, value);
    }

    @Override
    public int getSize() {
        return statements.size();
    }

    @Override
    public IntroDialog.Statement getElementAt(int index) {
        return statements.get(index);
    }
}
