package queue;

import java.util.Arrays;
import java.util.Objects;

// Model: a[head]..a[tale]
// Inv: tale - head >= 0 &&
//      forall i=head..tale: a[i] != null
// Let: immutable(l, r): forall i=l..r: a'[i] = a[i]
// Let: immutable: immutable(head, tale) &&
//      head' = head && tale' = tale
// Let: n = tale - head + 1;
public class ArrayQueueModule {
    private static int size = 0;
    private static int head = 0;
    private static Object[] elements = new Object[5];

    // Pre: element != null
    // Post: head' = head && tale' = tale + 1 &&
    //      a'[tale'] = element &&
    //      immutable(head, tale)
    public static void enqueue(Object element) {
        Objects.requireNonNull(element);
        elements[(head + size) % elements.length] = element;
        size ++;
        ensureCapacity(size);
    }
    // Pre: true
    // Post: immutable
    private static void ensureCapacity(int capacity) {
        if (capacity >= elements.length) {
            elements = Arrays.copyOf(elements, 2 * capacity);
            for (int i = 0; i < head; i++) {
                elements[i + capacity] = elements[i];
            }
        }
    }

    // Pre: n > 0
    // Post: R = a[head] && immutable
    public static Object element() {
        assert size > 0;

        return elements[head];
    }

    // Pre: n > 0
    // Post: head' = head + 1 && tale' = tale &&
    //      R = a[head] &&
    //      immutable(head', tale)
    public static Object dequeue() {
        assert size > 0;
        Object result = elements[head];
        head = (head + 1) % elements.length;
        size --;
        return result;
    }

    // Pre: true
    // Post: R = n && immutable
    public static int size() {
        return size;
    }

    // Pre: true
    // Post: R = (n = 0) && immutable
    public static boolean isEmpty() {
        return size == 0;
    }

    // Pre: true
    // Post: n = 0
    public static void clear() {
        elements = new Object[5];
        head = 0;
        size = 0;
    }

    // Pre: true
    // Post: immutable && R = "[{a[head].toString()}, {a[head + 1].toString()}, ..., {a[tale].toString()}]"
    public static String toStr() {
        StringBuilder result = new StringBuilder("[");
        if (size > 0) {
            for (int i = head; i < head + size - 1; i ++) {
                result.append(elements[i % elements.length].toString());
                result.append(", ");
            }
            result.append(elements[(head + size - 1) % elements.length]);
        }
        result.append(']');
        return result.toString();
    }
}
