package pcd.assignment.model

import java.io.File
import java.util.stream.Stream

class UnsafeCounter(
    private val maxLines: Int,
    private val numOfIntervals: Int,
    private val numberOfLongestFiles: Int = 5
) : Counter {

    override val intervals: List<IntRange> by lazy {
        val linesPerInterval = maxLines / (numOfIntervals - 1)
        Stream.concat(
            Stream.iterate(0) { it + linesPerInterval }
                .limit(numOfIntervals.toLong()-1)
                .map { it until  it + linesPerInterval },
            Stream.of(maxLines .. Int.MAX_VALUE)
        ).toList()
    }

    private val counters = intervals.map { _ -> 0 }.toMutableList()
    private val longestFiles = mutableListOf<Pair<String, Int>>()

    override fun submit(file: File, lines: Int) {
        intervals.indexOf(getInterval(lines)).let { counters[it] = counters[it] + 1 }
        longestFiles.add(Pair(file.name, lines))
        if (longestFiles.size > numberOfLongestFiles) {
            longestFiles.remove(longestFiles.minBy { it.second })
        }
    }

    private fun getInterval(lines: Int) = intervals.first { lines in it }

    override fun filesInNthInterval(index: Int) = counters[index]

    override fun toString(): String = buildString {
        intervals.forEachIndexed { index, range ->
            append("[$range] has ${counters[index]} files\n")
        }
        append("For a total of: $totalFiles files")
    }

    override val totalFiles: Int get() = counters.sum()

    override fun getNLongestFiles(): List<String> = longestFiles.sortedBy { it.second }.map { it.first }
}
