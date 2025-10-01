package info.kgeorgiy.ja.vysotin.arrayset;

import java.util.*;

@SuppressWarnings("unused")
public class ArraySet<E> extends AbstractSet<E> implements SortedSet<E> {
    private final Comparator<? super E> comparator;
    private final List<E> set;
    public ArraySet() {
        comparator = null;
        set = new ArrayList<>();
    }
    public ArraySet(Collection<? extends E> collection) {
        comparator = null;
        set = new ArrayList<>(new TreeSet<E>(collection));
    }

    public ArraySet(Collection<? extends E> collection, Comparator<? super E> comp) {
        comparator = comp;
        SortedSet<E> newSet = new TreeSet<>(comparator);
        newSet.addAll(collection);
        set = new ArrayList<>(newSet);
    }

    public ArraySet(Comparator<? super E> comp) {
        comparator = comp;
        set = new ArrayList<>();
    }

    @Override
    public Comparator<? super E> comparator() {
        return comparator;
    }

    @Override
    public SortedSet<E> subSet(E fromElement, E toElement) {
        if (compare(fromElement, toElement) == 1) {
            throw new IllegalArgumentException("fromKey > toKey");
        }
        int fromIndex = Collections.binarySearch(set, fromElement, comparator);
        int toIndex = Collections.binarySearch(set, toElement, comparator);
        if (fromIndex <= -1) {
            fromIndex = -(fromIndex + 1);
        }
        if (toIndex <= -1) {
            toIndex = -(toIndex + 1);
        }
        return new ArraySet<>(set.subList(fromIndex, toIndex), comparator);
    }

    @Override
    public SortedSet<E> headSet(E toElement) {
        if (set.isEmpty()) {
            return new ArraySet<>(comparator);
        }
        int toIndex = Collections.binarySearch(set, toElement, comparator);
        if (toIndex <= -1) {
            toIndex = -(toIndex + 1);
        }
        return new ArraySet<>(set.subList(0, toIndex), comparator);
    }

    @Override
    public SortedSet<E> tailSet(E fromElement) {
        if (set.isEmpty()) {
            return new ArraySet<>(comparator);
        }
        int fromIndex = Collections.binarySearch(set, fromElement, comparator);
        if (fromIndex <= -1) {
            fromIndex = -(fromIndex + 1);
        }
        return new ArraySet<>(set.subList(fromIndex, set.size()), comparator);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean contains(Object o) {
        return Collections.binarySearch(set, Objects.requireNonNull((E) o), comparator) >= 0;
    }

    @Override
    public E first() {
        if (set.isEmpty())
            throw new NoSuchElementException("ArraySet is empty");
        return set.getFirst();
    }

    @Override
    public E last() {
        if (set.isEmpty())
            throw new NoSuchElementException("ArraySet is empty");
        return set.getLast();
    }

    @Override
    public int size() {
        return set.size();
    }

    @Override
    public Iterator<E> iterator() {
        return Collections.unmodifiableList(set).iterator();
    }

    // Function for comparing
    @SuppressWarnings("unchecked")
    private int compare(E a, E b) {
        if (comparator == null) {
            return ((Comparable<E>) a).compareTo(b);
        } else {
            return Objects.compare(a, b, comparator);
        }
    }
}
