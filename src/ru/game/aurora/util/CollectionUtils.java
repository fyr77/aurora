package ru.game.aurora.util;

import java.util.*;

/**
 * User: jedi-philosopher
 * Date: 11.12.2010
 * Time: 0:51:57
 */
public abstract class CollectionUtils {

    @SafeVarargs
    public static <T> T selectRandomElement(T... elems) {
        return selectRandomElementArray(elems);
    }

    public static <T> T selectRandomElementArray(T[] elems) {
        if (elems.length == 0) {
            return null;
        }
        final int elemIdx = (int) (elems.length * Math.random());
        return elems[elemIdx];
    }

    /**
     * Selects random element from collection
     *
     * @param coll Collection to select element from
     * @param <T>  Element type
     * @return Randomly selected element of a collection or null if it was null or empty
     */
    public static <T> T selectRandomElement(Collection<? extends T> coll) {
        if (coll == null || coll.isEmpty()) {
            return null;
        }
        final int elemIdx = (int) (coll.size() * Math.random());
        int curIdx = 0;
        for (T elem : coll) {
            if (curIdx++ == elemIdx) {
                return elem;
            }
        }
        throw new IllegalStateException("Should never get here");
    }

    public static <T> List<T> selectRandomElementToCollection(Collection<? extends T> coll) {
        if (coll == null || coll.isEmpty()) {
            return Collections.emptyList();
        }
        T elem = selectRandomElement(coll);
        List<T> rz = new LinkedList<T>();
        rz.add(elem);
        return rz;
    }

    public static <T> List<T> selectRandomElementToCollection(T[] array) {
        T t = selectRandomElement(array);
        List<T> rz = new ArrayList<T>();
        if (t == null) {
            return rz;
        }
        rz.add(t);
        return rz;
    }

    /**
     * Returns set of keys, for which map contains specified value
     *
     * @param map Map to process
     * @param val Value, keys for which are searched
     * @param <K> Key type
     * @param <V> Value type
     * @return Collection of keys that have desired value in map
     */
    public static <K, V> Set<K> getMapKeysForValue(Map<K, V> map, V val) {
        if (map == null || val == null) {
            throw new IllegalArgumentException();
        }

        Set<K> result = new HashSet<K>();
        for (Map.Entry<K, V> entry : map.entrySet()) {
            if (entry.getValue().equals(val)) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    public static <E> List<E> toList(E... elems) {
        List<E> rz = new LinkedList<E>();
        Collections.addAll(rz, elems);
        return rz;
    }

    public static <E> boolean contains(E toFind, E... elems) {
        for (E e : elems) {
            if (e != null && e.equals(toFind)) {
                return true;
            }
        }
        return false;
    }

}
