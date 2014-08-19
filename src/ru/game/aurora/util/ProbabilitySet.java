package ru.game.aurora.util;

import java.io.Serializable;
import java.util.*;

/**
 * User: e_smirnov
 * Date: 19.04.2011
 * Time: 16:15:51
 * <p/>
 * Contains mapping from element into its weight
 * Contains method to get random element, each element is selected with probability of its weight / total weight
 */
public class ProbabilitySet<T> extends AbstractMap<T, Double> implements Serializable {

    @SuppressWarnings({"NonSerializableFieldInSerializableClass"})
    private final Map<T, Double> entries = new HashMap<>();

    private final Random random;

    private static final long serialVersionUID = -2548275137421202393L;

    public ProbabilitySet() {
        this(new Random(System.currentTimeMillis()));
    }

    public ProbabilitySet(Random random) {
        this.random = random;
    }

    public ProbabilitySet(Collection<? extends T> coll) {
        this(coll, new Random(System.currentTimeMillis()));
    }

    public ProbabilitySet(ProbabilitySet<? extends T> proto) {
        this(proto, new Random(System.currentTimeMillis()));
    }

    public ProbabilitySet(ProbabilitySet<? extends T> proto, Random random) {
        this.entries.putAll(proto);
        this.random = random;
    }

    /**
     * Creates set from given collection of values, all values are assigned equal weight
     *
     * @param coll   Items
     * @param random Random
     */
    public ProbabilitySet(Collection<? extends T> coll, Random random) {
        this(random);
        putAll(coll);
    }

    @Override
    public Set<Entry<T, Double>> entrySet() {
        return entries.entrySet();
    }

    @Override
    public Double put(T val, Double weight) {
        if (weight < 0.0) {
            throw new IllegalArgumentException("Weight should not be negative");
        }
        return entries.put(val, weight);
    }

    public void putAll(Collection<? extends T> coll) {
        for (T item : coll) {
            put(item, 1.0);
        }
    }

    public T getRandom() {
        if (entries.isEmpty()) {
            return null;
        }

        double totalWeight = 0.0;
        for (Map.Entry<T, Double> entry : entries.entrySet()) {
            totalWeight += entry.getValue();
        }

        double randomVal = random.nextDouble() * totalWeight;
        double currentVal = 0.0;

        for (Map.Entry<T, Double> entry : entries.entrySet()) {
            if (currentVal <= randomVal && currentVal + entry.getValue() > randomVal) {
                return entry.getKey();
            }
            currentVal += entry.getValue();
        }

        throw new IllegalStateException("Failed to select random element");
    }

    public T removeRandom() {
        T rz = getRandom();
        remove(rz);
        return rz;
    }
}
