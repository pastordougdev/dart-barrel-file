package pastordougdev.dartbarrelfile.dialog

import com.intellij.openapi.ui.DialogWrapper
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.Action
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel

class NoBarrelFileFoundDialog : DialogWrapper(true) {

    init {
        init()
        title = "No Barrel Files"
    }

    override fun createCenterPanel(): JComponent? {
        val panel = JPanel(BorderLayout())
        panel.preferredSize = Dimension(200, 50)
        val label = JLabel("No Barrel File(s) found in this directory\nor in tree above")
        panel.add(label, BorderLayout.CENTER)
        return panel
    }

    override fun createActions(): Array<Action> {
        return arrayOf(myOKAction)
    }

}