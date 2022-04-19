package pastordougdev.dartbarrelfile.dialog

import com.intellij.openapi.ui.DialogWrapper
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel

class NotBarrelFileDialogDialog : DialogWrapper(true) {

    private val message1 = "<html><p>File is not a previously generated barrel file.</p>"
    private val message2 = "<p>OK to replace the contents. CANCEL to abort</p></html>"
    init {
        init()
        title = "Are You Sure?"
    }

    override fun createCenterPanel(): JComponent? {
        val panel = JPanel(BorderLayout())
        panel.preferredSize = Dimension(200, 50)
        val label = JLabel(message1 + "\n\n" + message2)
        panel.add(label, BorderLayout.CENTER)
        return panel
    }
}