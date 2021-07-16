package com.example.degram.data

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.util.Log
import com.example.degram.util.Constants
import com.example.degram.util.getRandomCode
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class DegramRepository @Inject constructor(
        private val  context: Context
) {
    private val auth = FirebaseAuth.getInstance()

    suspend fun getStats(): Stats? =
        withContext(Dispatchers.IO) {

            val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

            //For calculating streaks and average , we get the past 10 days data
            //Streaks can go only till 10, after which they remain constant
            //Average is also computed based on last 10 days of data
            val calendarStreaks = Calendar.getInstance()
            calendarStreaks.add(Calendar.DAY_OF_MONTH, -10)
            val startTimeStreaks = calendarStreaks.timeInMillis

            //Get the stats , and filter the data for only instagram app
            val stats =
                    usageStatsManager.queryUsageStats(
                            UsageStatsManager.INTERVAL_DAILY,
                            startTimeStreaks,
                            System.currentTimeMillis()
                    ).filter { stats -> stats.packageName == Constants.INSTAGRAM_PACKAGE_NAME }


            Log.e(TAG, "Average : ${(stats.sumOf { it.totalTimeInForeground }) / stats.size}, Current : ${stats.last().totalTimeInForeground}")

            return@withContext if (stats.isNotEmpty()) {
                val dailyAverage = (stats.sumOf { it.totalTimeInForeground }) / stats.size
                val currentUsage = stats.last().totalTimeInForeground
                val streakCount = calculateStreakCount(stats)

                Stats(dailyAverage = dailyAverage,
                        streaksCount = streakCount,
                        currentUsage = currentUsage,
                        degramScore = calculateDegramScore(dailyAverage >= currentUsage, currentUsage <= Constants.GLOBAL_USAGE_AVERAGE, streakCount.toInt()),
                        stats = stats
                )

            } else {
                null
            }
        }


    private fun calculateDegramScore(isLessThanDailyAverage: Boolean, isLessThanGlobalAverage: Boolean, streakCount: Int): Int {
        Log.e(TAG, "${isLessThanDailyAverage}, ${isLessThanGlobalAverage}, $streakCount")
        var score = 0
        if (isLessThanDailyAverage) score += 30
        if (isLessThanGlobalAverage) score += 30
        score += streakCount * 4
        return score
    }

    private fun calculateStreakCount(streakStats: List<UsageStats>): Long {
        var index = 9
        var continueToCheck = true
        var count: Long = 0

        while (continueToCheck) {
            if (index > 0) {
                if (streakStats[index].totalTimeInForeground <= streakStats[index - 1].totalTimeInForeground) {
                    count++
                    index--
                } else continueToCheck = false
            } else break

        }

        return count
    }

    suspend fun updateScore() : Boolean =
        suspendCoroutine {
            val
        }

    suspend fun createNewArena(): Boolean =
            suspendCoroutine {
                val arena = ArenaMetaData(
                        hostUid = getSignedInUser()!!.uid,
                        code = getRandomCode(),
                        members = mutableListOf(
                                Member(
                                        getSignedInUser()!!.uid,
                                        getSignedInUser()!!.displayName ?: "Anonymous",
                                        getSignedInUser()!!.photoUrl.toString(),
                                        getDegramScore()
                                )
                        ),
                        membersUid = mutableListOf(getSignedInUser()!!.uid)
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
                .update("members", FieldValue.arrayUnion(member), "membersUid", FieldValue.arrayUnion(getSignedInUser()!!.uid))
                .addOnSuccessListener { _ -> it.resume(true) }
                .addOnFailureListener { e ->
                    it.resume(false)
                    Log.e(TAG, "Exception : $e")
                }
    }

    @ExperimentalCoroutinesApi
    suspend fun getArenas(): Flow<List<Arena>?> = callbackFlow {
        val subscription = Firebase.firestore.collection("arenas")
                .whereArrayContains("membersUid", getSignedInUser()!!.uid)
                .addSnapshotListener { value, error ->
                    if (error != null) {
                        Log.e(TAG, "Exception :  $error")
                        trySend(null).isFailure
                    } else {
                        val arenas = mutableListOf<Arena>()
                        value?.documents?.forEach { it.toObject(Arena::class.java)?.let { it1 -> arenas.add(it1) } }
                        value?.documentChanges?.forEach { documentChange ->
                            if (documentChange.type == DocumentChange.Type.REMOVED) {
                                arenas.remove(documentChange.document.toObject(Arena::class.java))
                            }
                        }
                        Log.d(TAG, "$arenas")
                        trySend(arenas).isSuccess
                    }
                }
        awaitClose { subscription.remove() }
    }

    suspend fun leaveArena(code: String): Boolean =
            suspendCoroutine {
                Firebase.firestore.collection("arenas").document(code)
                        .update("membersUid", getSignedInUser()!!.uid)
                        .addOnSuccessListener { _ -> it.resume(true) }
                        .addOnFailureListener { e ->
                            it.resume(false)
                            Log.e(TAG, "Exception : $e")
                        }
            }

    suspend fun deleteArena(code: String): Boolean =
            suspendCoroutine {
                Firebase.firestore.collection("arenas").document(code).delete()
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