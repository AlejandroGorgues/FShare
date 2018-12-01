package com.example.alejandro.fshare.fragments


import android.os.Bundle
import android.support.design.widget.TextInputLayout
import android.support.v4.app.Fragment
import android.support.v7.widget.Toolbar
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import com.example.alejandro.fshare.GlideApp

import com.example.alejandro.fshare.R
import com.example.alejandro.fshare.UserActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference


class PhotoDetailFragment : Fragment() {

    private var mAuth: FirebaseAuth? = null

    private var storage: FirebaseStorage? = null
    private var storageReference: StorageReference? = null
    private var storageReferencePhoto: StorageReference? = null

    private var database: FirebaseDatabase? = null
    private lateinit var referenceFotoDatabase: DatabaseReference
    private lateinit var referenceUserDatabase: DatabaseReference
    private lateinit var referenceFotoListDatabase: DatabaseReference

    private var changePhoto: Button? = null
    private var saveChanges: Button? = null
    private var deletePhoto: Button? = null
    private var cancel: Button? = null

    private var comentarioLayout: TextInputLayout? = null
    private var fotoView: ImageView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_photo_detail, container, false)

        //TODO diferenciar entre administrador y usuario
        setHasOptionsMenu(true)
        val toolbar = view.findViewById<Toolbar>(R.id.userToolbar)
        (activity as UserActivity).setSupportActionBar(toolbar)

        mAuth = FirebaseAuth.getInstance()

        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()

        comentarioLayout = view.findViewById(R.id.comentario_layout)
        fotoView = view.findViewById(R.id.fotoDetail)

        changePhoto = view.findViewById(R.id.button_changePhoto)
        saveChanges = view.findViewById(R.id.button_savePhoto)
        deletePhoto = view.findViewById(R.id.button_deletePhoto)
        cancel = view.findViewById(R.id.button_cancel)

        val bundle = this.arguments
        if(bundle != null){
            comentarioLayout!!.editText!!.text = Editable.Factory.getInstance().newEditable(bundle.getString("Comentario"))
            GlideApp
                    .with(this)
                    .load(bundle.getString("Foto"))
                    .into(fotoView!!)
        }

        return view
    }

    fun encodeString(string: String): String {
        val stringAux = string.replace("@", "?")
        return stringAux.replace(".", ",")
    }

    fun decodeString(string: String): String {
        val stringAux = string.replace("?", "@")
        return stringAux.replace(",", ".")
    }


}
