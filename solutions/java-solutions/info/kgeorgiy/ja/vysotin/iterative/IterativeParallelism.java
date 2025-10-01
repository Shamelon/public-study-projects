package info.kgeorgiy.ja.vysotin.iterative;

import info.kgeorgiy.java.advanced.iterative.NewScalarIP;
import info.kgeorgiy.java.advanced.iterative.ScalarIP;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public class IterativeParallelism implements ScalarIP, NewScalarIP {
    @Override
    public <T> T maximum(int threads, List<? extends T> values, Comparator<? super T> comparator, int step)
            throws InterruptedException {
        return maximum(threads, filterListByStep(values, step), comparator);
    }

    @Override
    public <T> T minimum(int threads, List<? extends T> values, Comparator<? super T> comparator, int step)
            throws InterruptedException {
        return minimum(threads, filterListByStep(values, step), comparator);
    }

    @Override
    public <T> boolean all(int threads, List<? extends T> values, Predicate<? super T> predicate, int step)
            throws InterruptedException {
        return all(threads, filterListByStep(values, step), predicate);
    }

    @Override
    public <T> boolean any(int threads, List<? extends T> values, Predicate<? super T> predicate, int step)
            throws InterruptedException {
        return any(threads, filterListByStep(values, step), predicate);
    }

    @Override
    public <T> int count(int threads, List<? extends T> values, Predicate<? super T> predicate, int step)
            throws InterruptedException {
        return count(threads, filterListByStep(values, step), predicate);
    }

    @Override
    public <T> T maximum(int threads, List<? extends T> values, Comparator<? super T> comparator)
            throws InterruptedException {
        return listParalleling(threads, values, list -> list.stream().max(comparator).orElseThrow(),
                list -> list.stream().max(comparator).orElseThrow());
    }

    @Override
    public <T> T minimum(int threads, List<? extends T> values, Comparator<? super T> comparator)
            throws InterruptedException {
        return maximum(threads, values, comparator.reversed());
    }

    @Override
    public <T> boolean all(int threads, List<? extends T> values, Predicate<? super T> predicate)
            throws InterruptedException {
        return listParalleling(threads, values, list -> list.stream().allMatch(predicate),
                list -> list.stream().allMatch(a -> a));
    }

    @Override
    public <T> boolean any(int threads, List<? extends T> values, Predicate<? super T> predicate)
            throws InterruptedException {
        return listParalleling(threads, values, list -> list.stream().anyMatch(predicate),
                list -> list.stream().anyMatch(a -> a));
    }

    @Override
    public <T> int count(int threads, List<? extends T> values, Predicate<? super T> predicate)
            throws InterruptedException {
        return listParalleling(threads, values, list -> (int) list.stream().filter(predicate).count(),
                list -> list.stream().reduce(Integer::sum).orElse(0));
    }

    private <T> List<T> filterListByStep(List<T> list, int step) {
        List<T> newList = new ArrayList<>();
        for (int i = 0; i < list.size(); i += step) {
            newList.add(list.get(i));
        }
        return newList;
    }

    private <T, R> R listParalleling(int threads, List<T> values, Function<List<T>, R> listFunction,
                                     Function<List<R>, R> finalFunction) throws InterruptedException {
        if (threads > values.size()) {
            threads = values.size();
        }
        int partSize = values.size() / threads;
        int remain = values.size() % threads;
        int currPos = 0;
        int step;
        List<ListParallelism<List<T>, R>> parts = new ArrayList<>();

        for (int i = 0; i < threads; ++i) {
            if (remain > 0) {
                step = partSize + 1;
                remain--;
            } else {
                step = partSize;
            }
            parts.add(new ListParallelism<>(values.subList(currPos, currPos + step), listFunction));
            currPos += step;
        }

        for (Thread part : parts) {
            part.start();
        }

        for (Thread part : parts) {
            part.join();
        }

        List<R> results = new ArrayList<>();
        for (ListParallelism<List<T>, R> part : parts) {
            results.add(part.getResult());
        }

        return finalFunction.apply(results);
    }

    private static final class ListParallelism<E, R> extends Thread {
        E list;
        Function<E, R> function;
        R result;

        public ListParallelism(E list, Function<E, R> function) {
            this.list = list;
            this.function = function;
        }

        @Override
        public void run() {
            result = function.apply(list);
        }

        public R getResult() {
            return result;
        }
    }
}
