package ru.game.aurora.modding;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;

/**
 * This class extends a standard object input stream but has a custom logic of class resolving.
 * Tries to resolve classes using mod classloaders
 */
public class SaveGameObjectInputStream extends ObjectInputStream {
    public SaveGameObjectInputStream(InputStream in) throws IOException {
        super(in);
    }

    @Override
    protected Class<?> resolveClass(ObjectStreamClass objectStreamClass) throws IOException, ClassNotFoundException {
        try {
            return Class.forName(objectStreamClass.getName());
        } catch (ClassNotFoundException ignore) {
            Class rz = ModManager.getInstance().resolveClass(objectStreamClass.getName());
            if (rz != null) {
                return rz;
            }
            throw new ClassNotFoundException(objectStreamClass.getName());
        }

    }
}
