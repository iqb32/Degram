package com.example.degram.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_info")
data class UserInfo constructor(
    @PrimaryKey
    val id: Int = 0,
    val score : Int = -1
)
