package com.example.alejandro.fshare

import android.app.Activity
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import com.example.alejandro.fshare.fragments.LoginSelectionFragment
import android.content.Intent




class LoginActivity : AppCompatActivity(), ChangeListener, FragmentManager.OnBackStackChangedListener {


    private var manager: FragmentManager? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        manager = supportFragmentManager
        val transaction = manager!!.beginTransaction()

        val fragmentLista = LoginSelectionFragment()

        transaction.add(R.id.loginFrameLayout, fragmentLista,  "fragmentLogin")
        transaction.addToBackStack("fragmentLogin")
        transaction.commit()
        manager!!.addOnBackStackChangedListener(this)

    }

    override fun replaceFragment(fragment: Fragment) {
        val fragmentManager = manager
        val fragmentTransaction = fragmentManager!!.beginTransaction()
        fragmentTransaction.replace(R.id.loginFrameLayout, fragment, fragment.toString())
        fragmentTransaction.addToBackStack(fragment.toString())
        fragmentTransaction.commit()
    }

    override fun replaceActivity(activity: Activity) {
        val intent = Intent(this, activity::class.java)
        startActivity(intent)
    }

    override fun onBackStackChanged() {

    }
}
