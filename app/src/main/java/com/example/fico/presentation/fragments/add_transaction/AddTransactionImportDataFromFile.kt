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
        return cellValue.trim()
            .replace("R$", "")
            .replace("R$ ", "")
            .replace("$", "")
            .replace("$ ", "")
            .replace(",", ".")
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

    /*fun readFromExcelFile(filepath: String): Pair<MutableList<Expense>, Boolean> {
        val inputStream = FileInputStream(filepath)
        //Instantiate Excel workbook using existing file:
        var xlWb = WorkbookFactory.create(inputStream)
        //Get reference to first sheet:
        val xlWs = xlWb.getSheetAt(0)

        var price = ""
        var description = ""
        var category = ""
        var date = ""
        var installment = ""

        var result = true

        val expenseList = mutableListOf<Expense>()

        val numberOfRows = xlWs.lastRowNum

        for (rowIndex in xlWs.firstRowNum + 1..numberOfRows) {
            val row = xlWs.getRow(rowIndex) ?: continue

            val numberOfColumns = row.lastCellNum

            // Iterando pelas colunas dentro da linha atual
            for (columnIndex in row.firstCellNum until numberOfColumns) {
                val cell = row.getCell(columnIndex)
                val cellValue = getCellValueAsString(cell)
                when (columnIndex) {
                    0 -> {
                        price = cellValue.replace("R$", "")
                        price = cellValue.replace("R$ ", "")
                        price = cellValue.replace("$", "")
                        price = cellValue.replace("$ ", "")
                        price = cellValue.replace(",", ".")
                        if (price == "xxx" || price == "XXX") {
                            return Pair(expenseList, result)
                        } else if (price.toDoubleOrNull() == null) {
                            result = false
                            expenseList.clear()
                            return Pair(expenseList, result)
                        }
                    }
                    1 -> {
                        description = cellValue.replace("  ", " ")
                        description = cellValue.replace("  ", " ")
                    }
                    2 -> {
                        category = cellValue
                    }
                    3 -> {
                        if (cell.cellType == CellType.STRING) {
                            date = cellValue
                        } else if (cell.cellType == CellType.NUMERIC) {
                            val dateDouble = cell.numericCellValue
                            val dateformat = SimpleDateFormat("dd/MM/yyyy")
                            date = dateformat.format(DateUtil.getJavaDate(dateDouble))
                        }

                        if (verifyDateFormat(date)) {
                            val day = date.substring(0, 2)
                            val month = date.substring(3, 5)
                            val year = date.substring(6, 10)
                            date = "$year-$month-$day"
                        } else {
                            result = false
                            expenseList.clear()
                            return Pair(expenseList, result)
                        }
                    }
                    4 -> {
                        installment = cellValue
                    }
                }

            }
            TODO("IMPLEMENT PURCHASE DATA AND INPUT_DATE_TIME")
            val expense = Expense("", price, description, category, date,"" ,"",installment)
            expenseList.add(expense)
        }
        xlWb.close()
        inputStream.close()

        return Pair(expenseList, result)
    }*/

    private fun verifyDateFormat(date: String): Boolean {
        val dateFormat = "\\d{2}/\\d{2}/\\d{4}" // Expressão regular para o formato "dd/mm/aaaa"
        return date.matches(Regex(dateFormat))
    }

    fun getCellValueAsString(cell: Cell?): String {
        return when (cell?.cellType) {
            CellType.STRING -> cell.stringCellValue
            CellType.NUMERIC -> cell.numericCellValue.toString()
            CellType.BOOLEAN -> cell.booleanCellValue.toString()
            else -> ""
        }
    }

    // Metodo para gerar uma URI para o novo arquivo (exemplo).
     fun getNewFileUri(): Uri {
        val downloadsDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val newFile = File(downloadsDir, "transactions.xlsx")
        return Uri.fromFile(newFile)
    }

}