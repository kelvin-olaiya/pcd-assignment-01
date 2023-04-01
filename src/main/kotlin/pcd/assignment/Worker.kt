package pcd.assignment

import pcd.assignment.model.Counter
import java.nio.file.Path

class Worker(
    private val files: List<Path>,
    private val counter: Counter,
) : Runnable {

    override fun run() {
        files.map { it.toFile() }.forEach {
            counter.submit(it, it.readLines().size)
            Thread.sleep(5)
        }
    }
}