package queue;

public class LinkedQueue extends AbstractQueue {
    private Node head;
    private Node tail;
    protected void enqueueImpl(Object element) {
        Node prevTail = tail;
        tail = new Node(element, null);
        if (size == 1) {
            head = tail;
        } else {
            prevTail.next = tail;
        }
    }

    protected Object elementImpl() {
        return head.value;
    }

    protected Object dequeueImpl() {
        Node prevHead = head;
        head = prevHead.next;
        return prevHead.value;
    }

    protected void clearImpl() {
        head = null;
        tail = null;
    }

    protected void elementsToString(StringBuilder result) {
        Node currEl = head;
        for (int i = 0; i < size - 1; i ++) {
            result.append(currEl.value);
            result.append(", ");
            currEl = currEl.next;
        }
        result.append(currEl.value);
    }

    private class Node {
        private final Object value;
        private Node next;

        // Pre: true
        // Post: immutable
        public Node(Object value, Node next) {
            assert value != null;

            this.value = value;
            this.next = next;
        }
    }
}
