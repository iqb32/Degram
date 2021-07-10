package com.example.degram.data

import android.util.Log
import com.example.degram.database.DegramDb
import com.example.degram.util.getRandomCode
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class DegramRepository @Inject constructor(
    private val database: DegramDb
) {

    private val auth = FirebaseAuth.getInstance()

    suspend fun createNewArena(): Boolean =
        suspendCoroutine {
            val arena = Arena(
                hostUid = getSignedInUser()!!.uid,
                code = getRandomCode(),
                members = mutableListOf(
                    ArenaParticipants(
                        getSignedInUser()!!.uid,
                        getSignedInUser()!!.displayName ?: "No Name",
                        getSignedInUser()!!.photoUrl.toString(),
                        getDegramScore()
                    )
                )
            )
            Firebase.firestore.collection("arenas").document(arena.code)
                .set(arena)
                .addOnSuccessListener { _ ->
                    it.resume(true)
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Exception = $e")
                    it.resume(false)
                }
        }

    private fun getSignedInUser() = auth.currentUser

    //TODO
    private fun getDegramScore() = -1

    companion object {
        private const val TAG = "DegramRepository"
    }

}