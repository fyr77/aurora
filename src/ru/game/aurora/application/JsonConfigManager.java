/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 07.02.13
 * Time: 16:56
 */
package ru.game.aurora.application;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Class that loads all entities stored in json format in given order
 */
public class JsonConfigManager<T extends JsonConfigManager.EntityWithId> {

    private static final Logger logger = LoggerFactory.getLogger(JsonConfigManager.class);

    public static interface EntityWithId {
        public String getId();
    }

    private Class<T> entityClass;

    private Gson gson = new Gson();

    private Map<String, T> entities = new HashMap<>();

    public JsonConfigManager(Class<T> eClass, String resourceFolder) {
        this.entityClass = eClass;
        File dir = new File(resourceFolder);
        if (!dir.exists() || !dir.isDirectory()) {
            throw new IllegalArgumentException("Directory " + dir.getAbsolutePath() + " does not exist");
        }

        processDir(dir);
    }

    private void processDir(File dir) {
        for (File f : dir.listFiles()) {
            if (f.isDirectory()) {
                processDir(f);
            } else if (f.isFile() && f.getName().endsWith(".json")) {
                loadEntity(f);
            }
        }
    }

    private void loadEntity(File f) {
        try {
            FileReader reader = new FileReader(f);
            T entity = gson.fromJson(reader, entityClass);
            entities.put(entity.getId(), entity);
        } catch (Exception e) {
            logger.error("Failed to read entity from " + f.getPath(), e);
        }
    }

    public T getEntity(String key) {
        if (!entities.containsKey(key)) {
            logger.warn("Entity '{}' not found", key);
        }
        return entities.get(key);
    }

    public Map<String, T> getEntities() {
        return entities;
    }
}
