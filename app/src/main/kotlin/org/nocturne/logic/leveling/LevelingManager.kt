package org.nocturne.logic.leveling

import org.nocturne.database.DataBaseManager.USER_PROFILE
import kotlin.math.exp
import kotlin.math.log

object LevelingManager {
    val LEVEL_CONSTANT = 100
    val LEVEL_COEFFICENT = 50
    val LEVEL_OFFSET = 50


    fun checkLevel(userID: Long): Boolean {
        val user = USER_PROFILE.selectUserByUserId(userID).executeAsOneOrNull() ?: return false
        val selectedUserEXP = user.experience
        val nextLevelReq = user.current_level.let { nextLevelReq(it) }
        if (selectedUserEXP < nextLevelReq) return false
        return true
    }

    fun nextLevelReq(level: Long): Double{
        var reqEXP = 0.0
        reqEXP = (LEVEL_COEFFICENT * (level + ((55* log(level.toDouble(), 2.11)) /(exp(-1*(level.toDouble()-LEVEL_OFFSET))+1 )) )) + LEVEL_CONSTANT
        return reqEXP
    }
}