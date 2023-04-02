package pcd.assignment

import pcd.assignment.model.ObservableCounter
import pcd.assignment.model.SafeObservableCounter
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Dimension
import javax.swing.*

private class InputBox(prompt: String) : Box(BoxLayout.Y_AXIS) {

    val spinner = JSpinner(SpinnerNumberModel(0, 0, Int.MAX_VALUE, 1))
    init {
        add(JLabel(prompt).apply { alignmentX = Component.RIGHT_ALIGNMENT })
        add(spinner.apply { setSizeForText(this) })
    }
}

private class NumericBox(label: String) : Box(BoxLayout.Y_AXIS) {

    private val textField = JTextField()
    init {
        add(JLabel(label).apply {  alignmentX = Component.RIGHT_ALIGNMENT })
        add(textField.apply {
            setSizeForText(this)
            isEditable = false
        })
    }

    fun reset() = update(0)
    fun update(value: Int) { textField.text = value.toString() }
}

private fun setSizeForText(component: JComponent) {
    component.preferredSize = Dimension(100, 40)
    component.maximumSize = component.preferredSize
    component.maximumSize = component.preferredSize
    component.alignmentX = Component.RIGHT_ALIGNMENT
}

private class ListView(listModel: ListModel<String>) : JScrollPane(JList(listModel).apply {
    minimumSize = Dimension(300, 400)
    cellRenderer = DefaultListCellRenderer()
})

class GUI(
    controller: Controller,
) : CounterObserver {

    private val frame = JFrame("Assignment#01")
    private val countingListModel = DefaultListModel<String>()
    private val longestFilesModel = DefaultListModel<String>()
    private val counting = ListView(countingListModel)
    private val leaderboard = ListView(longestFilesModel)
    private val totalFilesBox = NumericBox("Files counted:")
    private var counter: ObservableCounter? = null

    init {
        frame.setSize(800, 600)
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        val panel = frame.contentPane

        val inputsPanel = Box(BoxLayout.X_AXIS)
        val maxLinesBox = InputBox("Max. lines")
        val intervalsBox = InputBox("N. Intervals")
        val longestFilesBox = InputBox("# of longestFiles")

        inputsPanel.add(maxLinesBox)
        inputsPanel.add(Box.createGlue())
        inputsPanel.add(intervalsBox)
        inputsPanel.add(Box.createGlue())
        inputsPanel.add(longestFilesBox)
        inputsPanel.border = BorderFactory.createEmptyBorder(20, 20, 20, 20)

        val mainPanel = Box(BoxLayout.X_AXIS)
        mainPanel.add(counting)
        mainPanel.add(Box.createGlue())
        mainPanel.add(leaderboard)
        mainPanel.border = BorderFactory.createEmptyBorder(20, 20, 20, 20)

        val controlsPanel = Box(BoxLayout.X_AXIS)
        val startButton = JButton("START").also { it.alignmentX = Component.CENTER_ALIGNMENT }
        val stopButton = JButton("STOP").also { it.alignmentX = Component.CENTER_ALIGNMENT }
        controlsPanel.add(startButton)
        controlsPanel.add(Box.createRigidArea(Dimension(20, 0)))
        controlsPanel.add(stopButton)
        controlsPanel.add(Box.createGlue())
        // controlsPanel.add(totalFilesBox)
        startButton.addActionListener {
            totalFilesBox.reset()
            val maxLines = maxLinesBox.spinner.model.value as Int
            val intervals = intervalsBox.spinner.model.value as Int
            val longestFiles = longestFilesBox.spinner.model.value as Int
            counter = SafeObservableCounter(maxLines, intervals, longestFiles)
            counter?.let {
                it.addObserver(this)
                controller.startCounting(it)
            }
        }
        stopButton.addActionListener { controller.stopCounting() }
        controlsPanel.border = BorderFactory.createEmptyBorder(0, 10, 10, 10)
        controlsPanel.alignmentX = Component.CENTER_ALIGNMENT

        panel.add(inputsPanel, BorderLayout.NORTH)
        panel.add(controlsPanel, BorderLayout.PAGE_END)
        panel.add(mainPanel, BorderLayout.CENTER)
        frame.isVisible = true
    }

    override fun counterUpdated(filesCounted: Int) {
        SwingUtilities.invokeLater {
            counter?.let {
                countingListModel.clear()
                // totalFilesBox.update(filesCounted)
                val results = it.intervals.withIndex().map { indexedValue ->
                    val index = indexedValue.index
                    val value = indexedValue.value
                    "[${value.first}; ${value.last}] => ${it.filesInNthInterval(index)}"
                }.toList()
                countingListModel.addAll(results)
                longestFilesModel.clear()
                longestFilesModel.addAll(it.getNLongestFiles())
            }
        }
    }
}