package pcd.assignment

import pcd.assignment.model.Counter
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.extension
import kotlin.io.path.isDirectory

class Controller {

    private val stopFlag = Flag()
    private val workload = getJavaFilesFrom(Path.of(DEFAULT_ROOT_FOLDER))


    fun startCounting(counter: Counter, numberOfWorkers: Int = DEFAULT_N_WORKERS) {
        stopFlag.reset()
        workload.generateBatches(numberOfWorkers)
            .filter { it.isNotEmpty() }
            .map { Worker(it, counter, stopFlag) }
            .withIndex()
            .map { Thread(it.value, "[Thread-${it.index}]") }
            .forEach { it.start() }
    }

    private fun getJavaFilesFrom(path: Path): List<File> = Files.walk(path)
        .filter { it.extension == "java" }
        .filter { !it.isDirectory() }
        .map { it.toFile() }
        .toList()

    private fun List<File>.generateBatches(nThreads: Int): List<List<File>> {
        val numberOfFiles = this.size
        val filesPerBatch = (numberOfFiles / nThreads).let { if (it == 0) numberOfFiles else it }
        val splitIndex = ((numberOfFiles - (numberOfFiles % nThreads)) - filesPerBatch).let {
            if (it < 0) numberOfFiles else it
        }
        return slice(0 until splitIndex).chunked(filesPerBatch) + listOf(subList(splitIndex, numberOfFiles))
    }

    fun stopCounting() = stopFlag.set()

    companion object {
        private val DEFAULT_ROOT_FOLDER = System.getProperty("user.home")
        private const val DEFAULT_N_WORKERS = 9
    }
}
