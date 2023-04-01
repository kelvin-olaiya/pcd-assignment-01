package pcd.assignment.model

interface ObservableCounter : Counter {

    fun addObserver(observer: CounterObserver)
}