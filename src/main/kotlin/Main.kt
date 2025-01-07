package io.github.hanihashemi.payslipimporter

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper
import java.io.File
import java.util.Scanner

fun main() {
    // Get the PDF file path from the user
    val scanner = Scanner(System.`in`)
    println("Enter the path to your payslip PDF file:")
    val pdfFilePath = scanner.nextLine()

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

    // Read and process the PDF file
    val extractedData = extractFieldsFromPDF(pdfFilePath, fieldsToExtract)

    // Print the extracted data
    extractedData.forEach { (field, value) ->
        println("$field: $value")
    }
}

fun extractFieldsFromPDF(pdfFilePath: String, fields: List<String>): Map<String, String> {
    val extractedData = mutableMapOf<String, String>()

    try {
        // Load the PDF document
        PDDocument.load(File(pdfFilePath)).use { document ->
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
        println("Error reading PDF: ${e.message}")
    }

    return extractedData
}
