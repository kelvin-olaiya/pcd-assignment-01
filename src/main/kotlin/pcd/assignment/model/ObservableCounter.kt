package pcd.assignment.model

import pcd.assignment.CounterObserver

interface ObservableCounter : Counter {

    fun addObserver(observer: CounterObserver)
}