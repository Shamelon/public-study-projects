package queue;

public class QueueMyTest {
    public static void fill(Queue queue, String prefix) {
        for (int i = 0; i < 10; i++) {
            queue.enqueue(prefix + i);
        }
    }

    public static void dump(Queue queue) {
        while (!queue.isEmpty()) {
            System.out.println(
                    queue.size() + " " +
                            queue.element() + " " +
                            queue.dequeue()
            );
        }
    }

    public static void main(String[] args) {
        Queue queue1 = new ArrayQueue();
        Queue queue2 = new LinkedQueue();
        fill(queue1, "s1_");
        fill(queue2, "s2_");
        System.out.println(queue1.toStr());
        System.out.println(queue2.toStr());
        dump(queue1);
        dump(queue2);
        fill(queue1, "s1_");
        fill(queue2, "s2_");
        System.out.println(queue1.isEmpty());
        System.out.println(queue2.isEmpty());
        queue1.clear();
        queue2.clear();
        System.out.println(queue1.isEmpty());
        System.out.println(queue2.isEmpty());
    }
}
