package io.github.hanihashemi.payslipimporter

import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfReader
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File

fun main() {
    val directoryPath = "/Users/h.hashemifar/Downloads/Salary"
    val outputFile = File("/Users/h.hashemifar/Downloads/PayslipData.xlsx")

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
        MyField("Direktversicherung", isMinus = true),
        MyField("Gesamtbeitrag zur PV", isMinus = true),
        MyField("Gesamtbeitrag zur KV", isMinus = true)
    )

    // Create Excel workbook
    val workbook: Workbook = XSSFWorkbook()
    val sheet = workbook.createSheet("Payslip Data")

    // Add header row
    val headerRow = sheet.createRow(0)
    headerRow.createCell(0).setCellValue("Field Name")
    pdfFiles.forEachIndexed { index, pdfFile ->
        headerRow.createCell(index + 1).setCellValue(pdfFile.name)
    }

    // Add rows for each field
    fieldsToExtract.forEachIndexed { fieldIndex, field ->
        val row = sheet.createRow(fieldIndex + 1)
        row.createCell(0).setCellValue(field.name)

        pdfFiles.forEachIndexed { fileIndex, pdfFile ->
            val extractedData = extractFieldsFromPDF(pdfFile, fieldsToExtract)
            val value = extractedData[field.name] ?: "Not Found"
            row.createCell(fileIndex + 1).setCellValue(value)
            println("File: ${pdfFile.name}")
            extractedData.entries.forEach { println("${it.key}: ${it.value}") }
            println("=====================================")
        }
    }

    // Save Excel file
    workbook.use { it.write(outputFile.outputStream()) }
    println("Data written to ${outputFile.absolutePath}")
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

            val lines = pdfText.lines()
            fields.forEach { field ->
                if (field.isValueNextLine) {
                    val lineIndex = lines.indexOfFirst { it.contains(field.key) }
                    if (lineIndex != -1 && lineIndex + 1 < lines.size) {
                        val valueLine = lines[lineIndex + 1].trim()
                        val value = valueLine.split(Regex("\\s+")).lastOrNull() ?: "Not Found"
                        extractedData[field.name] = if (field.isMinus) "-$value" else value
                    } else {
                        extractedData[field.name] = "Not Found"
                    }
                } else {
                    val regex = Regex("\\b${field.key}\\b.*?(-?[\\d.,]+)")
                    val match = regex.find(pdfText.toString())
                    val value = match?.groups?.get(1)?.value ?: "Not Found"
                    extractedData[field.name] = if (field.isMinus) "-$value" else value
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
    val isMinus: Boolean = false,
)