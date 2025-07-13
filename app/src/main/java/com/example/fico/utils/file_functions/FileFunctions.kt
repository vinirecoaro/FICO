package com.example.fico.utils.file_functions

import android.os.Environment
import android.view.View
import com.example.fico.R
import com.example.fico.presentation.components.PersonalizedSnackBars
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class FileFunctions {
    companion object {
        fun copyFromAssetsToDownloadDeviceFolder(assetFolderName : String, fileName : String, fileExtension: String, rootView: View) {
            val context = rootView.context
            val assetManager = context.assets

            try {
                val inputStream = assetManager.open("$assetFolderName/$fileName$fileExtension")

                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

                var file = "$fileName$fileExtension"
                var outFile = File(downloadsDir, file)
                var index = 1

                while (outFile.exists()) {
                    file = "$fileName($index)$fileExtension"
                    outFile = File(downloadsDir, file)
                    index++
                }

                val outputStream = FileOutputStream(outFile)

                inputStream.copyTo(outputStream)

                inputStream.close()
                outputStream.close()

                PersonalizedSnackBars.successMessage(
                    rootView,
                    context.getString(R.string.file_saved_on_downloads_message)
                ).show()

            } catch (e: IOException) {
                e.printStackTrace()
                PersonalizedSnackBars.failureMessage(
                    rootView,
                    context.getString(R.string.error_on_saving_file_message)
                ).show()
            }
        }

    }
}