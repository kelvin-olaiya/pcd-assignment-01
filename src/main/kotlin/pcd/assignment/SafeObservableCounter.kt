package pcd.assignment

import java.io.File

class SafeObservableCounter(
    maxLines: Int,
    numberOfIntervals: Int,
    numberOfLongestFiles: Int = 5,
) : ObservableCounter {

    private val counter = SafeCounter(maxLines, numberOfIntervals, numberOfLongestFiles)
    private val observers = mutableListOf<CounterObserver>()

    @Synchronized override fun addObserver(observer: CounterObserver) { observers.add(observer) }

    @Synchronized override fun submit(file: File, lines: Int) {
        counter.submit(file, lines)
        notifyObservers()
    }

    override val intervals: List<IntRange>
        @Synchronized get() = counter.intervals

    @Synchronized override fun filesInNthInterval(index: Int) = counter.filesInNthInterval(index)

    override val totalFiles: Int
        @Synchronized get() = counter.totalFiles

    @Synchronized override fun getNLongestFiles() = counter.getNLongestFiles()

    private fun notifyObservers() = observers.forEach { it.counterUpdated() }

}
