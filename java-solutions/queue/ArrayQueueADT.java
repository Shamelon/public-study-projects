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
public class ArrayQueueADT {
    private int size = 0;
    private int head = 1;
    private Object[] elements = new Object[5];

    // Pre: true
    // Post: R.n = 0
    public static ArrayQueueADT create() {
        ArrayQueueADT queue = new ArrayQueueADT();
        queue.elements = new Object[10];
        return queue;
    }
    // Pre: element != null
    // Post: head' = head && tale' = tale + 1 &&
    //      a'[tale'] = element &&
    //      immutable(head, tale)
    public static void enqueue(ArrayQueueADT queue, Object element) {
        Objects.requireNonNull(element);
        queue.elements[(queue.head + queue.size) % queue.elements.length] = element;
        queue.size ++;
        ensureCapacity(queue, queue.size);
    }

    // Pre: true
    // Post: immutable
    private static void ensureCapacity(ArrayQueueADT queue, int capacity) {
        if (capacity >= queue.elements.length) {
            queue.elements = Arrays.copyOf(queue.elements, 2 * capacity);
            for (int i = 0; i < queue.head; i++) {
                queue.elements[i + capacity] = queue.elements[i];
            }
        }
    }

    // Pre: n > 0
    // Post: R = a[head] && immutable
    public static Object element(ArrayQueueADT queue) {
        assert queue.size > 0;

        return queue.elements[queue.head];
    }

    // Pre: n > 0
    // Post: head' = head + 1 && tale' = tale &&
    //      R = a[head] &&
    //      immutable(head', tale)
    public static Object dequeue(ArrayQueueADT queue) {
        assert queue.size > 0;
        Object result = queue.elements[queue.head];
        queue.head = (queue.head + 1) % queue.elements.length;
        queue.size --;
        return result;
    }

    // Pre: true
    // Post: R = n && immutable
    public static int size(ArrayQueueADT queue) {
        return queue.size;
    }

    // Pre: true
    // Post: R = (n = 0) && immutable
    public static boolean isEmpty(ArrayQueueADT queue) {
        return queue.size == 0;
    }

    // Pre: true
    // Post: n = 0
    public static void clear(ArrayQueueADT queue) {
        queue.elements = new Object[5];
        queue.head = 1;
        queue.size = 0;
    }

    // Pre: true
    // Post: immutable && R = "[{a[head].toString()}, {a[head + 1].toString()}, ..., {a[tale].toString()}]"
    public static String toStr(ArrayQueueADT queue) {
        StringBuilder result = new StringBuilder("[");
        if (queue.size > 0) {
            for (int i = queue.head; i < queue.head + queue.size - 1; i ++) {
                result.append(queue.elements[i % queue.elements.length].toString());
                result.append(", ");
            }
            result.append(queue.elements[(queue.head + queue.size - 1) % queue.elements.length]);
        }
        result.append(']');
        return result.toString();
    }
}
