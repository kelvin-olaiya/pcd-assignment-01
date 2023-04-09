package pcd.assignment

import pcd.assignment.model.Counter
import pcd.assignment.view.View

class Controller(
    private val view: View,
    private val rootFolder: String = DEFAULT_ROOT_FOLDER,
) {

    private val stopFlag = Flag()

    fun startCounting(counter: Counter, numberOfWorkers: Int = DEFAULT_N_WORKERS) {
        stopFlag.reset()
        LauncherAndViewNotifierWithBag(rootFolder, counter, numberOfWorkers, view, stopFlag).start()
        // LauncherAndViewNotifier(rootFolder, counter, numberOfWorkers, view, stopFlag).start()
    }

    fun stopCounting() = stopFlag.set()

    companion object {
        private val DEFAULT_ROOT_FOLDER = System.getProperty("user.home")
        private val DEFAULT_N_WORKERS = Runtime.getRuntime().availableProcessors() + 1
    }
}
