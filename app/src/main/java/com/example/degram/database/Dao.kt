package com.example.degram.database

import androidx.room.*

@Dao
interface UserInfoDao {
    @Query("SELECT * FROM user_info LIMIT 1")
    fun getUserInfo(): UserInfo

    //This should be called when the user logs in for the first time.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertDefaultData(userInfo: UserInfo) : Long

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateUserInfo(userInfo: UserInfo)
}
