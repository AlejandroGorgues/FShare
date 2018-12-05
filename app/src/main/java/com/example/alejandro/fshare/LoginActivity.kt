package com.example.alejandro.fshare

import android.app.Activity
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import com.example.alejandro.fshare.fragments.LoginSelectionFragment
import android.content.Intent
import android.util.Log


class LoginActivity : AppCompatActivity(), ChangeListener {


    private var manager: FragmentManager? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        manager = supportFragmentManager
        val transaction = manager!!.beginTransaction()

        val fragmentLista = LoginSelectionFragment()

        transaction.add(R.id.loginFrameLayout, fragmentLista)
        transaction.commit()

    }

    override fun replaceFragment(fragment: Fragment) {
        val fragmentManager = manager
        val fragmentTransaction = fragmentManager!!.beginTransaction()
        fragmentTransaction.replace(R.id.loginFrameLayout, fragment)
        fragmentTransaction.commit()
    }

    override fun replaceActivity(activity: Activity) {

        val intentAux = Intent(this, activity::class.java)
        if(activity.intent != null) {
            intentAux.putExtra("correoActual", activity.intent.extras.getString("correoActual"))
            intentAux.putExtra("admin", activity.intent.extras.getBoolean("admin"))
        }
        startActivity(intentAux)
    }
}
