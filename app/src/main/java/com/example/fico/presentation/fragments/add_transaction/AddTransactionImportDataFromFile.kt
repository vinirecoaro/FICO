package com.example.fico.presentation.fragments.add_transaction

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.annotation.RequiresApi
import com.example.fico.R
import com.example.fico.model.Earning
import com.example.fico.model.Expense
import com.example.fico.model.ImportTransactionsFromFileResponse
import com.example.fico.utils.constants.StringConstants
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.DateUtil
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.File
import java.io.FileInputStream
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.time.LocalDateTime

class AddTransactionImportDataFromFile(){

    @RequiresApi(Build.VERSION_CODES.O)
    fun readFromExcelFile(filepath: String, context : Context): ImportTransactionsFromFileResponse {
        val inputStream = FileInputStream(filepath)
        val workbook = WorkbookFactory.create(inputStream)

        val expenseList = mutableListOf<Expense>()
        val earningList = mutableListOf<Earning>()
        val installmentExpenseList = mutableListOf<Expense>()

        var result = true
        var message = context.getString(R.string.import_transactions_success_message)
        val now = LocalDateTime.now().toString()

        fun readSheetIfExists(sheetName: String, type: String): Boolean {
            val sheet = workbook.getSheet(sheetName) ?: return false

            for (rowIndex in sheet.firstRowNum + 1..sheet.lastRowNum) {
                val row = sheet.getRow(rowIndex) ?: continue
                val firstCellValue = getCellValueAsString(row.getCell(0)).trim()

                if (firstCellValue.equals(StringConstants.XLS.FINAL_LINE_IDENTIFICATION, ignoreCase = true)) break

                try {
                    when (type) {
                        StringConstants.DATABASE.EXPENSE -> {
                            val price = formatMoney(getCellValueAsString(row.getCell(0)))
                            val description = formatDescription(getCellValueAsString(row.getCell(1)))
                            val category = getCellValueAsString(row.getCell(2)).trim()
                            val paymentDate = formatDate(row.getCell(3)) ?: throw Exception(
                                context.getString(R.string.invalid_date)
                            )
                            val purchaseDate = formatDate(row.getCell(4)) ?: throw Exception(
                                context.getString(R.string.invalid_date)
                            )
                            expenseList.add(
                                Expense(
                                    id = "",
                                    price = price,
                                    description = description,
                                    category = category,
                                    paymentDate = paymentDate,
                                    purchaseDate = purchaseDate,
                                    inputDateTime = now
                                )
                            )
                        }

                        StringConstants.DATABASE.EARNING -> {
                            val value = formatMoney(getCellValueAsString(row.getCell(0)))
                            val description = formatDescription(getCellValueAsString(row.getCell(1)))
                            val category = getCellValueAsString(row.getCell(2)).trim()
                            val date = formatDate(row.getCell(3)) ?: throw Exception(context.getString(
                                R.string.invalid_date
                            ))

                            earningList.add(
                                Earning(
                                    id = "",
                                    value = value,
                                    description = description,
                                    category = category,
                                    date = date,
                                    inputDateTime = now
                                )
                            )
                        }

                        StringConstants.DATABASE.INSTALLMENT_EXPENSE -> {
                            val price = formatMoney(getCellValueAsString(row.getCell(0)))
                            val description = formatDescription(getCellValueAsString(row.getCell(1)))
                            val category = getCellValueAsString(row.getCell(2)).trim()
                            val paymentDate = formatDate(row.getCell(3)) ?: throw Exception(context.getString(
                                R.string.invalid_date
                            ))
                            val purchaseDate = formatDate(row.getCell(4)) ?: throw Exception(context.getString(
                                R.string.invalid_date
                            ))
                            val nOfInstallment = getCellValueAsString(row.getCell(5)).trim()

                            installmentExpenseList.add(
                                Expense(
                                    id = "",
                                    price = price,
                                    description = description,
                                    category = category,
                                    paymentDate = paymentDate,
                                    purchaseDate = purchaseDate,
                                    inputDateTime = now,
                                    nOfInstallment = nOfInstallment
                                )
                            )
                        }
                    }
                } catch (e: Exception) {
                    result = false
                    message = "${context.getString(R.string.import_transaction_from_file_failure_on_process_line)} " +
                            "${rowIndex + 1} " +
                            "${context.getString(R.string.at_sheet)} " +
                            "'$sheetName': ${e.message}"
                    expenseList.clear()
                    earningList.clear()
                    installmentExpenseList.clear()
                    return true
                }
            }

            return true
        }

        val sheet1Read = readSheetIfExists(StringConstants.XLS.SHEET_NAME_EXPENSES, StringConstants.DATABASE.EXPENSE)
        val sheet2Read = readSheetIfExists(StringConstants.XLS.SHEET_NAME_EARNINGS, StringConstants.DATABASE.EARNING)
        val sheet3Read = readSheetIfExists(StringConstants.XLS.SHEET_NAME_INSTALLMENT_EXPENSES, StringConstants.DATABASE.INSTALLMENT_EXPENSE)

        if (!sheet1Read && !sheet2Read && !sheet3Read) {
            result = false
            message = context.getString(R.string.import_transactions_no_sheet_found_message)
        }

        workbook.close()
        inputStream.close()

        return ImportTransactionsFromFileResponse(
            expenseList = expenseList,
            earningList = earningList,
            installmentExpenseList = installmentExpenseList,
            result = result,
            message = message
        )
    }

    fun formatMoney(cellValue: String): String {
        val removeSpecialChars = cellValue.trim()
            .replace("R$", "")
            .replace("R$ ", "")
            .replace("$", "")
            .replace("$ ", "")
            .replace(",", "")
            .replace(".", "")

        val formattedValue = BigDecimal(removeSpecialChars).divide(BigDecimal(100)).setScale(8, RoundingMode.HALF_UP).toString()

        return formattedValue
    }

    fun formatDescription(cellValue: String): String {
        return cellValue.trim().replace("  ", " ").replace("  ", " ")
    }

    fun formatDate(cell: Cell): String? {
        val rawDate = when (cell.cellType) {
            CellType.STRING -> cell.stringCellValue.trim()
            CellType.NUMERIC -> {
                val dateDouble = cell.numericCellValue
                val dateFormat = SimpleDateFormat("dd/MM/yyyy")
                dateFormat.format(DateUtil.getJavaDate(dateDouble))
            }
            else -> return null
        }

        return if (verifyDateFormat(rawDate)) {
            val day = rawDate.substring(0, 2)
            val month = rawDate.substring(3, 5)
            val year = rawDate.substring(6, 10)
            "$year-$month-$day"
        } else null
    }

    private fun verifyDateFormat(date: String): Boolean {
        val dateFormat = "\\d{2}/\\d{2}/\\d{4}" // Expressão regular para o formato "dd/mm/aaaa"
        return date.matches(Regex(dateFormat))
    }

    fun getCellValueAsString(cell: Cell?): String {
        return when (cell?.cellType) {
            CellType.STRING -> cell.stringCellValue
            CellType.NUMERIC -> BigDecimal(cell.numericCellValue.toString()).setScale(2, RoundingMode.HALF_UP).toString()
            CellType.BOOLEAN -> cell.booleanCellValue.toString()
            else -> ""
        }
    }

    // Method to generate a URI for the new file
     fun getNewFileUri(): Uri {
        val downloadsDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val newFile = File(downloadsDir, "transactions.xlsx")
        return Uri.fromFile(newFile)
    }

}