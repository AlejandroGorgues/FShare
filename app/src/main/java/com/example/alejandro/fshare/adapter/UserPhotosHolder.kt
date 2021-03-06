package com.example.alejandro.fshare.adapter

import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.request.RequestOptions
import com.example.alejandro.fshare.GlideApp
import com.example.alejandro.fshare.R
import com.example.alejandro.fshare.model.Photo


class UserPhotosHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    var commentarioFoto: TextView = itemView.findViewById(R.id.fotoComentario)
    private var foto: ImageView = itemView.findViewById(R.id.fotoAux)
    var cardUser: CardView = itemView.findViewById<CardView>(R.id.cardViewFoto)
    var viewAux: View = itemView

    fun bindFoto(f: Photo) {

            commentarioFoto.text = f.comentario
            GlideApp
                    .with(itemView.context)
                    .load(f.foto)
                    .centerCrop()
                    .apply(RequestOptions().circleCrop())
                    .into(foto)
    }

}

//Interfaz que es utilizada para implementar el click en el elemento
interface ClickListenerPhoto {
    fun elementClicked(id:Int, v: View, foto: Photo)
}