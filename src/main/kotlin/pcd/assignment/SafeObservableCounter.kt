package pcd.assignment

import java.io.File

class SafeObservableCounter(
    maxLines: Int,
    numberOfIntervals: Int,
    numberOfLongestFiles: Int = 5,
    private val counter: Counter = SafeCounter(maxLines, numberOfIntervals, numberOfLongestFiles)
) : ObservableCounter, Counter by counter {

    private val observers = mutableListOf<CounterObserver>()

    @Synchronized override fun addObserver(observer: CounterObserver) { observers.add(observer) }

    @Synchronized override fun submit(file: File, lines: Int) {
        counter.submit(file, lines)
        notifyObservers()
    }

    private fun notifyObservers() = observers.forEach { it.counterUpdated() }

}
