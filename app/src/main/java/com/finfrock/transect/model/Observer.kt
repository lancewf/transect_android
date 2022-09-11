package com.finfrock.transect.model

data class Observer(
    val id: String,
    val name: String
) {
    override fun toString(): String = name
    companion object Factory {
        val NullObserver = Observer("", "")
    }
}
