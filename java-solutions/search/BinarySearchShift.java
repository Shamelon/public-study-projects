package search;

import java.util.Arrays;

public class BinarySearchShift {
	// Pre: массив a такой, что существует k такое, что 0 <= k < a.length &&
	// 		a[k:a.length] + a[0:k] - отсортированный по неубыванию массив
	//      forall i != j a[i] != a[j]
	// 		start = 0, end = a.length
    // Post: R = k && a[k:a.length] + a[0:k] - отсортированный по неубыванию массив
	
	// I1: start <= k < end, end - start >= 1 &&
    //      0 < start < end <= a.length
    // I2: i > j ->
    //      1) a[i] < a[j] if ((j >= k) and (i >= k) or (j < k) and (i < k))
    //      2) a[i] > a[j] if ((j < k) and (i >= k))
    public static int binarySearchShiftRec(int[] a, int start, int end) {
        if (end - start == 1) { // cond: end - start == 1
            // I1 & cond: start <= k < end <= a.length, end - start == 1
            // start <= k < start + 1
			// 0 < start < end <= a.length
            // k = start
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

            // mid < end and end <= a.length -> mid <= a.length - 1
            if (a[mid] > a[a.length - 1]) {
                // mid <= a.length - 1 and a[mid] > a[a.length - 1] -> I2: mid >= k
                // start <= k < mid + 1
                return binarySearchShiftRec(a, start, mid + 1);
            } else {
                // mid <= a.length - 1 and a[mid] <= a[a.length - 1] -> I2: mid < k
                // mid + 1 <= k < end
                return binarySearchShiftRec(a, mid + 1, end);
            }
        }
    }


	// Pre: массив a такой, что существует k такое, что 0 <= k < a.length &&
	// 		a[k:a.length] + a[0:k] - отсортированный по неубыванию массив
	//      forall i != j a[i] != a[j]
    // Post: R = k && a[k:a.length] + a[0:k] - отсортированный по неубыванию массив
    public static int binarySearchShiftIt(int[] a) {
        int start = 0;
        int end = a.length;
		// I1: start <= k < end, end - start >= 1 &&
		//      0 < start < end <= a.length
		// I2: i > j ->
		//      1) a[i] < a[j] if ((j >= k) and (i >= k) or (j < k) and (i < k))
		//      2) a[i] > a[j] if ((j < k) and (i >= k))
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

            // mid < end and end <= a.length -> mid <= a.length - 1
            if (a[mid] > a[a.length - 1]) {
                // mid <= a.length - 1 and a[mid] > a[a.length - 1] -> I2: mid >= k
                // start <= k < mid + 1
                end = mid + 1;
            } else {
                // mid <= a.length - 1 and a[mid] <= a[a.length - 1] -> I2: mid < k
                // mid + 1 <= k < end
                start = mid + 1;
            }
        }
        return start;
    }

	// Pre: args[i] - целое число типа int, пусть args[i] = a[i]
	//		массив a такой, что существует k такое, что 0 <= k < a.length &&
	// 		a[k:a.length] + a[0:k] - отсортированный по неубыванию массив
	//      forall i != j a[i] != a[j]
	// Post: в консоли выведено k такое что a[k:a.length] + a[0:k] - отсортированный по неубыванию массив
    public static void main(String[] args) {
        int n = args.length;
        int[] a = new int[n];
        for (int i = 0; i < n; i++) {
            a[i] = Integer.parseInt(args[i]);
        }
		
        if (Arrays.stream(a).sum() % 2 == 0) {
            System.out.println(binarySearchShiftRec(a, 0, n));
        } else {
            System.out.println(binarySearchShiftIt(a));
        }
    }
}
