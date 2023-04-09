package lab02.jpf;

import java.util.ArrayList;
import java.util.List;

public class TestCLI {

    private static final List<Integer> BATCH_1 = new ArrayList<Integer>() {{
        add(1); add(5); add(12);
    }};

    private static final List<Integer> BATCH_2 = new ArrayList<Integer>() {{
        add(22); add(2); add(7);
    }};

    public static class Counter {
        private final int[] list = new int[3];

        synchronized public void submit(String filename, int count) {
            if (count < 5) {
                list[0] = list[0] + 1;
            } else if (count < 10) {
                list[1] = list[1] + 1;
            } else {
                list[2] = list[2] + 1;
            }
        }

        synchronized public int getCount(int interval) {
            return list[interval];
        }
    }

    public static class Worker extends Thread {

        private final List<Integer> fileLengths;
        private final Counter counter;
        public Worker(Counter counter, List<Integer> legths) {
            this.fileLengths = legths;
            this.counter = counter;
        }

        public void run() {
            for(int l : fileLengths) {
                counter.submit("", l);
            }

        }
    }

    public static class Launcher extends Thread {
        private final Counter counter;
        private final View view;
        public Launcher(Counter counter, View view) {
            this.counter = counter;
            this.view = view;
        }
        public void run() {
            Worker workerA = new Worker(this.counter, BATCH_1);
            Worker workerB = new Worker(this.counter, BATCH_2);
            try {
                workerA.start();
                workerB.start();
                workerA.join();
                workerB.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            view.countingCompleted();
        }
    }

    public static class Controller {
        private final View view;

        public Controller(View view) {
            this.view = view;
        }
        public void startCounting(Counter counter) {
            new Launcher(counter, view).start();
        }
    }

    public static class View extends Thread {
        private final Counter counter;
        public View() {
            this.counter = new Counter();
        }

        public void run() {
            new Controller(this).startCounting(this.counter);
        }

        public void countingCompleted() {
            assert counter.getCount(0) == 2;
            assert counter.getCount(1) == 2;
            assert counter.getCount(2) == 2;
        }
    }

    public static void main(String[] args) {
        new View().start();
    }
}
