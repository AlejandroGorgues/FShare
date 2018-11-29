package com.example.alejandro.fshare.fragments


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.alejandro.fshare.ChangeListener

import com.example.alejandro.fshare.R
import com.firebase.ui.auth.util.ui.SupportVectorDrawablesButton

class LoginSelectionFragment : Fragment() {

    private var googleButton: SupportVectorDrawablesButton? = null
    private var facebookButton: SupportVectorDrawablesButton? = null
    private var twitterButton: SupportVectorDrawablesButton? = null
    private var githubButton: SupportVectorDrawablesButton? = null
    private var phoneButton: SupportVectorDrawablesButton? = null
    private var emailButton: SupportVectorDrawablesButton? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_login_selection, container, false)

        googleButton = view.findViewById(R.id.google_button)
        facebookButton = view.findViewById(R.id.facebook_button)
        twitterButton = view.findViewById(R.id.twitter_button)
        githubButton = view.findViewById(R.id.github_button)
        phoneButton = view.findViewById(R.id.phone_button)
        emailButton = view.findViewById(R.id.email_button)

        googleButton!!.setOnClickListener {
            showEmailFragment()
        }

        facebookButton!!.setOnClickListener {
            showEmailFragment()
        }

        twitterButton!!.setOnClickListener {
            showEmailFragment()
        }

        githubButton!!.setOnClickListener {
            showEmailFragment()
        }

        phoneButton!!.setOnClickListener {
            showEmailFragment()
        }

        emailButton!!.setOnClickListener {
            showEmailFragment()
        }


        return view
    }



    private fun showEmailFragment() {
        val fr = SignInEmailFragment()
        val fc = activity as ChangeListener?
        fc!!.replaceFragment(fr)
    }
}
