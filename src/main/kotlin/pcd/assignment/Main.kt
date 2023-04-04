package pcd.assignment

import pcd.assignment.view.CLIView
import pcd.assignment.view.GUIView

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        GUIView()
    } else {
        if (args.size == 4) {
            CLIView(args[0].toInt(), args[1].toInt(), args[2].toInt(), args[3].toInt())
        } else {
            CLIView(args[0].toInt(), args[1].toInt(), args[2].toInt())
        }
    }
}
