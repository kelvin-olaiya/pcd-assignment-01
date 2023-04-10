package pcd.assignment

import java.io.File
import java.util.*
import java.util.concurrent.locks.ReentrantLock

// Blocking queue could be used instead
class Bag<T> {
    private val queue = LinkedList<T>()
    private val mutex = ReentrantLock()
    private val fileAvailable = mutex.newCondition()
    private val completed = mutex.newCondition()
    private var closed = false
    private var done = false

    fun addTask(task: T) {
        mutex.lock()
        queue.add(task)
        fileAvailable.signalAll()
        mutex.unlock()
    }

    fun getTask(): Optional<T> {
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
}
