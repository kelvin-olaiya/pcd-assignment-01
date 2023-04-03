package pcd.assignment

import pcd.assignment.model.Counter
import java.io.File
import java.util.concurrent.CountDownLatch

class Worker(
    private val paths: List<String>,
    private val counter: Counter,
    private val stopFlag: Flag,
    private val completeLatch: CountDownLatch
) : Runnable {

    override fun run() {
        println("${Thread.currentThread().name} accepted a batch of size ${paths.size}")
        for (path in paths) {
            if (stopFlag.isSet()) {
                completeLatch.countDown()
                return
            }
            with(File(path)) { counter.submit(this, this.readLines().size) }

        }
        completeLatch.countDown()
        println("${Thread.currentThread().name} DONE!")
    }
}