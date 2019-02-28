package dev.pushkar.goconso.ui.main

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.*
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.games.Games
import com.google.android.material.bottomsheet.BottomSheetBehavior
import dev.pushkar.goconso.Data
import dev.pushkar.goconso.Info
import dev.pushkar.goconso.R
import kotlinx.android.synthetic.main.fragment_main.*
import kotlinx.android.synthetic.main.info_sheet.*

class MainFragment : Fragment() {

    private var mListener: OnFragmentInteractionListener? = null
    private lateinit var bottomSheetViewGroup: LinearLayout
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>
    private lateinit var sp: SharedPreferences
    private lateinit var edit: SharedPreferences.Editor

    companion object {
        fun newInstance() = MainFragment()
    }

    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun showAchievements()
        fun signOut()
        fun showScoreboard()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        init()
        super.onActivityCreated(savedInstanceState)
        this.setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.main_menu, menu)
        menu.findItem(R.id.sign_out).setOnMenuItemClickListener {
            mListener!!.signOut()
            return@setOnMenuItemClickListener true
        }
        menu.findItem(R.id.info).setOnMenuItemClickListener {
            startActivity(Intent(activity, Info::class.java))
            return@setOnMenuItemClickListener true
        }
        menu.findItem(R.id.multiplayer).isVisible = Data.localData.getBoolean("multiplayer",false)
        menu.findItem(R.id.multiplayer).setOnMenuItemClickListener {
            Toast.makeText(activity!!, "Update to play in multiplayer mode", Toast.LENGTH_LONG).show()
            return@setOnMenuItemClickListener true
        }
        activity!!.invalidateOptionsMenu()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            mListener = context
        } else {
            throw RuntimeException("$context must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    private fun init(){
        if(GoogleSignIn.getLastSignedInAccount(activity!!)!=null) {
            Games.getGamesClient(activity!!, GoogleSignIn.getLastSignedInAccount(activity!!)!!).setViewForPopups(view!!)
        } else {
            activity!!.finish()
        }
        button_leaderboard.setOnClickListener {
            mListener!!.showAchievements()
        }
        button_lb.setOnClickListener{
            mListener!!.showScoreboard()
        }
        bottomSheetViewGroup = info_sheet
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetViewGroup)
        bottomSheetBehavior.peekHeight = 0
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        sp = activity!!.getSharedPreferences(resources.getString(R.string.app_name), Context.MODE_PRIVATE)
        if (!sp.contains(resources.getString(R.string.app_name))) {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
        btn_submit.setOnClickListener {
            edit = sp.edit()
            edit.putString(getString(R.string.info_sheet_1), fn.text.toString())
            edit.putString(getString(R.string.info_sheet_2), ln.text.toString())
            edit.putBoolean(getString(R.string.app_name), true)
            edit.apply()
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }
        button_play_tap_tiles.setOnClickListener {
            val ft = fragmentManager!!.beginTransaction()
            ft.replace(id, GamePlay(), "NewFragmentTag")
            ft.commit()
        }
        Data.localData = sp
        this.view!!.isFocusableInTouchMode= true
        this.view!!.requestFocus()
        this.view!!.setOnKeyListener { _, i, _ ->
            if(i == KeyEvent.KEYCODE_BACK) {
                if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
                    if (fn.text.isNullOrEmpty()) {
                        fnl.error = "Please enter a valid name"
                    } else {
                        btn_submit.performClick()
                    }
                } else {
                    activity!!.finish()
                }
            }
            return@setOnKeyListener true
        }
    }

    override fun onResume() {
        super.onResume()
        (activity as AppCompatActivity).supportActionBar!!.show()

    }

    override fun onPause() {
        super.onPause()
        (activity as AppCompatActivity).supportActionBar!!.hide()
    }

}
