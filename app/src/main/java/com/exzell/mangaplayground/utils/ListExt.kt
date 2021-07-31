package com.exzell.mangaplayground.utils

import timber.log.Timber
import java.util.*

fun <T> MutableList<T>.removeDuplicates(): MutableList<T> {
    Timber.d("Duplicated List is $this")

    val dupList = LinkedHashSet(this)
    clear()
    addAll(dupList)

    Timber.i("New List is $this")
    return this
}

fun <T> String.toList(delimiter: String, mapTo: ((String) -> T)? = null): MutableList<T> {
    if (isEmpty()) return mutableListOf()


    return with(split(delimiter.toRegex())) {

        if (mapTo != null) map { mapTo.invoke(it) }
        else this
    }.toMutableList() as MutableList<T>
}