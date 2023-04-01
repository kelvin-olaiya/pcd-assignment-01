package pcd.assignment

import pcd.assignment.model.Counter
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.extension
import kotlin.io.path.isDirectory

class Controller {

    fun startCounting(counter: Counter) {
        val directory = Path.of(ROOT_FOLDER)
        val batches = getJavaFilesFrom(directory).generateBatches(DEFAULT_N_WORKERS)
        val workers = batches.filter { it.isNotEmpty() }.map { chunk -> Worker(chunk,counter) }.map { Thread(it) }
        workers.forEach { it.start() }
    }

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

    companion object {
        private val SEP = File.separator
        private val ROOT_FOLDER = "${SEP}home${SEP}kelvin"
        private const val DEFAULT_N_WORKERS = 5
    }
}