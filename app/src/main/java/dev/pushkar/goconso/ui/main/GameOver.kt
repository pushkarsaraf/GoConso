package dev.pushkar.goconso.ui.main

import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.games.Games
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import dev.pushkar.goconso.Data
import dev.pushkar.goconso.Data.KEY_TT_BEST_SCORE
import dev.pushkar.goconso.Data.KEY_TT_DAILY_BEST_SCORE
import dev.pushkar.goconso.Data.localData
import dev.pushkar.goconso.HighScore
import dev.pushkar.goconso.MainActivity
import dev.pushkar.goconso.R
import kotlinx.android.synthetic.main.fragment_game_over.*
import kotlinx.android.synthetic.main.info_sheet.*
import nl.dionsegijn.konfetti.KonfettiView
import nl.dionsegijn.konfetti.models.Shape
import nl.dionsegijn.konfetti.models.Size

class GameOver : Fragment() {
    private var listener: OnFragmentInteractionListener? = null
    private var bestScore: Int = 0
    private var dailyBestScore: Int = 0
    private lateinit var viewKonfetti: KonfettiView
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_game_over, container, false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnFragmentInteractionListener")
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        db = FirebaseFirestore.getInstance()
        init()
    }

    private fun init() {
        if (GoogleSignIn.getLastSignedInAccount(activity!!) != null) {
            Games.getGamesClient(activity!!, GoogleSignIn.getLastSignedInAccount(activity!!)!!).setViewForPopups(view!!)
        } else {
            activity!!.finish()
        }
        bestScore = localData.getInt(KEY_TT_BEST_SCORE, 0)
        dailyBestScore = localData.getInt(KEY_TT_DAILY_BEST_SCORE, 0)
        try {
            val highScore1: HighScore =
                if ("${Data.localData.getString(
                        getString(R.string.info_sheet_1), ""
                    )}${Data.localData.getString(getString(R.string.info_sheet_2), "")}".isBlank()
                ) {
                    HighScore(dailyBestScore, GoogleSignIn.getLastSignedInAccount(activity!!)!!.displayName!!)
                } else {
                    HighScore(
                        dailyBestScore, "${Data.localData.getString(
                            getString(R.string.info_sheet_1),
                            ""
                        )}${Data.localData.getString(getString(R.string.info_sheet_2), "")}"
                    )
                }
            db.collection("Daily").document(GoogleSignIn.getLastSignedInAccount(activity!!)!!.id!!).set(highScore1)
            val highScore2: HighScore =
                if ("${Data.localData.getString(
                        getString(R.string.info_sheet_1), ""
                    )}${Data.localData.getString(getString(R.string.info_sheet_2), "")}".isBlank()
                ) {
                    HighScore(bestScore, GoogleSignIn.getLastSignedInAccount(activity!!)!!.displayName!!)
                } else {
                    HighScore(
                        bestScore, "${Data.localData.getString(
                            getString(R.string.info_sheet_1),
                            ""
                        )}${Data.localData.getString(getString(R.string.info_sheet_2), "")}"
                    )
                }
            db.collection("AllTime").document(GoogleSignIn.getLastSignedInAccount(activity!!)!!.id!!).set(highScore2)
        } catch (ex: Exception) {
            Snackbar.make(view!!, "Please fill your name  in profile menu  to appear on scoreboards", Snackbar.LENGTH_LONG).show()
        }
        this.view!!.isFocusableInTouchMode = true
        this.view!!.requestFocus()
        this.view!!.setOnKeyListener { _, i, _ ->
            if (i == KeyEvent.KEYCODE_BACK) {
                fragmentManager!!.beginTransaction()
                    .replace(id, GamePlay(), "GP")
                    .commit()
                return@setOnKeyListener true
            }
            return@setOnKeyListener false
        }
        view!!.setBackgroundColor(Color.TRANSPARENT)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            try {
                activity!!.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            } catch (ex: Exception) {
                Toast.makeText(activity!!, "Incompatible device", Toast.LENGTH_LONG).show()
            }
        }
        listener!!.getAchievements()
        viewKonfetti = view!!.findViewById(R.id.viewKonfetti)
        if (MainActivity.confetti) {
            Toast.makeText(activity!!, "New High Score!", Toast.LENGTH_LONG).show()
            viewKonfetti.build()
                .addColors(Color.YELLOW, Color.RED, Color.CYAN)
                .setDirection(0.0, 359.0)
                .setSpeed(1f, 5f)
                .setFadeOutEnabled(true)
                .setTimeToLive(2000L)
                .addShapes(Shape.RECT, Shape.CIRCLE)
                .addSizes(Size(12, 5f))
                .setPosition(-50f, viewKonfetti.width + 50f, -50f, -50f)
                .streamFor(300, 5000L)
        }

        tv_details.text = getString(R.string.scorex, MainActivity.score, dailyBestScore, bestScore)
        btn_lb.setOnClickListener {
            listener!!.showLeaderboard()
        }
        btn_return.setOnClickListener {
            fragmentManager!!.beginTransaction().replace(id, MainFragment(), "MF").commit()
        }
        btn_replay.setOnClickListener {
            fragmentManager!!.beginTransaction().replace(id, GamePlay(), "GP").commit()
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onResume() {
        super.onResume()
        (activity as AppCompatActivity).supportActionBar!!.hide()
    }

    interface OnFragmentInteractionListener {
        fun onFragmentInteraction(uri: Uri)
        fun getAchievements()
        fun showLeaderboard()
    }
}
