package queue;

import java.util.Arrays;
import java.util.Objects;

public class ArrayQueue extends AbstractQueue {
    private int head = 0;
    private Object[] elements = new Object[5];

    protected void enqueueImpl(Object element) {
        elements[posInArray(head + size - 1)] = element;
        ensureCapacity(size);
    }

    // Pre: true
    // Post: immutable
    private void ensureCapacity(int capacity) {
        if (capacity >= elements.length) {
            Object[] newElements = new Object[elements.length * 2];
            for (int i = head; i < size + head; i++) {
                newElements[i] = elements[posInArray(i)];
            }
            elements = newElements;
        }
    }
    protected Object elementImpl() {
        return elements[head];
    }

    protected Object dequeueImpl() {
        Object result = elements[head];
        head = posInArray(head + 1);
        return result;
    }

    protected void clearImpl() {
        elements = new Object[5];
        head = 0;
    }

    protected void elementsToString(StringBuilder result) {
        for (int i = head; i < head + size - 1; i ++) {
            result.append(elements[posInArray(i)].toString());
            result.append(", ");
        }
        result.append(elements[(head + size - 1) % elements.length]);
    }

    private int posInArray(int realPos) {
        return realPos % elements.length;
    }
}
