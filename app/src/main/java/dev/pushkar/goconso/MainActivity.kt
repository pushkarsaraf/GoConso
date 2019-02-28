package dev.pushkar.goconso

import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.games.AchievementsClient
import com.google.android.gms.games.EventsClient
import com.google.android.gms.games.Games
import com.google.android.gms.games.LeaderboardsClient
import dev.pushkar.goconso.ui.main.GameOver
import dev.pushkar.goconso.ui.main.GamePlay
import dev.pushkar.goconso.ui.main.MainFragment
import kotlinx.android.synthetic.main.main_activity.*
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import android.content.Intent
import android.view.Menu
import android.view.MenuItem
import android.widget.Toolbar
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PlayGamesAuthProvider
import kotlinx.android.synthetic.main.main_fragment.*

class MainActivity : AppCompatActivity(),
    MainFragment.OnFragmentInteractionListener,
    GameOver.OnFragmentInteractionListener,
    GamePlay.OnFragmentInteractionListener {

    override fun getAchievements() {
        checkForAchievements(score)
        updateLeaderboards(score)
        pushAccomplishments()
    }

    override fun onFragmentInteraction(uri: Uri) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private var mAchievementsClient: AchievementsClient? = null
    private var mLeaderboardsClient: LeaderboardsClient? = null
    private var mEventsClient: EventsClient? = null
    private val mOutbox = AccomplishmentsOutbox()


    private lateinit var bgAudioPlayer: MediaPlayer
    private lateinit var tilePressPlayer: MediaPlayer
    private lateinit var mGoogleSignInClient: GoogleSignInClient

    override fun startMediaAndLoop() {
        bgAudioPlayer = MediaPlayer.create(
            this,
            R.raw.audio_tap_tiles
        )
        bgAudioPlayer.isLooping = true
        tilePressPlayer = MediaPlayer.create(
            this,
            R.raw.tap_tile_press
        )
    }

    override fun pauseBg() {
        bgAudioPlayer.pause()
    }

    override fun startBg() {
        bgAudioPlayer.start()
    }

    override fun stopMedia() {
        if (bgAudioPlayer.isPlaying)
            bgAudioPlayer.pause()
    }

    override fun playWav() {
        tilePressPlayer.start()
    }

    override fun resumeBg() {
        bgAudioPlayer.seekTo(bgAudioPlayer.currentPosition)
        bgAudioPlayer.start()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        FirebaseApp.initializeApp(this)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, MainFragment.newInstance())
                .commitNow()
        }
        init()
        Toast.makeText(this, "V1", Toast.LENGTH_LONG).show()
    }

    private fun init() {
        mGoogleSignInClient = GoogleSignIn.getClient(
            this,
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN).build()
        )
        if (!isSignedIn())
            signInSilently()
        GoogleApiClient.Builder(this).setViewForPopups(container)
    }

    override fun showAchievements() {
        Games.getAchievementsClient(this, GoogleSignIn.getLastSignedInAccount(this)!!)
            .achievementsIntent
            .addOnSuccessListener { intent -> startActivityForResult(intent, RC_ACHIEVEMENT_UI) }
    }

    override fun showLeaderboard() {
        Games.getLeaderboardsClient(this, GoogleSignIn.getLastSignedInAccount(this)!!)
            .getLeaderboardIntent(getString(R.string.leaderboard_consoboard))
            .addOnSuccessListener { intent -> startActivityForResult(intent, RC_LEADERBOARD_UI) }
    }

    override fun showScoreboard(){
        startActivity(Intent(this,Scoreboard::class.java))
    }

    private fun isSignedIn(): Boolean {
        return GoogleSignIn.getLastSignedInAccount(this) != null
    }

    private fun signInSilently() {
        Log.d(TAG, "signInSilently()")

        mGoogleSignInClient.silentSignIn().addOnCompleteListener(
            this
        ) { task ->
            if (task.isSuccessful) {
                Log.d(TAG, "signInSilently(): success")
                onConnected(task.result)
            } else {
                Log.d(TAG, "signInSilently(): failure", task.exception)
                startSignInIntent()
            }
        }
    }

    private fun startSignInIntent() {
        startActivityForResult(mGoogleSignInClient.signInIntent, RC_SIGN_IN)
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume()")
        signInSilently()
    }

    override fun signOut() {
        Log.d(TAG, "signOut()")

        if (!isSignedIn()) {
            Log.w(TAG, "signOut() called, but was not signed in!")
            return
        }

        mGoogleSignInClient.signOut().addOnCompleteListener(
            this
        ) { task ->
            val successful = task.isSuccessful
            Log.d(TAG, "signOut(): " + (if (successful) "success" else "failed"))

            onDisconnected()
        }
    }

    private fun isPrime(n: Int): Boolean {
        var i = 2
        if (n == 0 || n == 1) {
            return false
        }
        while (i <= n / 2) {
            if (n % i == 0) {
                return false
            }
            i++
        }
        return true
    }

    private fun checkForAchievements(finalScore: Int) {
        // Check if each condition is met; if so, unlock the corresponding
        // achievement.
        if (isPrime(finalScore)) {
            mOutbox.mPrimeAchievement = true
            achievementToast(getString(R.string.achievement_prime_toast_text))
        }
        if (finalScore == 99) {
            mOutbox.mArrogantAchievement = true
            achievementToast(getString(R.string.achievement_arrogant_toast_text))
        }
        if (finalScore == 0) {
            mOutbox.mHumbleAchievement = true
            achievementToast(getString(R.string.achievement_humble_toast_text))
        }
        if (finalScore == 137) {
            mOutbox.mLeetAchievement = true
            achievementToast(getString(R.string.achievement_leet_toast_text))
        }
        if (finalScore == 2){
            mOutbox.multiplayerAchievement = true
            achievementToast(getString(R.string.unlocked))
        }
        mOutbox.mBoredSteps++
    }

    private fun achievementToast(achievement: String) {
        // Only show toast if not signed in. If signed in, the standard Google Play
        // toasts will appear, so we don't need to show our own.
        if (!isSignedIn()) {
            Toast.makeText(
                this, getString(R.string.achievement) + ": " + achievement,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun pushAccomplishments() {
        if (!isSignedIn()) {
            return
        }
        if (mOutbox.multiplayerAchievement){
            mAchievementsClient!!.unlock(getString(R.string.achievement_dont_play_alone))
            mOutbox.multiplayerAchievement = false
            Data.localData.edit().putBoolean("multiplayer",true).apply()
        }
        if (mOutbox.mPrimeAchievement) {
            mAchievementsClient!!.unlock(getString(R.string.achievement_prime))
            mOutbox.mPrimeAchievement = false
        }
        if (mOutbox.mArrogantAchievement) {
            mAchievementsClient!!.unlock(getString(R.string.achievement_arrogant))
            mOutbox.mArrogantAchievement = false
        }
        if (mOutbox.mHumbleAchievement) {
            mAchievementsClient!!.unlock(getString(R.string.achievement_humble))
            mOutbox.mHumbleAchievement = false
        }
        if (mOutbox.mLeetAchievement) {
            mAchievementsClient!!.unlock(getString(R.string.achievement_leet))
            mOutbox.mLeetAchievement = false
        }
        Toast.makeText(this,"${mOutbox.mBoredSteps}",Toast.LENGTH_LONG).show()
        if (mOutbox.mBoredSteps > 0) {
            mAchievementsClient!!.increment(
                getString(R.string.achievement_really_bored),
                1
            )
            mAchievementsClient!!.increment(
                getString(R.string.achievement_bored),
                1
            )
            mOutbox.mBoredSteps = 0
        }
        if (mOutbox.mHardModeScore >= 0) {
            mLeaderboardsClient!!.submitScore(
                getString(R.string.leaderboard_consoboard),
                mOutbox.mHardModeScore
            )
            mOutbox.mHardModeScore = -1
        }
    }

    private fun updateLeaderboards(finalScore: Int) {
        if (mOutbox.mHardModeScore < finalScore) {
            mOutbox.mHardModeScore = finalScore.toLong()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(intent)
            try {
                val account = task.getResult(ApiException::class.java)
                onConnected(account)
            } catch (apiException: ApiException) {
                var message: String? =
                    apiException.message + "\n" + apiException.statusCode + "\n" + apiException.stackTrace.toString()
                if (message == null || message.isEmpty()) {
                    message = getString(R.string.signin_other_error)
                }

                onDisconnected()

                AlertDialog.Builder(this)
                    .setMessage(message)
                    .setNeutralButton(android.R.string.ok, null)
                    .show()
            }
        }
    }

    private fun onConnected(googleSignInAccount: GoogleSignInAccount?) {
        Log.d(TAG, "onConnected(): connected to Google APIs")
        if(googleSignInAccount!=null) {
            mAchievementsClient = Games.getAchievementsClient(this, googleSignInAccount)
            mLeaderboardsClient = Games.getLeaderboardsClient(this, googleSignInAccount)
            mEventsClient = Games.getEventsClient(this, googleSignInAccount)
        }
        if (!mOutbox.isEmpty) {
            pushAccomplishments()
            Toast.makeText(
                this,
                getString(R.string.your_progress_will_be_uploaded),
                Toast.LENGTH_LONG
            ).show()
        }
        loadAndPrintEvents()
    }

    private fun onDisconnected() {
        Log.d(TAG, "onDisconnected()")
        Toast.makeText(this, "Signed Out", Toast.LENGTH_SHORT).show()
        mAchievementsClient = null
        mLeaderboardsClient = null
    }

    private fun loadAndPrintEvents() {
        mEventsClient!!.load(true)
            .addOnSuccessListener { eventBufferAnnotatedData ->
                val eventBuffer = eventBufferAnnotatedData.get()

                var count = 0
                if (eventBuffer != null) {
                    count = eventBuffer.count
                }

                Log.i(TAG, "number of events: $count")

                for (i in 0 until count) {
                    val event = eventBuffer!!.get(i)
                    Log.i(
                        TAG, "event: "
                                + event.name
                                + " -> "
                                + event.value
                    )
                }
            }
            .addOnFailureListener { e -> handleException(e, getString(R.string.achievements_exception)) }
    }

    private fun handleException(e: Exception, details: String) {
        var status = 0

        if (e is ApiException) {
            status = e.statusCode
        }

        val message = getString(R.string.status_exception_error, details, status, e)

        AlertDialog.Builder(this@MainActivity)
            .setMessage(message)
            .setNeutralButton(android.R.string.ok, null)
            .show()
    }

    private inner class AccomplishmentsOutbox {
        internal var mPrimeAchievement = false
        internal var mHumbleAchievement = false
        internal var mLeetAchievement = false
        internal var mArrogantAchievement = false
        internal var multiplayerAchievement = false
        internal var mBoredSteps = 0
        internal var mHardModeScore: Long = -1

        internal val isEmpty: Boolean
            get() = !mPrimeAchievement && !mHumbleAchievement && !mLeetAchievement &&
                    !mArrogantAchievement && mBoredSteps == 0 &&
                    mHardModeScore < 0

    }


    companion object {
        private const val RC_SIGN_IN = 9001
        const val TAG = "SIGNIN?"
        private const val RC_ACHIEVEMENT_UI = 9003
        private const val RC_LEADERBOARD_UI = 9004
        var confetti: Boolean = false
        var score: Int = 0
    }
}

