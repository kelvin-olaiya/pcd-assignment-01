package lab02.jpf;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class TestCLI2 {

    private static final List<Integer> FILES = new ArrayList<Integer>() {{
        add(1); // add(5); add(12); add(22); add(2); add(7);
    }};

    public static class Counter {
        private final int[] list = new int[3];

        synchronized public void submit(int count) {
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

    public static class Bag {
        private final LinkedList<Integer> queue = new LinkedList<Integer>();
        private boolean closed = false;

        public synchronized void addTask(int task) {
            queue.add(task);
            notifyAll();
        }

        public synchronized Integer getTask() throws InterruptedException {
            while (queue.isEmpty() && !this.closed) {
                wait();
            }
            return queue.poll();
        }

        public synchronized void close() {
            this.closed = true;
            notifyAll();
        }

        public synchronized boolean isDone() {
            return this.closed && queue.isEmpty();
        }
    }

    public static class FileReader extends Thread {
        private final Bag tasks;
        private final Bag results;

        public FileReader(Bag tasks, Bag results) {
            this.tasks = tasks;
            this.results = results;
        }

        public void run() {
            while(true) {
                Integer task;
                try {
                    task = tasks.getTask();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                if (task != null) {
                    results.addTask(task);
                }
                if (tasks.isDone()) {
                    results.close();
                    return;
                }
            }
        }
    }

    public static class CounterUpdater extends Thread {
        private final Counter counter;
        private final Bag results;

        public CounterUpdater(Counter counter, Bag results) {
            this.counter = counter;
            this.results = results;
        }

        public void run() {
            while (true) {
                Integer result;
                try {
                    result = results.getTask();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                if (result != null) {
                    counter.submit(result);
                }
                if (results.isDone()) {
                    return;
                }
            }
        }
    }

    public static class Launcher extends Thread {
        private final Counter counter;
        private final View view;
        private final Bag tasks;
        private final Bag results;

        public Launcher(Counter counter, View view) {
            this.counter = counter;
            this.view = view;
            tasks = new Bag();
            results = new Bag();
        }
        public void run() {
            FileReader fileReaderA = new FileReader(tasks, results);
            FileReader fileReaderB = new FileReader(tasks, results);
            CounterUpdater updater = new CounterUpdater(counter, results);
            try {
                fileReaderA.start();
                fileReaderB.start();
                updater.start();
                for(int l : FILES) {
                    tasks.addTask(l);
                }
                tasks.close();
                updater.join();
                // view.countingCompleted();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
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
            assert counter.getCount(0) == 1;
            assert counter.getCount(1) == 0;
            assert counter.getCount(2) == 0;
        }
    }

    public static void main(String[] args) {
        new View().start();
    }
}
