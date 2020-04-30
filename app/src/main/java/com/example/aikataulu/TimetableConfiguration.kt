package com.example.aikataulu

import android.content.Context
import android.util.Log
import com.example.aikataulu.models.newLine
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonSyntaxException
import com.google.gson.annotations.Expose
import java.io.File
import java.lang.Exception
import java.lang.IllegalStateException
import java.lang.reflect.Modifier

data class TimetableConfigurationData(var updateIntervalS: Int = 10, var stopName: String? = null, var autoUpdate: Boolean = false) { }

// Persistent configuration object for the app.
object TimetableConfiguration {
    private var isLoaded = false
    private const val TAG = "TIMETABLE.Configuration"
    private const val fileName = "configuration.json"
    var data: TimetableConfigurationData = TimetableConfigurationData()

    private fun loadFromFile(context: Context): TimetableConfigurationData {
        val file = File(context.filesDir.path).resolve(fileName)
        if (!file.exists()) {
            Log.i(TAG, "Configuration file did not exist. Creating configuration file...")
            saveToFile(context)
        } else {
            // Open file stream
            context.openFileInput(fileName).bufferedReader().use {
                // Open input stream, deserialize and assign fields to this
                val json = it.readLines().joinToString(newLine)
                it.close()
                try {
                    data = Gson().fromJson(json, TimetableConfigurationData::class.javaObjectType)
                } catch(ex: Exception) {
                    when(ex) {
                        is JsonSyntaxException,
                        is IllegalStateException -> {
                            // If JSON is malformed or the schema becomes unparseable, just go to defaults.
                            Log.w(TAG, "An error occurred while parsing configuration JSON. Deleting file.")
                            context.deleteFile(fileName)
                            return loadFromFile(context)
                        }
                    }
                }
            }
        }
        isLoaded = true
        return data
    }

    fun saveToFile(context: Context): TimetableConfigurationData {
        // Open file stream
        context.openFileOutput(fileName, Context.MODE_PRIVATE).use {
            // Serialize and write to file
            val json = Gson().toJson(data)
            it.write(json.toByteArray())
            it.close()
            Log.d(TAG, "Configuration:$newLine\t$json")
        }
        Log.i(TAG, "Configuration file was saved.")
        return data
    }

    fun ensureLoaded(context: Context): TimetableConfigurationData {
        val caller = Thread.currentThread().stackTrace[3].let {
            "${it.className}::${it.methodName}"
        }
        Log.i(TAG, "Configuration loading initiated by $caller...")
        if (!isLoaded) {
            loadFromFile(context)
            Log.i(TAG, "Configuration loaded.")
        } else Log.i(TAG, "Configuration was already loaded.")
        return data
    }
}