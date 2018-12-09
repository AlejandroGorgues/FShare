package com.example.alejandro.fshare

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import java.io.File


class PathClass {


    //Función que obtiene el nombre del objeto a partir de su URI
    fun getRealPathFromURI(context: Context, contentUri: Uri): String {
        val uriString = contentUri.toString()
        val myFile = File(uriString)
        var displayName: String? = null

        if (uriString.startsWith("content://")) {
            var cursor: Cursor? = null
            try {
                cursor = context.contentResolver.query(contentUri, null, null, null, null)
                if (cursor != null && cursor.moveToFirst()) {
                    displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                    cursor.close()
                }
            } finally {
                cursor!!.close()
            }
        } else if (uriString.startsWith("file://")) {
            displayName = myFile.name
        }
        return displayName!!
    }
}