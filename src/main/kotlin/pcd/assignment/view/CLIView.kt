package pcd.assignment.view

import pcd.assignment.Controller
import pcd.assignment.model.SafeCounter

class CLIView(
    maxLines: Int,
    numberOfIntervals: Int,
    numberOfLongestFiles: Int,
    numberOfWorkers: Int = Runtime.getRuntime().availableProcessors() + 1
) : View {

    private val controller = Controller(this)
    private val counter = SafeCounter(maxLines, numberOfIntervals, numberOfLongestFiles)

    init {
        controller.startCounting(counter, numberOfWorkers)
    }

    override fun countingCompleted(duration: Long) {
        println("---------LONGEST FILES---------")
        counter.getNLongestFiles().forEach { println(it) }
        println("-------------------------------")
        println(counter)
        println("-------------------------------")
        println("Counting done in $duration ms")
    }
}