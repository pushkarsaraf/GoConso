package dev.pushkar.goconso

import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.Toast

class SplashActivity : AppCompatActivity() {
    private lateinit var splashThread: Thread
    private lateinit var animator: Thread
    private val splashImages = IntArray(8)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            try {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            } catch (ex: Exception) {
                Toast.makeText(this, "Incompatible device", Toast.LENGTH_LONG).show()
            }

        }
        foo()
    }

    private fun foo() {
        val splashImageView = findViewById<View>(R.id.splash_image_view) as ImageView

        val splashScreen = this

        splashImages[0] = R.drawable.splash00
        splashImages[1] = R.drawable.splash01
        splashImages[2] = R.drawable.splash02
        splashImages[3] = R.drawable.splash03
        splashImages[4] = R.drawable.splash04
        splashImages[5] = R.drawable.splash05
        splashImages[6] = R.drawable.splash06
        splashImages[7] = R.drawable.splash07
        val lock = java.lang.Object()

        animator = object : Thread() {
            @Synchronized override fun run() {
                var i = 0
                while (true) {
                    val index = i
                    runOnUiThread {
                        splashImageView
                            .setImageResource(splashImages[index])
                    }
                    try {
                        synchronized(lock) {
                            lock.wait(200)
                        }
                    } catch (e: InterruptedException) {
                    }

                    i = (i + 1) % 8
                }
            }
        }

        splashThread = object : Thread() {
            override fun run() {
                try {
                    synchronized(lock) {
                        // Wait given period of time or exit on touch
                        lock.wait(2000)
                    }
                } catch (ex: InterruptedException) {
                }

                animator.interrupt()
                finish()
                try {
                    synchronized(lock) {
                        lock.wait(100)
                    }
                } catch (e: InterruptedException) {
                }

                // Run next activity
                val intent = Intent()
                intent.putExtra("navigation", true)
                intent.setClass(splashScreen, MainActivity::class.java)
                startActivity(intent)

            }
        }
        animator.start()
        splashThread.start()
    }

    override fun onTouchEvent(evt: MotionEvent): Boolean {
        if (evt.action == MotionEvent.ACTION_DOWN) {
            synchronized(splashThread) {
                (splashThread as java.lang.Object).notifyAll()
            }
        }
        return true
    }

    override fun onBackPressed() {}
}
