package queue;

// Model: a[head]..a[tail]
// Let: n = tail - head + 1;
// Inv: n >= 0 &&
//      forall i=head..tail: a[i] != null
// Let: immutable(l, r): forall i=l..r: a'[i] = a[i]
// Let: immutable: immutable(head, tail) &&
//      head' = head && tail' = tail
public interface Queue {
    // Pre: element != null
    // Post: head' = head && tail' = tail + 1 &&
    //      a'[tail'] = element &&
    //      immutable(head, tail)
    void enqueue(Object element);

    // Pre: n > 0
    // Post: R = a[head] && immutable
    Object element();

    // Pre: n > 0
    // Post: head' = head + 1 && tail' = tail &&
    //      R = a[head] &&
    //      immutable(head', tail)
    Object dequeue();

    // Pre: true
    // Post: R = n && immutable
    int size();

    // Pre: true
    // Post: R = (n = 0) && immutable
    boolean isEmpty();

    // Pre: true
    // Post: n = 0
    void clear();

    // Pre: true
    // Post: immutable && R = "[{a[head].toString()}, {a[head + 1].toString()}, ..., {a[tail].toString()}]"
    String toStr();

    // Pre: element != null
    // Post: immutable && R = count of element in queue
    int count(Object element);
}
