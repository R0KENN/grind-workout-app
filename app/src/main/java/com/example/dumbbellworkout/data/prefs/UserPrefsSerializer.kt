package com.example.dumbbellworkout.data.prefs

import androidx.datastore.core.Serializer
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

object UserPrefsSerializer : Serializer<UserPrefs> {
    override val defaultValue: UserPrefs = UserPrefs()

    override suspend fun readFrom(input: InputStream): UserPrefs =
        try {
            Json.decodeFromString(
                UserPrefs.serializer(),
                input.readBytes().decodeToString()
            )
        } catch (e: Exception) {
            defaultValue
        }

    override suspend fun writeTo(t: UserPrefs, output: OutputStream) {
        output.write(
            Json.encodeToString(UserPrefs.serializer(), t).encodeToByteArray()
        )
    }
}
