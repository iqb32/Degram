package com.example.degram.data

data class Arena(
    val hostUid : String = "",
    val code : String = "",
    val members : List<ArenaParticipants> = mutableListOf()
)

data class ArenaParticipants(
    val uid : String = "",
    val name : String = "",
    val imgUrl  : String = "",
    val degramScore : Int = -1
)