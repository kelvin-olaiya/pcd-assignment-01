package pcd.assignment

interface ObservableCounter : Counter {

    fun addObserver(observer: CounterObserver)
}