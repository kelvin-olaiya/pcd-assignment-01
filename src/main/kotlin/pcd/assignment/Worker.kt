package pcd.assignment

import pcd.assignment.model.Counter
import java.io.File

class Worker(
    private val files: List<File>,
    private val counter: Counter,
    private val stopFlag: Flag,
) : Runnable {

    override fun run() {
        println("${Thread.currentThread().name} accepted a batch of size ${files.size}")
        for (file in files) {
            if (stopFlag.isSet()) {
                return
            }
            counter.submit(file, file.readLines().size)
        }
        println("${Thread.currentThread().name} DONE!")
    }
}