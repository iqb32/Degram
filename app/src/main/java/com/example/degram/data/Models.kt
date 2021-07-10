package com.example.degram.data

data class Arena(
    val hostUid : String = "",
    val code : String = "",
    val members : List<Member> = mutableListOf()
)

data class Member(
    val uid : String = "",
    val name : String = "",
    val imgUrl  : String = "",
    val degramScore : Int = -1
)