/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 07.02.13
 * Time: 16:56
 */
package ru.game.aurora.application;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.game.aurora.util.GsonTransient;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

/**
 * Class that loads all entities stored in json format in given order
 */
public class JsonConfigManager<T extends JsonConfigManager.EntityWithId> {

    private static final Logger logger = LoggerFactory.getLogger(JsonConfigManager.class);

    public interface EntityWithId {
        String getId();
        String getCustomClass();
    }

    private final Class<T> entityClass;

    // we will ignore all fields marked with GsonTransient annotation
    private final Gson gson = new GsonBuilder().setExclusionStrategies(new ExclusionStrategy() {
        @Override
        public boolean shouldSkipField(FieldAttributes f) {
            return f.getAnnotation(GsonTransient.class) != null;
        }

        @Override
        public boolean shouldSkipClass(Class<?> clazz) {
            return false;
        }
    }).create();

    private final Map<String, T> entities = new HashMap<>();

    public JsonConfigManager(Class<T> eClass, String... resourceFolders) {
        this.entityClass = eClass;
        for (String resourceFolder : resourceFolders) {
            File dir = new File(resourceFolder);
            if (!dir.exists() || !dir.isDirectory()) {
                throw new IllegalArgumentException("Directory " + dir.getAbsolutePath() + " does not exist");
            }

            processDir(dir);
        }
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
            Reader reader = new BufferedReader(new FileReader(f));
            reader.mark(4096);
            String json = IOUtils.toString(reader);

            T entity = gson.fromJson(json, entityClass);
            String customClassName = entity.getCustomClass();
            if (customClassName != null) {
                Class customClass = Class.forName(customClassName);
                if (!EntityWithId.class.isAssignableFrom(customClass)) {
                    logger.error("Custom class for entity in file {} does not implement the EntityWithId interface", f.getName());
                    return;
                }
                reader.reset();
                entity = (T) gson.fromJson(json, (Class<? extends EntityWithId>) customClass);
            }
            reader.close();

            if (entities.containsKey(entity.getId())) {
                logger.warn("Duplicated entry with id " + entity.getId());
            }
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
