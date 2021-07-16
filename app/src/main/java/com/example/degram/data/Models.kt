package com.example.degram.data

import android.app.usage.UsageStats

data class Arena(
        val hostUid: String = "",
        val code: String = "",
        val members: List<Member> = mutableListOf(),
        val membersUid: List<String> = mutableListOf()
)

data class Member(
        val uid: String = "",
        val name: String = "",
        val imgUrl: String = "",
        val degramScore: Int = -1
)

data class Stats(
        val dailyAverage: Long,
        val streaksCount: Long,
        val currentUsage: Long,
        val degramScore: Int,
        val stats : List<UsageStats>
)

data class ArenaMetaData(
        val hostUid: String = "",
        val code: String = "",
        val membersUid: List<String> = mutableListOf()
)