package com.example.fico.presentation.interfaces

import android.app.Activity
import android.os.Environment
import android.text.TextUtils
import android.text.format.DateFormat
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import java.io.File
import java.io.FileOutputStream
import java.util.*
import kotlin.collections.HashMap

interface XLSInterface {

    fun generateXlsFile(activity : Activity, expensesTitles : Array<String>, earningsTitles : Array<String>,
                        expensesIndexName : Array<String>, earningsIndexName : Array<String>, expensesList : JsonArray,
                        earningsList : JsonArray, otherValueMap : HashMap<String, String>, expensesSheetName : String,
                        earningsSheetName : String, fileName : String, otherRowItemCount : Int) : File? {

        try{
            val wb : Workbook = HSSFWorkbook()
            val expensesSheet : Sheet = wb.createSheet(expensesSheetName)
            var cell : Cell
            var rowIndex = 0

            if(otherValueMap.isNotEmpty() && otherValueMap.keys.isNotEmpty()){
                var keys : Set<String> = otherValueMap.keys
                var i = 0
                var limit = 0
                var row : Row = expensesSheet.createRow(rowIndex)
                for(one in keys){
                    if(otherValueMap.containsKey(one)){
                        if(limit == otherRowItemCount){
                            ++rowIndex
                            i = 0
                            limit = 0
                        }else{
                            if(i != 0){
                                ++i
                                cell = row.createCell(i)
                                cell.setCellValue("")
                            }
                        }
                        cell = row.createCell(i)
                        cell.setCellValue(one)
                        i++

                        cell = row.createCell(i)
                        cell.setCellValue(otherValueMap.get(one))
                        ++i
                        ++limit
                    }
                }
                ++rowIndex
                expensesSheet.createRow(rowIndex)
                ++rowIndex
            }

            val row : Row = expensesSheet.createRow(rowIndex)

            ++rowIndex
            var a = 0

            for (title in expensesTitles){
                cell = row.createCell(a)
                cell.setCellValue(title)
                a++
            }

            for(j in 0 until 123){
                expensesSheet.setColumnWidth(j,(30*200))
            }

            for(i in 0 until expensesList.size()){

                val jsonObject : JsonObject = expensesList.get(i).asJsonObject

                if(jsonObject != null){
                    var b = 0
                    val row1 : Row = expensesSheet.createRow(i + rowIndex)

                    for( index in expensesIndexName){
                        val cell = row1.createCell(b)
                        try{
                            if(index != null && !TextUtils.isEmpty(index)){
                                if(jsonObject.has(index) &&
                                    jsonObject.get(index).asString != null){
                                    cell.setCellValue(jsonObject.get(index).asString)
                                }else {
                                    cell.setCellValue(" - ")
                                }
                            }
                        }catch (e : java.lang.Exception){
                            e.printStackTrace()
                        }
                        ++b
                    }
                }
            }

            var file : File? = null

            try{
                file = getFile(activity, fileName + System.currentTimeMillis()+".xls")
            }catch(e : java.lang.Exception){
                e.printStackTrace()
            }

            var fileOutputStream : FileOutputStream = FileOutputStream(file?.path)
            wb.write(fileOutputStream)
            fileOutputStream.close()
            return file

        }catch (e : java.lang.Exception){
            e.printStackTrace()
        }

        return null
    }

    fun getFile(activity : Activity, fileName : String) : File{
        val now = Date()
        DateFormat.format("yyyy-MM-_hh:mm:ss", now)
        val rootFolder = File(activity.getExternalFilesDir(Environment.DIRECTORY_DCIM).toString())
        val filePath = File(rootFolder, fileName.replace(Regex("^a-zZ-Z0-9._&"),""))

        if(filePath.exists()){
            filePath.mkdir()
        }
        if(!filePath.exists()){
            filePath.createNewFile()
        }else {
            filePath.delete()
            filePath.createNewFile()
        }
        return filePath
    }
}