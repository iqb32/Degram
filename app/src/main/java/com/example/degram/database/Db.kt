package com.example.degram.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [UserInfo::class], version = 1, exportSchema = false)
abstract class DegramDb : RoomDatabase() {
    abstract fun userInfoDao()  : UserInfoDao
}