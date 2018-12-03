package com.example.alejandro.fshare.fragments


import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.TextInputLayout
import android.support.v4.app.Fragment
import android.support.v7.widget.Toolbar
import android.text.Editable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.example.alejandro.fshare.*
import com.example.alejandro.fshare.R
import com.example.alejandro.fshare.model.Photo
import com.google.firebase.database.*




class PhotoDetailFragment : Fragment() {

    private var mAuth: FirebaseAuth? = null

    private var storage: FirebaseStorage? = null
    private var storageReference: StorageReference? = null
    private var storageReferencePhoto: StorageReference? = null

    private var database: FirebaseDatabase? = null
    private lateinit var referenceDeleteFotoDatabase: DatabaseReference
    private lateinit var referenceModifyFotoDatabase: DatabaseReference
    private lateinit var referenceFotoComentarioDatabase: DatabaseReference

    private var changePhoto: Button? = null
    private var saveChanges: Button? = null
    private var deletePhoto: Button? = null
    private var cancel: Button? = null

    private var comentarioLayout: TextInputLayout? = null
    private var fotoView: ImageView? = null
    private var currentEmail: String? = null
    private var comentarioAux: String? = null
    private var url: String?= null
    private var uri: Uri? = null
    private var uri2: Uri? = null

    private var cambiarImagen = false

    private val OPEN_DOCUMENT_CODE = 2


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_photo_detail, container, false)


        setHasOptionsMenu(true)


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
            comentarioAux = bundle.getString("Comentario")
            comentarioLayout!!.editText!!.text = Editable.Factory.getInstance().newEditable(comentarioAux)
            uri = Uri.parse(bundle.getString("Uri"))
            url = bundle.getString("Foto")
            GlideApp
                    .with(this)
                    .load(url)
                    .into(fotoView!!)
            currentEmail = bundle.getString("correoActual")
        }

        changePhoto!!.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "image/*"
            startActivityForResult(intent, OPEN_DOCUMENT_CODE)
        }

        saveChanges!!.setOnClickListener {
            if(cambiarImagen) {

                storageReference = storage!!.reference.child(currentEmail!!)
                val fotoRef = storageReference!!.child(uri2!!.lastPathSegment)
                val uploadTask = fotoRef.putFile(uri2!!)
                uploadTask.addOnFailureListener {
                    // Handle unsuccessful uploads
                }.addOnSuccessListener {
                    storage!!.reference.child(mAuth!!.currentUser!!.email!! + "/" + uri2!!.lastPathSegment).downloadUrl.addOnSuccessListener { itUrl ->
                        val foto = Photo(itUrl.toString(), comentarioLayout!!.editText!!.text.toString(), currentEmail + "/" + uri2!!.lastPathSegment, currentEmail!!)
                        referenceModifyFotoDatabase = database!!.reference.child("photos")
                        checkPhoto(foto)
                        val fr = AlbumFragment()
                        val fc = activity as ChangeListener?
                        fc!!.replaceFragment(fr)
                    }.addOnFailureListener {
                        // Handle any errors
                    }
                }
            }else{
                if(comentarioLayout!!.editText!!.text.toString() != comentarioAux){
                    referenceFotoComentarioDatabase = database!!.reference.child("photos")
                    changeComment()
                }



            }
        }

        deletePhoto!!.setOnClickListener {

            storageReferencePhoto = storage!!.reference.child(uri!!.toString())
            storageReferencePhoto!!.delete().addOnSuccessListener {
                // File deleted successfully
                referenceDeleteFotoDatabase = database!!.reference.child("photos")
                deletePhotoDB()

            }.addOnFailureListener {

            }
        }

        cancel!!.setOnClickListener {
            val fr = AlbumFragment()
            val fc = activity as ChangeListener?
            fc!!.replaceFragment(fr)
        }

        if(mAuth!!.currentUser!!.email == "administrator@gmail.com") {
            val toolbar = view.findViewById<Toolbar>(R.id.adminToolbar)
            (activity as AdministratorActivity).setSupportActionBar(toolbar)
        }else{
            val toolbar = view.findViewById<Toolbar>(R.id.userToolbar)
            (activity as UserActivity).setSupportActionBar(toolbar)
        }

        return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == OPEN_DOCUMENT_CODE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                cambiarImagen = true
                uri2 = data.data
                GlideApp
                        .with(this)
                        .load(data.data)
                        .into(fotoView!!)
            }
        }
    }

    private fun deletePhotoDB(){
        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for( ds: DataSnapshot in dataSnapshot.children){
                    val foto = ds.getValue(Photo::class.java)
                    if(foto!!.foto == url && foto.correo == currentEmail){
                        val key = ds.key
                        referenceDeleteFotoDatabase.child(key!!).removeValue()

                        val fr = AlbumFragment()
                        val fc = activity as ChangeListener?
                        fc!!.replaceFragment(fr)
                        break
                    }

                }

            }

            override fun onCancelled(databaseError: DatabaseError) {}
        }

        referenceDeleteFotoDatabase.addListenerForSingleValueEvent(valueEventListener)
    }

    private fun checkPhoto(foto: Photo){
        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for(ds: DataSnapshot in dataSnapshot.children){
                    val fotoAux = ds.getValue(Photo::class.java)!!
                    if(fotoAux.foto == url && fotoAux.correo == foto.correo) {
                        referenceModifyFotoDatabase.child(ds.key.toString()).setValue(foto)
                    }
                }

            }

            override fun onCancelled(databaseError: DatabaseError) {}
        }
        referenceModifyFotoDatabase.addListenerForSingleValueEvent(valueEventListener)
    }

    private fun changeComment(){
        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for(ds: DataSnapshot in dataSnapshot.children){
                    val fotoAux = ds.getValue(Photo::class.java)!!
                    if(fotoAux.foto == url && fotoAux.correo == currentEmail) {
                        referenceFotoComentarioDatabase.child(ds.key.toString()).child("comentario").setValue(comentarioLayout!!.editText!!.text.toString())
                        val fr = AlbumFragment()
                        val fc = activity as ChangeListener?
                        fc!!.replaceFragment(fr)
                    }
                }

            }

            override fun onCancelled(databaseError: DatabaseError) {}
        }
        referenceFotoComentarioDatabase.addListenerForSingleValueEvent(valueEventListener)
    }
}
