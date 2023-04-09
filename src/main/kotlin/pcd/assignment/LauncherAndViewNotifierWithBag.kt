package pcd.assignment

import pcd.assignment.model.Counter
import pcd.assignment.view.View
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.CountDownLatch
import kotlin.io.path.extension
import kotlin.io.path.isDirectory

class LauncherAndViewNotifierWithBag(
    private val rootPath: String,
    private val counter: Counter,
    private val numberOfWorkers: Int,
    private val view: View,
    private val stopFlag: Flag,
) : Thread() {

    private val terminationLatch = CountDownLatch(numberOfWorkers)
    override fun run() {
        val tasks = BagOfFiles()
        val workers = IntRange(1, numberOfWorkers).map { LinesCounter(tasks, counter, terminationLatch, stopFlag) }
        workers.forEach { it.start() }
        val start = System.currentTimeMillis()
        Files.walk(Path.of(rootPath))
            .filter { it.extension == "java" }
            .filter { !it.isDirectory() }
            .map { it.toFile() }
            .forEach { tasks.addFile(it) }
        tasks.close()
        terminationLatch.await()
        val duration = System.currentTimeMillis() - start
        view.countingCompleted(duration)
    }
}

private class LinesCounter(
    private val bag: BagOfFiles,
    private val counter: Counter,
    private val terminationLatch: CountDownLatch,
    private val stopFlag: Flag,
) : Thread() {
    override fun run() {
        while(!bag.isDone()) {
            if (stopFlag.isSet()) { terminationLatch.countDown(); return }
            bag.getFile().let {
                it.ifPresent { f -> counter.submit(f, f.readLines().size) }
            }
        }
        terminationLatch.countDown()
    }
}
