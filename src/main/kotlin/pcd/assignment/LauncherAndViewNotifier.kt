package pcd.assignment

import pcd.assignment.model.Counter
import pcd.assignment.pcd.assignment.Batch
import pcd.assignment.view.View
import java.io.File
import java.util.concurrent.CountDownLatch

class LauncherAndViewNotifier(
    private val batches: Iterable<Batch>,
    private val counter: Counter,
    private val view: View,
    private val stopFlag: Flag,
) : Thread("CompletionWaiter") {
    override fun run() {
        val completionLatch = CountDownLatch(batches.count())
        val workers = batches.map { Worker(it, counter, stopFlag, completionLatch) }
            .withIndex()
            .map { Thread(it.value, "[Worker-${it.index}]") }
        val start = System.currentTimeMillis()
        workers.forEach { it.start() }
        completionLatch.await()
        val duration = System.currentTimeMillis() - start
        view.countingCompleted(duration)
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
