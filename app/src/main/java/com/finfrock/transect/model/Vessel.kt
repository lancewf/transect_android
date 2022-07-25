package com.finfrock.transect.model

data class Vessel(
    val name: String,
    val id: String,
){
    override fun toString(): String = name
}
