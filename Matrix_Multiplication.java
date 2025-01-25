import java.util.*;
import java.lang.Math;


class Calculation extends Thread {
    int[][] result;
    int[][] a;
    int[][] b;
    int row;


    public Calculation(int[][] a, int[][] b, int[][] result, int row) {
        this.a = a;
        this.b = b;
        this.result = result;
        this.row = row;
    }


    public void run() {
        for (int i = 0; i < b[0].length; i++) {
            result[row][i] = 0;
            for (int j = 0; j < a[row].length; j++) {
                result[row][i] += a[row][j] * b[j][i];
            }
        }
    }
}

class Initialize extends Thread {
    int[][] a;
    int row;


    public Initialize(int[][] a, int row) {
        this.a = a;
        this.row = row;
    }


    public void run() {
        for (int i = 0; i < a.length; i++) {
            a[row][i] = (int) (Math.random() * 11); // Generate elements between 0 and 10
        }
    }
}

public class Matrix_Multiplication {
    public static void main(String[] args) throws Exception {
        int no_of_threads = Integer.parseInt(args[0]); // Number of threads to use
        int size = 1000; // Size of the matrices
        int[][] a = new int[size][size];
        int[][] b = new int[size][size];
        int[][] result = new int[a.length][b[0].length];
        long starttime = System.currentTimeMillis();

        List<Thread> thread_list = new ArrayList<>();

        // Initialize matrix A using multiple threads
        for (int i = 0; i < a.length; i++) {
            Initialize one_thread = new Initialize(a, i);
            one_thread.start();
            thread_list.add(one_thread);
            if (thread_list.size() == no_of_threads) {
                for (Thread node : thread_list) {
                    node.join();
                }
                thread_list.clear();
            }
        }

        // Initialize matrix B using multiple threads
        for (int i = 0; i < b.length; i++) {
            Initialize one_thread = new Initialize(b, i);
            one_thread.start();
            thread_list.add(one_thread);
            if (thread_list.size() == no_of_threads) {
                for (Thread node : thread_list) {
                    node.join();
                }
                thread_list.clear();
            }
        }

        // Wait for all initialization threads to complete
        for (Thread thread : thread_list) {
            thread.join();
        }
        thread_list.clear();

        // Perform matrix multiplication using multiple threads
        for (int i = 0; i < a.length; i++) {
            Calculation one_thread = new Calculation(a, b, result, i);
            one_thread.start();
            thread_list.add(one_thread);
            if (thread_list.size() == no_of_threads) {
                for (Thread node : thread_list) {
                    node.join();
                }
                thread_list.clear();
            }
        }

        // Wait for all calculation threads to complete
        for (Thread thread : thread_list) {
            thread.join();
        }
        thread_list.clear();

        long endtime = System.currentTimeMillis();

        System.out.println("Time taken for 1000x1000 matrix multiplication (in ms) = " + (endtime - starttime));
    }
}