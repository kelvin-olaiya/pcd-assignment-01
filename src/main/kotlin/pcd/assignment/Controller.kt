package pcd.assignment

import pcd.assignment.model.Counter
import pcd.assignment.view.View
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import kotlin.io.path.extension
import kotlin.io.path.isDirectory
import kotlin.jvm.optionals.getOrElse

class Controller(
    private val view: View,
    rootFolder: String = DEFAULT_ROOT_FOLDER
) {

    private val stopFlag = Flag()
    private val workload = getJavaFilesFrom(Path.of(rootFolder))

    fun startCounting(counter: Counter, numberOfWorkers: Int = DEFAULT_N_WORKERS) {
        stopFlag.reset()
        val batches = workload.generateBatches(numberOfWorkers).filter { it.isNotEmpty() }
        LauncherAndViewNotifier(batches, counter, view, stopFlag).start()
    }

    private fun getJavaFilesFrom(path: Path): List<String> = Files.walk(path)
        .filter { it.extension == "java" }
        .filter { !it.isDirectory() }
        .map { it.toFile().absolutePath }
        .toList()

    private fun List<String>.generateBatches(nThreads: Int): List<Batch> {
        val numberOfFiles = this.size
        val coercedNThread = nThreads.coerceAtLeast(1)
        val filesPerBatch = Optional.of(numberOfFiles / coercedNThread)
            .filter { it != 0 }.getOrElse { numberOfFiles }
        val splitIndex = Optional.of((numberOfFiles - (numberOfFiles % coercedNThread)) - filesPerBatch)
            .filter { it != 0 }.getOrElse { numberOfFiles }
        return slice(0 until splitIndex).chunked(filesPerBatch) + listOf(subList(splitIndex, numberOfFiles))
    }

    fun stopCounting() = stopFlag.set()

    companion object {
        private val DEFAULT_ROOT_FOLDER = System.getProperty("user.home")
        private val DEFAULT_N_WORKERS = Runtime.getRuntime().availableProcessors() + 1
    }
}
