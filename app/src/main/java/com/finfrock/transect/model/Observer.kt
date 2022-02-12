package com.finfrock.transect.model

data class Observer(
    val id: Int,
    val name: String
) {
    override fun toString(): String = name
}
