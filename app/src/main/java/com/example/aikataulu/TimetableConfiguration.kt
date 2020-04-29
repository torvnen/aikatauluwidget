package com.example.aikataulu

import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.annotations.Expose
import java.io.File
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable
import kotlin.reflect.typeOf

// Persistent configuration object for the app.
object TimetableConfiguration {
    @Expose(serialize = false) private val _fileName = "configuration.json"
    @Expose(serialize = false) var isLoaded = false
    @Expose(serialize = true) var updateIntervalS: Int = 10
    @Expose(serialize = true) var stopName: String? = null
    @Expose(serialize = true) var autoUpdate = false

    fun saveToFile(context: Context): TimetableConfiguration {
        // Open file stream
        context.openFileOutput(_fileName, Context.MODE_PRIVATE).use {
            // Serialize and write to file
            val json = Gson().toJson(this)
            it.write(json.toByteArray())
            it.close()
        }
        return this
    }

    fun ensureLoaded(context: Context) {
        if (!isLoaded) loadFromFile(context)
    }

    fun loadFromFile(context: Context): TimetableConfiguration {
        val file = File(context.filesDir.path).resolve(_fileName)
        if (!file.exists()) {
            saveToFile(context)
        } else {
            // Open file stream
            context.openFileInput(_fileName).bufferedReader().use {
                // Open input stream, deserialize and assign fields to this
                val json = it.readLines().joinToString(System.lineSeparator())
                it.close()
                try {
                    val savedConfiguration = Gson().fromJson(json, TimetableConfiguration::class.javaObjectType)

                    autoUpdate = savedConfiguration.autoUpdate
                    stopName = savedConfiguration.stopName
                    updateIntervalS = savedConfiguration.updateIntervalS
                } catch(jsex: JsonSyntaxException) {
                    context.deleteFile(_fileName)
                }
            }
        }
        isLoaded = true
        return this
    }
}