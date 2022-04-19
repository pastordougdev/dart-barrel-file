package pastordougdev.dartbarrelfile.dialog

import com.intellij.ide.util.DefaultPsiElementCellRenderer
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.LabeledComponent
import com.intellij.ui.CollectionListModel
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.components.JBList
import java.awt.BorderLayout
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField

class BarrelDialog (
    project: Project,
    private val dirName: String,
    private val availableFileNames: List<String>
) : DialogWrapper(project) {

    private val filesCollection: CollectionListModel<String>
    private val filesList: JBList<String>
    private val filesComponent: LabeledComponent<JPanel>
    private val barrelLabel: JLabel
    private val barrelFileName: JTextField
    init {
        filesCollection = CollectionListModel(
            availableFileNames.map { it }
        )

        filesList = JBList(filesCollection).apply{
            cellRenderer = DefaultPsiElementCellRenderer()
            selectedIndices = availableFileNames
                .withIndex()
                .map { it.index }
                .toIntArray()
        }

        val decorator = ToolbarDecorator.createDecorator(filesList)
            .disableAddAction()
        val panel = decorator.createPanel()

        filesComponent = LabeledComponent.create(
            panel,
            "Files to include in barrel file"
        )

        barrelLabel = JLabel("Barrel File Name")
        barrelFileName = JTextField(40)
        barrelFileName.text = dirName
        init()
    }
    override fun createCenterPanel(): JComponent? {
        val panel = JPanel(BorderLayout())
        panel.add(barrelFileNamePanel(), BorderLayout.NORTH)
        panel.add(filesComponent, BorderLayout.CENTER)
        return panel
        //return filesComponent
    }

    override fun getPreferredFocusedComponent(): JComponent? {
        return filesList
    }

    private fun barrelFileNamePanel(): JComponent? {
        //val panel = JPanel(GridLayout(0, 2))
        val panel = JPanel(BorderLayout())
        panel.add(barrelLabel, BorderLayout.LINE_START);
        panel.add(barrelFileName, BorderLayout.CENTER);
        return panel;
    }

    private fun extractSelectedIndices(): Set<Int> {
        return this.filesList.selectedIndices.toSet()
    }

    fun getSelectedFiles(): List<String> {
        val selectedIndices = extractSelectedIndices();
        return this.availableFileNames.filterIndexed { index, _ -> index in selectedIndices }
    }

    fun getBarrelFileName(): String {
        return "${this.barrelFileName.text}.dart";
    }
}