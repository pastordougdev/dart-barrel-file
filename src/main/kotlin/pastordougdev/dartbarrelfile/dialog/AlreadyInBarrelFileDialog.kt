package pastordougdev.dartbarrelfile.dialog

import com.intellij.openapi.ui.DialogWrapper
import com.intellij.psi.PsiFile
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.*

class AlreadyInBarrelFileDialog(
    private val dartFile: PsiFile,
    private val inBarrelFiles: List<PsiFile>
) : DialogWrapper(true) {

    init {
        init()
        title = "Already In Barrel File(s)"
    }


    override fun createCenterPanel(): JComponent? {
        val panel = JPanel(BorderLayout(10, 10))
        val label = JLabel("${dartFile.name} already in barrel file(s):")
        label.setSize(300, 60)
        panel.add(label, BorderLayout.PAGE_START)

        //val filesPanel = JPanel(BorderLayout())

        //val fileNames = StringBuilder()
//        for(barrelFile in inBarrelFiles) {
//            println("appending ${barrelFile.name}")
//            //fileNames.append("${barrelFile.name}\n")
//            filesPanel.add(JLabel("${barrelFile.name}"), BorderLayout.AFTER_LAST_LINE)
//        }

        //val filesLabel = JLabel(fileNames.toString())
        //panel.add(filesLabel, BorderLayout.CENTER)

        val filesPanel = fileListPane(inBarrelFiles)
        panel.add(filesPanel, BorderLayout.CENTER)

        panel.add(JLabel("OK to continue add, CANCEL to cancel add"), BorderLayout.PAGE_END)
        return panel
    }

    private fun fileListPane(barrelFiles: List<PsiFile>): JComponent {
        val listPane = JPanel()
        listPane.layout = BoxLayout(listPane, BoxLayout.PAGE_AXIS)

        for(file in barrelFiles) {
            listPane.add(JLabel("${file.name}"))
            listPane.add(Box.createRigidArea(Dimension(0, 5)))
        }
        return listPane
    }
}