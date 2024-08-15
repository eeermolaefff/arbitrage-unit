package com.stambul.library.tools;

import java.util.*;

public class IterableTools {

    /**
     * Returns the length of iterable object
     * @param iterable an iterable object the length of which you want to know
     * @return a length of iterable object
     */
    public static int size(Iterable<?> iterable) {
        if (iterable == null)
            return 0;

        if (iterable instanceof Collection<?>)
            return ((Collection<?>) iterable).size();

        int counter = 0;
        for (Object obj : iterable)
            counter++;
        return counter;
    }

    /**
     * Returns the difference between two sets of incomparable objects
     * @param minuend a set of objects from which elements of subtrahend will be subtracted
     * @param subtrahend a set of objects that will be subtracted from the set of minuend
     * @param comparator comparator to compare elements of type &lt;T&gt;
     * @return a set of objects represents the difference between minuend and subtrahend sets
     */
    public static <T> Set<T> minus(Iterable<T> minuend, Iterable<T> subtrahend, Comparator<T> comparator) {
        Set<T> minuendSet = new TreeSet<>(comparator);
        for (T obj : minuend)
            minuendSet.add(obj);
        subtrahend.forEach(minuendSet::remove);
        return minuendSet;
    }

    /**
     * Returns the difference between two sets of comparable objects
     * @param minuend a set of objects from which elements of subtrahend will be subtracted
     * @param subtrahend a set of objects that will be subtracted from the set of minuend
     * @return a set of objects represents the difference between minuend and subtrahend sets
     */
    public static <T extends Comparable<T>> Set<T> minus(Iterable<T> minuend, Iterable<T> subtrahend) {
        Set<T> minuendSet = new TreeSet<>();
        minuend.forEach(minuendSet::add);
        subtrahend.forEach(minuendSet::remove);
        return minuendSet;
    }

    /**
     * Splits the iterable object into batches of fixed size
     * @param iterable the object you want to split into batches
     * @param batchSize the length of each batch in the resulting split
     * @return a list of batches
     */
    public static <T> List<List<T>> partition(Iterable<T> iterable, int batchSize) {
        List<List<T>> batches = new LinkedList<>();

        int currentSize = 0;
        List<T> currentBatch = new LinkedList<>();
        for (T obj : iterable) {
            if (currentSize >= batchSize) {
                batches.add(new LinkedList<>(currentBatch));
                currentSize = 0;
                currentBatch.clear();
            }
            currentBatch.add(obj);
            currentSize += 1;
        }

        if (!currentBatch.isEmpty())
            batches.add(new LinkedList<>(currentBatch));

        return batches;
    }

    /**
     * Splits the iterable object into specified amount of batches
     * @param iterable the object you want to split into batches
     * @param numberOfBatches the number of batches you want to get
     * @return a list of batches
     */
    public static <T> List<List<T>> split(Iterable<T> iterable, int numberOfBatches) {
        int collectionSize = size(iterable);
        int batchSize = collectionSize / numberOfBatches;
        if (collectionSize % numberOfBatches != 0)
            batchSize += 1;

        return IterableTools.partition(iterable, batchSize);
    }
}
