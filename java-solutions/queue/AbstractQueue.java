package queue;

import java.util.Objects;


public abstract class AbstractQueue implements Queue {
    protected int size;
    @Override
    public void enqueue(Object element) {
        Objects.requireNonNull(element);
        size++;
        enqueueImpl(element);
    }

    protected abstract void enqueueImpl(Object element);

    @Override
    public Object element() {
        assert size > 0;

        return elementImpl();
    }

    protected abstract Object elementImpl();

    @Override
    public Object dequeue() {
        assert size > 0;
        size --;
        return dequeueImpl();
    }

    protected abstract Object dequeueImpl();

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public void clear() {
        size = 0;

        clearImpl();
    }

    protected abstract void clearImpl();

    @Override
    public String toStr() {
        StringBuilder result = new StringBuilder("[");
        if (size > 0) {
            elementsToString(result);
        }
        result.append(']');
        return result.toString();
    }

    protected abstract void elementsToString(StringBuilder result);

    // Pre: True
    // Post: R = count of element in queue; immutable
    public int count(Object element) {
        int s = 0;
        for (int i = 0; i < size; i ++) {
            if (element().equals(element)) {
                s ++;
            }
            enqueue(dequeue());
        }
        return s;
    }
}
