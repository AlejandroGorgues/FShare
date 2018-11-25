package com.example.alejandro.fshare

import android.app.Activity
import android.support.v4.app.Fragment

interface ChangeListener {
        fun replaceFragment(fragment: Fragment)

        fun replaceActivity(activity: Activity)
}