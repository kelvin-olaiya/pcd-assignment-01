package pcd.assignment

import pcd.assignment.model.Counter
import java.io.File

class Worker(
    private val files: List<File>,
    private val counter: Counter,
    private val stopFlag: Flag,
) : Runnable {

    override fun run() {
        for (file in files) {
            if (stopFlag.isSet()) {
                return
            }
            counter.submit(file, file.readLines().size)
        }
    }
}