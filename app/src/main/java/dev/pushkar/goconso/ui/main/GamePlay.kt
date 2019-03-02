package dev.pushkar.goconso.ui.main

import android.content.Context
import android.content.pm.ActivityInfo
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.games.Games
import dev.pushkar.goconso.Data
import dev.pushkar.goconso.MainActivity
import dev.pushkar.goconso.R
import kotlinx.android.synthetic.main.fragment_game_play.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.indices

class GamePlay : Fragment() {

    private lateinit var scoreView: TextView
    private lateinit var tapTiles: Array<ImageView?>
    private lateinit var fillTiles: Array<ImageView?>
    private lateinit var tapTilesImageIDs: IntArray
    private lateinit var fillTilesImageIDs: IntArray
    private var emptyTileImageID: Int = 0
    private lateinit var unfilledTiles: ArrayList<Int>
    private lateinit var unusedColors: ArrayList<Int>
    private lateinit var colorQueue: LinkedList<Int>
    private lateinit var coloredTiles: LinkedList<Int>
    private lateinit var randomGen: Random
    private var gameOver: Boolean = false
    private var gameOverHandled: Boolean = false
    private var forceQuit: Boolean = false
    private var delay: Long = 0
    private var score: Int = 0
    private var disappearedTileIndex = 0
    private lateinit var fillTilesThread: ControlThread
    private lateinit var tapListener: View.OnClickListener
    private lateinit var appear: Animation
    private lateinit var disappear: Animation
    private var confetti = false
    private var listener: OnFragmentInteractionListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_game_play, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initialize()
        startGame()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            try {
                activity!!.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            } catch (ex: Exception) {
                Toast.makeText(activity, "Incompatible device", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun initialize() {
        if(MainActivity.isFullVersion){
            if(GoogleSignIn.getLastSignedInAccount(activity!!)!=null) {
                Games.getGamesClient(activity!!, GoogleSignIn.getLastSignedInAccount(activity!!)!!).setViewForPopups(view!!)
            } else {
                activity!!.finish()
            }
        }
        this.view!!.isFocusableInTouchMode= true
        this.view!!.requestFocus()
        this.view!!.setOnKeyListener { _, i, _ ->
            if(i == KeyEvent.KEYCODE_BACK) {
                fragmentManager!!.beginTransaction()
                    .replace(id, MainFragment(),"GP")
                    .commit()
                Toast.makeText(activity!!,"back",Toast.LENGTH_LONG).show()
                return@setOnKeyListener true
            }
            return@setOnKeyListener false
        }
        this.view!!.isFocusableInTouchMode= true
        this.view!!.requestFocus()
        this.view!!.setOnKeyListener { _, i, _ ->
            if(i == KeyEvent.KEYCODE_BACK) {
                fragmentManager!!.beginTransaction()
                    .replace(id,GamePlay(),"GP")
                    .commit()
                return@setOnKeyListener true
            }
            return@setOnKeyListener false
        }
        scoreView = tap_tile_textview
        tapTiles = arrayOfNulls(Data.N_COLORS)
        tapTiles[0] = tap_tile_0
        tapTiles[1] = tap_tile_1
        tapTiles[2] = tap_tile_2
        tapTiles[3] = tap_tile_3
        tapTiles[4] = tap_tile_4
        tapTiles[5] = tap_tile_5
        tapTiles[6] = tap_tile_6
        tapTiles[7] = tap_tile_7

        for (i in 0 until Data.N_COLORS) {
            tapTiles[i]!!.tag = i
        }

        fillTiles = arrayOfNulls(Data.N_COLORS)
        fillTiles[0] = fill_tile_0
        fillTiles[1] = fill_tile_1
        fillTiles[2] = fill_tile_2
        fillTiles[3] = fill_tile_3
        fillTiles[4] = fill_tile_4
        fillTiles[5] = fill_tile_5
        fillTiles[6] = fill_tile_6
        fillTiles[7] = fill_tile_7

        tapTilesImageIDs = IntArray(Data.N_COLORS)
        tapTilesImageIDs[0] = R.drawable.ic_tap_tile_red
        tapTilesImageIDs[1] = R.drawable.ic_tap_tile_green
        tapTilesImageIDs[2] = R.drawable.ic_tap_tile_blue
        tapTilesImageIDs[3] = R.drawable.ic_tap_tile_yellow
        tapTilesImageIDs[4] = R.drawable.ic_tap_tile_magenta
        tapTilesImageIDs[5] = R.drawable.ic_tap_tile_indigo
        tapTilesImageIDs[6] = R.drawable.ic_tap_tile_brown
        tapTilesImageIDs[7] = R.drawable.ic_tap_tile_gray

        fillTilesImageIDs = IntArray(Data.N_COLORS)
        fillTilesImageIDs[0] = R.drawable.ic_fill_tile_red
        fillTilesImageIDs[1] = R.drawable.ic_fill_tile_green
        fillTilesImageIDs[2] = R.drawable.ic_fill_tile_blue
        fillTilesImageIDs[3] = R.drawable.ic_fill_tile_yellow
        fillTilesImageIDs[4] = R.drawable.ic_fill_tile_magenta
        fillTilesImageIDs[5] = R.drawable.ic_fill_tile_indigo
        fillTilesImageIDs[6] = R.drawable.ic_fill_tile_brown
        fillTilesImageIDs[7] = R.drawable.ic_fill_tile_gray

        emptyTileImageID = R.drawable.ic_fill_tile_empty

        randomGen = Random()

        listener!!.startMediaAndLoop()


        appear = AlphaAnimation(0f, 1f)
        appear.duration = 250
        appear.interpolator = LinearInterpolator()
        disappear = AlphaAnimation(1f, 0f)
        disappear.duration = 250
        disappear.interpolator = LinearInterpolator()
    }


    private fun startGame() {
        initGameValues()
        enableTapTiles(true)
        fillTilesThread.start()
    }

    private fun handleUserTapAction(tile: ImageView) {
        listener!!.playWav()
        if (colorQueue.isEmpty() || coloredTiles.isEmpty()) {
            gameOver = true
        } else {
            val tappedIndex = tile.tag as Int
            val colorIndex = colorQueue.remove()
            val fillTileIndex = coloredTiles.remove()
            if (tappedIndex == colorIndex) {
                val fillTile = fillTiles[fillTileIndex]
                fillTile!!.setImageResource(emptyTileImageID)
                unfilledTiles.add(fillTileIndex)
                unusedColors.add(colorIndex)
                score++
                scoreView.text = getString(R.string.score,score)
                updateDelay()
                shuffleTiles()
                if (score in 101..150 && score % 4 == 1 || score > 200) {
                    tapTiles[disappearedTileIndex]!!.startAnimation(appear)
                    disappearedTileIndex = randomGen.nextInt(Data.N_COLORS)
                    tapTiles[disappearedTileIndex]!!.startAnimation(disappear)
                }
            } else {
                gameOver = true
            }
        }

        if (gameOver) {
            handleGameOverAction()
        }
    }

    private fun shuffleTiles() {
        if (score < 70)
            return
        var magicNumber = 20
        when {
            score > 120 -> magicNumber = 9
            score > 180 -> magicNumber = 5
            score > 250 -> magicNumber = 3
        }
        val num = randomGen.nextInt(magicNumber)
        if (num != 0)
            return
        val array = IntArray(Data.N_COLORS)
        for (i in 0 until Data.N_COLORS) {
            array[i] = i
        }
        val shuffledCodes = shuffle(array)
        for (i in 0 until Data.N_COLORS) {
            tapTiles[i]!!.setImageResource(tapTilesImageIDs[shuffledCodes[i]])
            tapTiles[i]!!.tag = shuffledCodes[i]
        }
    }

    private fun updateDelay() {
        if (score < 30)
            return
        if (score <= 80) {
            if (score % 5 == 0)
                delay -= 10
        } else if (score < 120) {
            if (score % 5 == 0)
                delay -= 3
        } else if (score < 200) {
            if (score % 10 == 0)
                delay -= 8
        } else if (score < 250) {
            if (score % 10 == 0)
                delay -= 5
        } else if (score < 300) {
            if (score % 10 == 0) {
                delay -= 20
            }
        }
    }

    private fun handleGameOverAction() {
        if (!gameOverHandled) {
            gameOverHandled = true
            if (fillTilesThread.isAlive) {
                fillTilesThread.interrupt()
            }
            enableTapTiles(false)
            scoreView.text = getString(R.string.score,score)
            val bestScore = Data.localData.getInt(Data.KEY_TT_BEST_SCORE, 0)
            val dailyBestScore = Data.localData.getInt(Data.KEY_TT_DAILY_BEST_SCORE, 0)
            val editor = Data.localData.edit()
            confetti = false
            if (bestScore < score) {
                editor.putInt(Data.KEY_TT_BEST_SCORE, score)
                confetti = true
            }
            when {
                dailyBestScore < score -> {
                    editor.putInt(Data.KEY_TT_DAILY_BEST_SCORE, score)
                }
                bestScore < score -> {
                    editor.putInt(Data.KEY_TT_BEST_SCORE, score)
                }
            }
            editor.apply()

            val ft = fragmentManager!!.beginTransaction()
            ft.replace(id, GameOver(), "NewFragmentTag")
            ft.commit()
            MainActivity.confetti = confetti
            MainActivity.score = score

        }
    }

    private fun initGameValues() {
        gameOver = false
        score = 0
        gameOverHandled = false
        forceQuit = false
        delay = INITIAL_DELAY.toLong()
        for (i in 0 until Data.N_COLORS) {
            fillTiles[i]!!.setImageResource(emptyTileImageID)
        }
        unfilledTiles = ArrayList(Data.N_COLORS)
        unusedColors = ArrayList(Data.N_COLORS)
        colorQueue = LinkedList()
        coloredTiles = LinkedList()

        for (i in 0 until Data.N_COLORS) {
            unfilledTiles.add(i)
            unusedColors.add(i)
        }

        tapListener = View.OnClickListener { v -> handleUserTapAction(v as ImageView) }

        fillTilesThread = object : ControlThread() {
            private val lock = java.lang.Object()
            private var pauseRequest = false

            override fun onPause() {
                pauseRequest = true
            }

            override fun onResume() {
                try {
                    Thread.sleep(300)
                } catch (e: InterruptedException) {
                }

                pauseRequest = false
                synchronized(lock) {
                    lock.notifyAll()
                }
            }

            override fun run() {
                var randomIndex: Int
                activity!!.runOnUiThread {
                    listener!!.startBg()
                    scoreView.text = getString(R.string.score,score)
                }
                try {
                    Thread.sleep(500)
                } catch (e1: InterruptedException) {
                }

                while (!gameOver) {
                    if (Thread.interrupted())
                        return
                    if (pauseRequest) {
                        try {
                            synchronized(lock){
                                lock.wait()
                            }
                        } catch (e: InterruptedException) {
                        }

                    }

                    if (unfilledTiles.isEmpty() || unfilledTiles.isEmpty()) {
                        gameOver = true
                    } else {
                        randomIndex = randomGen.nextInt(unfilledTiles.size)
                        val tileIndex = unfilledTiles[randomIndex]
                        unfilledTiles.removeAt(randomIndex)

                        randomIndex = randomGen.nextInt(unusedColors.size)
                        val colorIndex = unusedColors[randomIndex]
                        unusedColors.removeAt(randomIndex)

                        colorQueue.add(colorIndex)
                        coloredTiles.add(tileIndex)

                        activity!!.runOnUiThread {
                            fillTiles[tileIndex]!!.setImageResource(fillTilesImageIDs[colorIndex])
                        }
                        try {
                            Thread.sleep(delay)
                        } catch (e: InterruptedException) {
                        }

                    }
                }
                if (!forceQuit) {
                    activity!!.runOnUiThread { handleGameOverAction() }
                }
            }
        }
    }

    private fun shuffle(array: IntArray): IntArray {
        val arr = IntArray(array.size)

        val rgen = Random()
        val numbers = ArrayList<Int>(arr.size)

        for (i in arr.indices) {
            arr[i] = array[i]
            numbers.add(arr[i])
        }
        for (i in arr.indices) {
            val index = rgen.nextInt(numbers.size)
            arr[i] = numbers[index]
            numbers.removeAt(index)
        }
        return arr
    }


    private fun enableTapTiles(enable: Boolean) {
        if (enable) {
            for (i in 0 until Data.N_COLORS) {
                tapTiles[i]!!.setOnClickListener(tapListener)
            }
        } else {
            for (i in 0 until Data.N_COLORS) {
                tapTiles[i]!!.setOnClickListener(null)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        listener!!.stopMedia()
        fillTilesThread.onPause()
    }

    override fun onResume() {
        super.onResume()
        if (!gameOver) {
            listener!!.resumeBg()
        }
        fillTilesThread.onResume()
        if (gameOver) {
            fragmentManager!!.beginTransaction().remove(this).commit()
        }
        (activity as AppCompatActivity).supportActionBar!!.hide()
    }

    override fun onDestroy() {
        super.onDestroy()
        forceQuit = true
        fillTilesThread.interrupt()
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }


    companion object {
        private const val INITIAL_DELAY = 750
    }

    interface OnFragmentInteractionListener{
        fun startMediaAndLoop()
        fun stopMedia()
        fun playWav()
        fun pauseBg()
        fun startBg()
        fun resumeBg()
    }

    open inner class ControlThread : Thread() {
        open fun onPause() {}
        open fun onResume() {}
    }

}
