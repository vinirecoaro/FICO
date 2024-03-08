package com.example.fico.presentation.fragments.expense.add_expense

import android.net.Uri
import android.os.Environment
import com.example.fico.domain.model.Expense
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.DateUtil
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.File
import java.io.FileInputStream
import java.text.SimpleDateFormat

class AddExpenseImportaDataFromFile(){

    fun readFromExcelFile(filepath: String): Pair<MutableList<Expense>, Boolean> {
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
            val expense = Expense("", price, description, category, date, installment)
            expenseList.add(expense)
        }
        xlWb.close()
        inputStream.close()

        return Pair(expenseList, result)
    }

    private fun verifyDateFormat(date: String): Boolean {
        val formatoData = "\\d{2}/\\d{2}/\\d{4}" // Expressão regular para o formato "dd/mm/aaaa"
        return date.matches(Regex(formatoData))
    }

    fun getCellValueAsString(cell: Cell?): String {
        return when (cell?.cellType) {
            CellType.STRING -> cell.stringCellValue
            CellType.NUMERIC -> cell.numericCellValue.toString()
            CellType.BOOLEAN -> cell.booleanCellValue.toString()
            else -> ""
        }
    }

    // Método para gerar uma URI para o novo arquivo (exemplo).
     fun getNewFileUri(): Uri {
        val downloadsDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val newFile = File(downloadsDir, "expenses.xlsx")
        return Uri.fromFile(newFile)
    }

}