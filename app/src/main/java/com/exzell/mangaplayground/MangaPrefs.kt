package com.exzell.mangaplayground

import android.content.Context
import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import androidx.core.content.edit
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

typealias PrefCallback = (String) -> Unit

@Singleton
class MangaPrefs @Inject constructor(private val context: Context) : SharedPreferences.OnSharedPreferenceChangeListener {

    private val handler: Handler = Handler(Looper.getMainLooper())

    private val pref: SharedPreferences

    private val settingsPref: SharedPreferences

    val listeners: ArrayList<PrefCallback> = arrayListOf()

    init {
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        pref.registerOnSharedPreferenceChangeListener(this)

        settingsPref = context.getSharedPreferences(PREF_SETTINGS_NAME, Context.MODE_PRIVATE)
        settingsPref.registerOnSharedPreferenceChangeListener(this)
    }

    fun addListener(listener: PrefCallback) {
        listeners.add(listener)
    }

    fun removeListener(listener: PrefCallback) {
        listeners.remove(listener)
    }

    fun setDownloadValue(value: Boolean) {
        handler.post {

            pref.edit().run {
                putBoolean(PREF_DOWNLOAD_VALUE, value)
                commit()
            }
        }
//        pref.edit {
//            putBoolean(PREF_DOWNLOAD_VALUE, value)
//        }
    }

    fun getDownloadValue() = pref.getBoolean(PREF_DOWNLOAD_VALUE, true)

    fun getSortValue() = pref.getString(PREF_SORT_VALUE, Sort.DEFAULT.name)

    fun setSortValue(value: Sort) {
        pref.edit {
            putString(PREF_SORT_VALUE, value.name)
        }
    }

    /**
     * Writes mapList into the preference, with each key being the
     * preference key and the list will be converted to a string with a
     * delimiter of [LIST_DELIMITER]
     */
    fun writeMapList(mapList: HashMap<String, out Collection<Long>>) {
        pref.edit {
            mapList.keys.forEach {
                putString(it, mapList[it]!!.joinToString(LIST_DELIMITER))
            }
        }
    }

    fun getStringPreferenceValue(key: String): String {
        return pref.getString(key, "")!!
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        listeners.forEach {
            if (key != null) it.invoke(key)
        }
    }

    fun getDownloadLimit(): Int {
        return settingsPref.getInt(PREF_DOWNLOAD_LIMIT, 3)
    }

    companion object {
        val PREF_NAME = "user_values_pref"
        val PREF_SETTINGS_NAME = "user_settings"

        val PREF_DOWNLOAD_VALUE = "downloading value"
        val PREF_SORT_VALUE = "sort value"
        val PREF_DOWNLOAD_LIMIT = "download limit"

        const val LIST_DELIMITER = ">"
    }
}