package pcd.assignment.model

import java.io.File

class SafeCounter(
    maxLines: Int,
    numOfIntervals: Int,
    numberOfLongestFiles: Int = 5
) : Counter {

    private val counter = UnsafeCounter(maxLines, numOfIntervals, numberOfLongestFiles)

    @Synchronized override fun submit(file: File, lines: Int) = counter.submit(file, lines)

    override val intervals: List<IntRange> = counter.intervals

    @Synchronized override fun filesInNthInterval(index: Int) = counter.filesInNthInterval(index)

    override val totalFiles: Int
        @Synchronized get() = counter.totalFiles

    @Synchronized override fun getNLongestFiles() = counter.getNLongestFiles()

    @Synchronized override fun toString() = counter.toString()
}