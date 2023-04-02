package pcd.assignment.model

import pcd.assignment.view.CounterObserver

interface ObservableCounter : Counter {

    fun addObserver(observer: CounterObserver)
}