package dev.pushkar.goconso

import android.graphics.Typeface
import android.content.SharedPreferences
import android.widget.ImageView
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import java.util.*

object Data {
    lateinit var USER: GoogleSignInAccount
    const val LEVELS_PER_BUFFER = 2
    const val MAX_BUFFER = 3
    const val STEPS_PER_LEVEL = 30
    const val TOTOL_LEVELS = LEVELS_PER_BUFFER * MAX_BUFFER
    const val TIMEOUT_INCREMENT = 500
    const val TIMEOUT_MIN = 3000
    const val TIMEOUT_MAX = 10000
    const val LEADERBOARD_LENGTH = 10
    const val COLOR_RED = "RED"
    const val COLOR_YELLOW = "YELLOW"
    const val COLOR_BLUE = "BLUE"
    const val COLOR_GREEN = "GREEN"
    const val COLOR_BROWN = "BROWN"
    const val COLOR_MAGENTA = "MAGENTA"
    const val COLOR_INDIGO = "INDIGO"
    const val COLOR_GRAY = "GRAY"
    val COLORS = arrayOf(
        COLOR_RED,
        COLOR_BLUE,
        COLOR_GREEN,
        COLOR_YELLOW,
        COLOR_MAGENTA,
        COLOR_INDIGO,
        COLOR_BROWN,
        COLOR_GRAY
    )
    const val N_COLORS = 8
    var consoLogos = arrayOfNulls<ImageView>(N_COLORS)
    var colorImages = IntArray(N_COLORS)//to be initialized
    var calender: Calendar = Calendar.getInstance()
    lateinit var localData: SharedPreferences
    const val applicationPreference = "appPref"
    const val KEY_NEW_INSTALL = "new_install"
    const val KEY_TT_BEST_SCORE = "bs1"
    const val KEY_TT_DAILY_BEST_SCORE = "dbs1${Calendar.DATE}"
    const val KEY_TODAY = "day:${Calendar.DATE}"
    const val KEY_DAILY_SIZE = "daily_size"
    const val KEY_ALL_TIME_SIZE = "all_time_size"
    var KEY_USERS_ALL_TIME = arrayOfNulls<String>(LEADERBOARD_LENGTH)
    var KEY_USERS_DAILY = arrayOfNulls<String>(LEADERBOARD_LENGTH)
    var KEY_SCORES_ALL_TIME = arrayOfNulls<String>(LEADERBOARD_LENGTH)
    var KEY_SCORES_DAILY = arrayOfNulls<String>(LEADERBOARD_LENGTH)
    var appTypeface: Typeface = Typeface.DEFAULT
    init {
        for (i in 0 until LEADERBOARD_LENGTH) {
            KEY_USERS_ALL_TIME[i] = "all_time_users_" + (i + 1)
            KEY_SCORES_ALL_TIME[i] = "all_time_scores_" + (i + 1)
            KEY_USERS_DAILY[i] = "daily_users_" + (i + 1)
            KEY_SCORES_DAILY[i] = "daily_scores_" + (i + 1)
        }
    }
}
