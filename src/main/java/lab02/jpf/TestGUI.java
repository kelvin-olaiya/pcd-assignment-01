package lab02.jpf;

import java.util.ArrayList;
import java.util.List;

public class TestGUI {

    private static final List<Integer> BATCH_1 = new ArrayList<Integer>() {{
        add(1); add(5); add(12);
    }};

    private static final List<Integer> BATCH_2 = new ArrayList<Integer>() {{
        add(22); add(2); add(7);
    }};

    public static class ObservableCounter {
        private final int[] list = new int[3];
        private final CounterObserver observer;

        public ObservableCounter(CounterObserver observer) {
            this.observer = observer;
        }

        synchronized public void submit(int count) {
            if (count < 5) {
                list[0] = list[0] + 1;
            } else if (count < 10) {
                list[1] = list[1] + 1;
            } else {
                list[2] = list[2] + 1;
            }
            int sum = 0;
            for(int l : list) {
                sum += l;
            }
            this.observer.counterUpdated(sum);
        }

        synchronized public int getCount(int interval) {
            return list[interval];
        }
    }

    public static class Worker extends Thread {

        private final List<Integer> fileLengths;
        private final ObservableCounter counter;
        public Worker(ObservableCounter counter, List<Integer> lengths) {
            this.fileLengths = lengths;
            this.counter = counter;
        }

        public void run() {
            for(int l : fileLengths) {
                counter.submit(l);
            }
        }
    }

    public static class Launcher extends Thread {
        private final ObservableCounter counter;
        private final CounterObserver view;
        public Launcher(ObservableCounter counter, CounterObserver view) {
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
        private final CounterObserver view;

        public Controller(CounterObserver view) {
            this.view = view;
        }
        public void startCounting(ObservableCounter counter) {
            new Launcher(counter, view).start();
        }
    }

    public static class CounterObserver {
        private final ObservableCounter counter;
        private int filesCounted = 0;

        public CounterObserver() {
            this.counter = new ObservableCounter(this);
            new Controller(this).startCounting(this.counter);
        }

        public void countingCompleted() {
            assert counter.getCount(0) == 2;
            assert counter.getCount(1) == 2;
            assert counter.getCount(2) == 2;
        }

        public void counterUpdated(int filesCounted) {
            this.filesCounted++;
            assert this.filesCounted == filesCounted;
        }
    }

    public static void main(String[] args) {
        new CounterObserver();
    }
}
