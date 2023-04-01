package pcd.assignment.model

import java.io.File

interface Counter {

    fun submit(file: File, lines: Int)
    val intervals: List<IntRange>
    fun filesInNthInterval(index: Int): Int
    val totalFiles: Int
    fun getNLongestFiles(): List<String>
}