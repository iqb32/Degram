package com.example.degram.data

import com.example.degram.database.DegramDb
import javax.inject.Inject

class DegramRepository @Inject constructor(
    private val database : DegramDb
) {

}