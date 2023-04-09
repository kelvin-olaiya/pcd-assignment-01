package pcd.assignment

import java.io.File
import java.util.*
import java.util.concurrent.locks.ReentrantLock

// Blocking queue could be used instead
class BagOfFiles {
    private val queue = PriorityQueue<File>()
    private val mutex = ReentrantLock()
    private val fileAvailable = mutex.newCondition()
    private val completed = mutex.newCondition()
    private var closed = false
    private var done = false

    fun addFile(file: File) {
        mutex.lock()
        queue.add(file)
        fileAvailable.signalAll()
        mutex.unlock()
    }

    fun getFile(): Optional<File> {
        try {
            mutex.lock()
            while (queue.isEmpty() && !closed) {
                fileAvailable.await()
            }
            done = isCompleted()
            return Optional.ofNullable(queue.poll())
        } finally {
            completed.signalAll()
            mutex.unlock()
        }
    }

    fun close() {
        mutex.lock()
        closed = true
        fileAvailable.signalAll()
        mutex.unlock();
    }

    fun isDone(): Boolean {
        try {
            mutex.lock()
            return done
        } finally {
            mutex.unlock()
        }
    }

    private fun isCompleted(): Boolean = this.closed && queue.isEmpty()

    fun awaitCompletion() = try {
        mutex.lock()
        while(!isCompleted()) {
            completed.await();
        }
    } finally {
        mutex.unlock()
    }
}
