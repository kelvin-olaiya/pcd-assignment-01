package pcd.assignment

import pcd.assignment.model.Counter
import java.io.File
import java.util.concurrent.CountDownLatch

class Worker(
    private val files: List<File>,
    private val counter: Counter,
    private val stopFlag: Flag,
    private val completeLatch: CountDownLatch
) : Runnable {

    override fun run() {
        println("${Thread.currentThread().name} accepted a batch of size ${files.size}")
        for (file in files) {
            if (stopFlag.isSet()) {
                return
            }
            counter.submit(file, file.readLines().size)
        }
        completeLatch.countDown()
        println("${Thread.currentThread().name} DONE!")
    }
}