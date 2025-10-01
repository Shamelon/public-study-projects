package search;

import java.util.Arrays;
public class BinarySearch {

    // Пусть y - такой, что a[y] <= x and (y == 0 or a[y - 1] > x)
    // Рассматриваем полуинтервалы [start, end)

    // I1: start <= y < end, end - start >= 1
    // I2: i >= j -> a[i] <= a[j]
    public static int binarySearchRec(int x, int[] a, int start, int end) {
        if (end - start == 1) { // cond: end - start == 1
            // I1 & cond: start <= y < end, end - start == 1
            // start <= y < start + 1
            // y = start
            return start;
        } else {
            int mid = (start + end - 1) / 2;
            // start <= mid < end (*)
            /* Док-во (*)
            * start <= (start + end - 1) / 2 < end
            * Если (start + end) % 2 == 1:
            * start + start <= start + end - 1 < end + end
            * start <= end - 1 and start - 1 < end
            * end - start >= 1 and end - start > -1  ( оба верны из Pre)
            * Если (start + end) % 2 == 0:
            * start + start <= start + end - 2 < end + end
            * start <= end - 2 and start - 2 < end
            * end - start >= 2 and end - start > -2
            * end - start != 1 т.к. (start + end) % 2 == 0
            * Значит end - start >= 2 and end - start > -2 верно
            *  */
            if (a[mid] <= x) {
                // cond & I2: a[mid] <= x, for all i in [mid, end): i >= mid -> a[i] <= a[mid]
                // for all i in [mid, end): a[i] <= a[x]
                // for all i in [mid + 1, end): i != y
                // start <= y < mid + 1
                return binarySearchRec(x, a, start, mid + 1);
            } else {
                // !cond & I2: a[mid] > x, for all i in [start, mid + 1): mid >= i -> a[mid] <= a[i]
                // for all i in [start, mid + 1): a[i] > a[x]
                // for all i in [start, mid + 1): i != y
                // mid + 1 <= y < end
                return binarySearchRec(x, a, mid + 1, end);
            }
        }
    }
    // Post: a[y] <= x and (y == 0 or a[y - 1] > x)

    public static int binarySearchIt(int x, int[] a) {
        int start = 0;
        int end = a.length;
        // I1: start <= y < end, end - start >= 1
        // I2: i >= j -> a[i] <= a[j]
        while (end - start > 1) {
            int mid = (start + end - 1) / 2;
            // start <= mid < end (*)
            /* Док-во (*)
             * start <= (start + end - 1) / 2 < end
             * Если (start + end) % 2 == 1:
             * start + start <= start + end - 1 < end + end
             * start <= end - 1 and start - 1 < end
             * end - start >= 1 and end - start > -1  ( оба верны из Pre)
             * Если (start + end) % 2 == 0:
             * start + start <= start + end - 2 < end + end
             * start <= end - 2 and start - 2 < end
             * end - start >= 2 and end - start > -2
             * end - start != 1 т.к. (start + end) % 2 == 0
             * Значит end - start >= 2 and end - start > -2 верно
             *  */
            if (a[mid] <= x) {
                // cond & I2: a[mid] <= x, for all i in [mid, end): i >= mid -> a[i] <= a[mid]
                // for all i in [mid, end): a[i] <= a[x]
                // for all i in [mid + 1, end): i != y
                // start <= y < mid + 1
                end = mid + 1;
            } else {
                // !cond & I2: a[mid] > x, for all i in [start, mid + 1): mid >= i -> a[mid] <= a[i]
                // for all i in [start, mid + 1): a[i] > a[x]
                // for all i in [start, mid + 1): i != y
                // mid + 1 <= y < end
                start = mid + 1;
            }
        }
        return start;
    }
    // Post: a[y] <= x and (y == 0 or a[y - 1] > x)


    public static void main(String[] args) {
        int x = Integer.parseInt(args[0]);
        int n = args.length;
        int[] a = new int[n];
        for (int i = 1; i < n; i++) {
            a[i - 1] = Integer.parseInt(args[i]);
        }
        a[n - 1] = Integer.MIN_VALUE;

        if (Arrays.stream(a).sum() % 2 == 0) {
            System.out.println(binarySearchRec(x, a, 0, n));
        } else {
            System.out.println(binarySearchIt(x, a));
        }
    }
}