package io.github.hanihashemi.payslipimporter
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper
import java.io.File
import java.util.Scanner

fun main() {
    // Get the directory path from the user
//    val scanner = Scanner(System.`in`)
//    println("Enter the path to the directory containing your payslip PDF files:")
//    val directoryPath = scanner.nextLine()
    val directoryPath = "/Users/h.hashemifar/Downloads/Salary"

    // Get all PDF files in the directory, sorted by name and last modified date
    val pdfFiles = File(directoryPath).listFiles { file -> file.extension.equals("pdf", ignoreCase = true) }
        ?.sortedWith(compareBy<File> { it.name })
        ?: run {
            println("No PDF files found in the specified directory.")
            return
        }

    // Define the fields you want to extract
    val fieldsToExtract = listOf(
        "Grundgehalt",
        "Gesamt-Brutto",
        "Netto-Verdienst",
        "Steuerrechtliche Abzüge",
        "SV-rechtliche Abzüge",
        "Direktversicherung",
        "Gesamtbeitrag zur PV",
        "Gesamtbeitrag zur KV"
    )

    // Process each PDF file
    pdfFiles.forEach { pdfFile ->
        println("Processing file: ${pdfFile.name}")
        val extractedData = extractFieldsFromPDF(pdfFile, fieldsToExtract)

        // Print the extracted data
        extractedData.forEach { (field, value) ->
            println("$field: $value")
        }
        println("-------------------------------------")
    }
}

fun extractFieldsFromPDF(pdfFile: File, fields: List<String>): Map<String, String> {
    val extractedData = mutableMapOf<String, String>()

    try {
        // Load the PDF document
        PDDocument.load(pdfFile).use { document ->
            // Extract text from the PDF
            val pdfText = PDFTextStripper().getText(document)

            // Extract each field based on its key or pattern
            fields.forEach { field ->
                val regex = Regex("$field\\s+([\\d.,]+)")
                val match = regex.find(pdfText)
                val value = match?.groups?.get(1)?.value ?: "Not Found"
                extractedData[field] = value
            }
        }
    } catch (e: Exception) {
        println("Error reading PDF ${pdfFile.name}: ${e.message}")
    }

    return extractedData
}
