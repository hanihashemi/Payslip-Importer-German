# Payslip Importer

This repository contains a Kotlin-based tool designed to extract specific data fields from multiple PDF payslip files and compile the results into an organized Excel spreadsheet. The tool uses the following libraries:

- **iText**: For parsing and extracting text from PDF files.
- **Apache POI**: For creating and managing Excel files.

## Features

- **Batch Processing**: Processes all PDF files in a specified directory.
- **Customizable Field Extraction**: Supports extracting predefined fields from the payslips.
- **Excel Report Generation**: Compiles extracted data into an Excel file with fields as rows and PDF files as columns.
- **Negative Value Support**: Handles fields that may include negative values.

## How It Works

1. **Input**: A directory containing PDF payslip files.
2. **Field Extraction**: The tool extracts specific fields based on configurable keys and patterns.
3. **Excel Generation**: Outputs the extracted data into an Excel file where:
    - Rows represent predefined field names.
    - Columns represent individual PDF files.

## Getting Started

### Prerequisites

- Kotlin setup in your development environment.
- Required dependencies in your `build.gradle.kts` file:

```kotlin
dependencies {
    implementation("com.itextpdf:itext7-core:7.2.5")
    implementation("org.apache.poi:poi-ooxml:5.2.3")
}
```

### Usage

1. Place your PDF payslip files in a directory (e.g., `/Users/yourname/Downloads/Salary`).
2. Update the `directoryPath` variable in the `main()` function to point to your directory.
3. Run the program.
4. The extracted data will be saved as `PayslipData.xlsx` in the same directory.

## Configuration

Fields to extract are defined in the `fieldsToExtract` list. Each field is represented by the `MyField` data class:

```kotlin
data class MyField(
    val key: String,       // The text to search for in the PDF
    val name: String = key, // The name to display in the Excel file
    val isValueNextLine: Boolean = false // If the value appears in the next line after the key
)
```

### Example

```kotlin
val fieldsToExtract = listOf(
    MyField("Grundgehalt"),
    MyField(key = "Steuer/Sozialversicherung", name = "Gesamt-Brutto", isValueNextLine = true),
    MyField("Steuerrechtliche Abzüge", isValueNextLine = true),
    MyField("SV-rechtliche Abzüge", isValueNextLine = true),
    MyField("Direktversicherung")
)
```

## Output Format

The output Excel file will have:

- **Header Row**: Names of the PDF files.
- **First Column**: Names of the predefined fields.
- **Data Cells**: Extracted values from the PDF files.

## Limitations

- Assumes consistent formatting across all PDF files.
- May require adjustments to `fieldsToExtract` for different document layouts.

## License

This project is licensed under the MIT License.

