package com.example.alejandro.fshare

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.util.Log
import com.example.alejandro.fshare.fragments.UserListFragment
import com.firebase.ui.auth.data.model.User
import com.google.firebase.auth.FirebaseAuth

class AdministratorActivity : AppCompatActivity(), ChangeListener {


    private var manager: FragmentManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_administrator)

        manager = supportFragmentManager
        val transaction = manager!!.beginTransaction()

        val fragmentLista = UserListFragment()

        transaction.add(R.id.administratorFrameLayout, fragmentLista)
        transaction.commit()
    }


    override fun replaceFragment(fragment: Fragment) {
        val fragmentManager = manager
        val fragmentTransaction = fragmentManager!!.beginTransaction()
        fragmentTransaction.replace(R.id.administratorFrameLayout, fragment)
        fragmentTransaction.commit()
    }

    override fun replaceActivity(activity: Activity) {
        val intent = Intent(this, activity::class.java)
        startActivity(intent)
    }

}
