package com.example.degram.data

import android.util.Log
import com.example.degram.database.DegramDb
import com.example.degram.util.getRandomCode
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
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
                                Member(
                                        getSignedInUser()!!.uid,
                                        getSignedInUser()!!.displayName ?: "Anonymous",
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

    suspend fun joinExistingArena(code: String): Boolean = suspendCoroutine {

        val member = Member(getSignedInUser()!!.uid, getSignedInUser()!!.displayName
                ?: "Anonymous", getSignedInUser()!!.photoUrl.toString(), getDegramScore())

        Firebase.firestore.collection("arenas").document(code)
                .update("members", FieldValue.arrayUnion(member))
                .addOnSuccessListener { _ -> it.resume(true) }
                .addOnFailureListener { e ->
                    it.resume(false)
                    Log.e(TAG, "Exception : $e")
                }
    }

    private fun getSignedInUser() = auth.currentUser

    //TODO
    private fun getDegramScore() = -1

    companion object {
        private const val TAG = "DegramRepository"
    }

}