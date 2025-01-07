package io.github.hanihashemi.payslipimporter

import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfReader
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor
import java.io.File

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
        MyField("Grundgehalt"),
        MyField(key = "Steuer/Sozialversicherung", name = "Gesamt-Brutto", isValueNextLine = true),
        MyField("Steuerrechtliche Abzüge", isValueNextLine = true),
        MyField("SV-rechtliche Abzüge", isValueNextLine = true),
        MyField(
            key = "Verdienstbescheinigung Netto-Bezüge/Netto-Abzüge",
            name = "Netto-Verdienst",
            isValueNextLine = true
        ),
        MyField("Direktversicherung"),
        MyField("Gesamtbeitrag zur PV"),
        MyField("Gesamtbeitrag zur KV")
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

fun extractFieldsFromPDF(pdfFile: File, fields: List<MyField>): Map<String, String> {
    val extractedData = mutableMapOf<String, String>()

    try {
        // Open the PDF document
        PdfDocument(PdfReader(pdfFile)).use { document ->
            val pdfText = StringBuilder()

            // Extract text from each page
            for (i in 1..document.numberOfPages) {
                pdfText.append(PdfTextExtractor.getTextFromPage(document.getPage(i)))
            }

            // Print PDF text for debugging
            println("Extracted text from ${pdfFile.name}:")
//            println(pdfText.toString())

            val lines = pdfText.lines()
            fields.forEach { field ->
                if (field.isValueNextLine) {
                    val lineIndex = lines.indexOfFirst { it.contains(field.key) }
                    if (lineIndex != -1 && lineIndex + 1 < lines.size) {
                        val valueLine = lines[lineIndex + 1].trim()
                        val value = valueLine.split(Regex("\\s+")).lastOrNull() ?: "Not Found"
                        extractedData[field.name] = value
                    } else {
                        extractedData[field.name] = "Not Found"
                    }
                } else {
                    val regex = when (field.key) {
//                        "Gesamt-Brutto" -> Regex("\\b${field.key}\\b.*?\\n.*?Steuer/Sozialversicherung\\s+([\\d.,]+)")
                        "Steuerrechtliche Abzüge" -> Regex("\\b${field.key}\\b.*?\\n.*?([\\d.,]+)$")
                        else -> Regex("\\b${field.key}\\b.*?([\\d.,]+)") // Default matching
                    }
                    val match = regex.find(pdfText.toString())
                    val value = match?.groups?.get(1)?.value ?: "Not Found"
                    extractedData[field.name] = value
                }
            }

        }
    } catch (e: Exception) {
        println("Error reading PDF ${pdfFile.name}: ${e.message}")
    }

    return extractedData
}

data class MyField(
    val key: String,
    val name: String = key,
    val isValueNextLine: Boolean = false,
)