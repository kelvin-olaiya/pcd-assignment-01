package pcd.assignment

import pcd.assignment.model.Counter
import pcd.assignment.view.View
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import java.util.concurrent.CountDownLatch
import kotlin.io.path.extension
import kotlin.io.path.isDirectory
import kotlin.jvm.optionals.getOrElse

class BatchLauncherAndViewNotifier(
    private val rootFolder: String,
    private val counter: Counter,
    private val numberOfWorker: Int,
    private val view: View,
    private val stopFlag: Flag,
) : Thread("CompletionWaiter"), Launcher {

    override fun run() {
        val start = System.currentTimeMillis()
        val batches = getJavaFilesFrom(Path.of(rootFolder)).generateBatches(numberOfWorker)
        val completionLatch = CountDownLatch(batches.count())
        val workers = batches.map { Worker(it, counter, stopFlag, completionLatch) }
            .withIndex()
            .map { Thread(it.value, "[Worker-${it.index}]") }
        workers.forEach { it.start() }
        completionLatch.await()
        val duration = System.currentTimeMillis() - start
        view.countingCompleted(duration)
    }

    private fun getJavaFilesFrom(path: Path): List<String> = Files.walk(path)
        .filter { it.extension == "java" }
        .filter { !it.isDirectory() }
        .map { it.toFile().absolutePath }
        .toList()

    private fun List<String>.generateBatches(nThreads: Int): List<Batch> {
        val numberOfFiles = this.size
        val coercedNThread = nThreads.coerceAtLeast(1)
        val filesPerBatch = Optional.of(numberOfFiles / coercedNThread)
            .filter { it != 0 }.getOrElse { numberOfFiles }
        val splitIndex = Optional.of((numberOfFiles - (numberOfFiles % coercedNThread)) - filesPerBatch)
            .filter { it != 0 }.getOrElse { numberOfFiles }
        return slice(0 until splitIndex).chunked(filesPerBatch) + listOf(subList(splitIndex, numberOfFiles))
    }
}

private class Worker(
    private val paths: List<String>,
    private val counter: Counter,
    private val stopFlag: Flag,
    private val completeLatch: CountDownLatch
) : Runnable {

    override fun run() {
        println("${Thread.currentThread().name} accepted a batch of size ${paths.size}")
        for (path in paths) {
            if (stopFlag.isSet()) { completeLatch.countDown(); return }
            with(File(path)) { counter.submit(this, this.readLines().size) }
        }
        completeLatch.countDown()
        println("${Thread.currentThread().name} DONE!")
    }
}
