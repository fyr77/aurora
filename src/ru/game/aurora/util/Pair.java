/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 13.06.13
 * Time: 18:08
 */
package ru.game.aurora.util;

import java.io.Serializable;

public class Pair<K extends Serializable, V extends Serializable> implements Serializable
{
    private static final long serialVersionUID = 8061153878845074111L;

    private final K key;

    private final V val;

    public Pair(K key, V val) {
        this.key = key;
        this.val = val;
    }

    public K getKey() {
        return key;
    }

    public V getVal() {
        return val;
    }

    @Override
    public String toString() {
        return "[" + key + ", " + val + "]";
    }
}
