package utils

import org.apache.pdfbox.Loader
import org.apache.pdfbox.text.PDFTextStripper
import java.io.File

object PdfReader {
    fun extractText(filePath: String): String {
        val file = File(filePath)
        if (!file.exists()) {
            throw IllegalArgumentException("File not found: $filePath")
        }

        Loader.loadPDF(file).use { document ->
            val stripper = PDFTextStripper()
            return stripper.getText(document)
        }
    }
}
