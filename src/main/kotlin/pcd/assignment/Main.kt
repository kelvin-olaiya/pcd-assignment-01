package pcd.assignment

import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.extension
import kotlin.io.path.isDirectory

private fun getJavaFilesFrom(path: Path): List<Path> = Files.walk(path)
    .filter { it.extension == "java" }
    .filter { !it.isDirectory() }
    .toList()

private fun List<Path>.generateBatches(nThreads: Int): List<List<Path>> {
    val numberOfFiles = this.size
    val filesPerBatch = (numberOfFiles / nThreads).let { if (it == 0) numberOfFiles else it }
    val splitIndex = ((numberOfFiles - (numberOfFiles % nThreads)) - filesPerBatch).let {
        if (it < 0) numberOfFiles else it
    }
    return slice(0 until splitIndex).chunked(filesPerBatch) + listOf(subList(splitIndex, numberOfFiles))
}

private const val DEFAULT_N_WORKERS = 10

fun main(args: Array<String>) {
    check(args.size == 3) { "Wrong number of parameters provided" }
    val directory = Path.of(args[0])
    val numOfIntervals = args[1].toInt()
    val maxLines = args[2].toInt()
    val counter = SafeCounter(maxLines, numOfIntervals)
    check(directory.toFile().isDirectory) { "$directory is not a directory" }
    val batches = getJavaFilesFrom(directory).generateBatches(DEFAULT_N_WORKERS)
    val workers = batches.filter { it.isNotEmpty() }.map { chunk -> Worker(chunk,counter) }.map { Thread(it) }
    val startTime = System.currentTimeMillis()
    workers.forEach { it.start() }
    workers.forEach { it.join() }
    val duration = System.currentTimeMillis() - startTime
    println(counter)
    println("Counted in $duration ms using ${workers.size} threads")
    println("Longest files:")
    counter.getNLongestFiles().forEach { println(it) }
}