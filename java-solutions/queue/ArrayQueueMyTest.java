package queue;

public class ArrayQueueMyTest {
    public static void fill(ArrayQueue queue, String prefix) {
        for (int i = 0; i < 10; i++) {
            queue.enqueue(prefix + i);
        }
    }

    public static void dump(ArrayQueue queue) {
        while (!queue.isEmpty()) {
            System.out.println(
                    queue.size() + " " +
                            queue.element() + " " +
                            queue.dequeue()
            );
        }
    }

    public static void main(String[] args) {
        ArrayQueue queue1 = new ArrayQueue();
        ArrayQueue queue2 = new ArrayQueue();
        fill(queue1, "s1_");
        fill(queue2, "s2_");
        System.out.println(queue1.toStr());
        System.out.println(queue2.toStr());
        dump(queue1);
        dump(queue2);
        fill(queue1, "s1_");
        fill(queue2, "s2_");
        queue1.clear();
        System.out.println(queue1.isEmpty());
        System.out.println(queue2.isEmpty());
    }
}
