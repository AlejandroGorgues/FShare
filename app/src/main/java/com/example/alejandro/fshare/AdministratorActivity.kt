package com.example.alejandro.fshare

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import com.example.alejandro.fshare.fragments.UserListFragment

class AdministratorActivity : AppCompatActivity(), ChangeListener {


    private var manager: FragmentManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_administrator)

        manager = supportFragmentManager
        val transaction = manager!!.beginTransaction()

        val fragmentLista = UserListFragment()

        transaction.add(R.id.administratorFrameLayout, fragmentLista,  "fragmentUserList")
        transaction.addToBackStack(null)
        transaction.commit()
    }

    override fun replaceFragment(fragment: Fragment) {
        val fragmentManager = manager
        val fragmentTransaction = fragmentManager!!.beginTransaction()
        fragmentTransaction.replace(R.id.administratorFrameLayout, fragment, fragment.toString())
        fragmentTransaction.addToBackStack(fragment.toString())
        fragmentTransaction.commit()
    }

    override fun replaceActivity(activity: Activity) {
        val intent = Intent(this, activity::class.java)
        startActivity(intent)
    }

}
