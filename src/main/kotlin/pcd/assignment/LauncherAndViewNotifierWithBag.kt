package pcd.assignment

import pcd.assignment.model.Counter
import pcd.assignment.view.View
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.CountDownLatch
import kotlin.io.path.extension
import kotlin.io.path.isDirectory

class LauncherAndViewNotifierWithBag(
    private val rootPath: String,
    counter: Counter,
    numberOfWorkers: Int,
    private val view: View,
    private val stopFlag: Flag,
) : Thread() {
    private val tasks = Bag<File>()
    private val results = Bag<Pair<File, Int>>()
    private val terminationLatch = CountDownLatch(1)
    private val workers = IntRange(1, numberOfWorkers)
        .map { LinesCounter(tasks, results, stopFlag) }
    private val counterUpdater = CounterUpdater(results, counter, terminationLatch, stopFlag)

    init {
        counterUpdater.start()
        workers.forEach { it.start() }
    }
    override fun run() {
        val start = System.currentTimeMillis()
        Files.walk(Path.of(rootPath))
            .filter { it.extension == "java" }
            .filter { !it.isDirectory() }
            .map { it.toFile() }
            .forEach { tasks.addTask(it) }
        tasks.close()
        terminationLatch.await()
        val duration = System.currentTimeMillis() - start
        view.countingCompleted(duration)
    }
}

private class LinesCounter(
    private val tasks: Bag<File>,
    private val results: Bag<Pair<File, Int>>,
    private val stopFlag: Flag,
) : Thread() {
    override fun run() {
        while(!tasks.isDone()) {
            if (stopFlag.isSet()) { return }
            tasks.getTask().let {
                it.ifPresent { f -> results.addTask(f to f.readLines().size) }
            }
            if (tasks.isDone()) results.close()
        }
    }
}

private class CounterUpdater(
    private val results: Bag<Pair<File, Int>>,
    private val counter: Counter,
    private val terminationLatch: CountDownLatch,
    private val stopFlag: Flag,
) : Thread() {
    override fun run() {
        while (!results.isDone()) {
            if (stopFlag.isSet()) { terminationLatch.countDown(); return }
            results.getTask().let {
                it.ifPresent { p -> counter.submit(p.first, p.second) }
            }
        }
        terminationLatch.countDown()
    }
}
