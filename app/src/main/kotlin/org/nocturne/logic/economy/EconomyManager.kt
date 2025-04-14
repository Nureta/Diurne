package org.nocturne.logic.economy

import org.nocturne.UserProfile
import org.nocturne.database.DataBaseManager.USER_PROFILE
import kotlin.math.roundToLong

object EconomyManager {
    fun giveLunaris(userID: Long, lunarisGain: Long, isBoosting: Boolean = false) {
        val user = USER_PROFILE.selectUserByUserId(userID).executeAsOneOrNull()
        var lunarisFinal = lunarisGain
        if (isBoosting) {
            lunarisFinal = (lunarisFinal.toDouble() * 1.5).roundToLong()
        }
        lunarisFinal += user!!.lunaris
        USER_PROFILE.updateLunaris(lunarisFinal,userID)
    }
}