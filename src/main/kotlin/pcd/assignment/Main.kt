package pcd.assignment

import pcd.assignment.view.CLIView
import pcd.assignment.view.GUIView

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        GUIView(withBag = true)
    } else {
        if (args.size >= 4) {
            CLIView(args[0].toInt(), args[1].toInt(), args[2].toInt(), args[3].toInt(), withBag = args.contains("-b"))
        } else {
            CLIView(args[0].toInt(), args[1].toInt(), args[2].toInt(), withBag = args.contains("-b"))
        }
    }
}
