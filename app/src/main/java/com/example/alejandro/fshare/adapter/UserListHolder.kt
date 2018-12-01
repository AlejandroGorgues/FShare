package com.example.alejandro.fshare.adapter

import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.example.alejandro.fshare.R
import com.example.alejandro.fshare.model.User




class UserListHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {



    var nombreUser: TextView = itemView.findViewById(R.id.uNombre)
    var phoneUser: TextView = itemView.findViewById(R.id.uTelefono)
    var cardUser: CardView = itemView.findViewById(R.id.cardViewUser)
    var viewAux: View = itemView

    fun bindUser(u: User) {

        nombreUser.text = u.nombre
        phoneUser.text = u.telefono

    }



}

interface ClickListenerUser {
    fun elementClicked(id:Int, v: View, user: User)
}