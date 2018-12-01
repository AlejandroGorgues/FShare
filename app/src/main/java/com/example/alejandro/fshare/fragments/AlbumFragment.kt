package com.example.alejandro.fshare.fragments


import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.TextInputLayout
import android.support.v4.app.Fragment
import android.support.v7.widget.*
import android.view.*
import android.widget.Button
import android.widget.EditText
import com.example.alejandro.fshare.adapter.ClickListenerPhoto
import com.example.alejandro.fshare.adapter.UserPhotosHolder
import com.example.alejandro.fshare.model.Photo
import com.example.alejandro.fshare.model.User
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import android.content.Intent
import android.text.Editable
import android.util.Log
import android.widget.ImageView
import com.example.alejandro.fshare.*
import com.example.alejandro.fshare.R
import com.firebase.ui.database.FirebaseRecyclerOptions


class AlbumFragment : Fragment(), ClickListenerPhoto {


    private var mAuth: FirebaseAuth? = null

    private var storage: FirebaseStorage? = null
    private var storageReference: StorageReference? = null

    private var database: FirebaseDatabase? = null
    private lateinit var referenceFotoDatabase: DatabaseReference
    private lateinit var referenceUserDatabase: DatabaseReference
    private lateinit var referenceFotoListDatabase: DatabaseReference

    private var mAdapter: FirebaseRecyclerAdapter<Photo, UserPhotosHolder>? = null
    private var recyclerFotoList: RecyclerView? = null

    private var obtenerDir: Button? = null
    private var upload: Button? = null

    private var comentarioLayout: TextInputLayout? = null
    private var dirFoto: EditText? = null

    private lateinit var addPhotoFloatingB: FloatingActionButton

    private val OPEN_DOCUMENT_CODE = 2

    private var query: Query? = null
    private var path: Uri? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_album, container, false)

        setHasOptionsMenu(true)
        val toolbar = view.findViewById<Toolbar>(R.id.userToolbar)
        (activity as UserActivity).setSupportActionBar(toolbar)

        mAuth = FirebaseAuth.getInstance()

        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()

        referenceFotoListDatabase = database!!.reference.child("user").child(encodeString(mAuth!!.currentUser!!.email!!)).child("photos")
        // Inflate the layout for this fragment
        recyclerFotoList = view.findViewById(R.id.rvfotoList)


        query = referenceFotoListDatabase
        val options = FirebaseRecyclerOptions.Builder<Photo>()
                .setQuery(query as DatabaseReference, Photo::class.java)
                .build()

        mAdapter = object : FirebaseRecyclerAdapter<Photo, UserPhotosHolder>(options) {


            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserPhotosHolder {
                val viewHolder = LayoutInflater.from(parent.context)
                        .inflate(R.layout.photo_format_holder, parent, false)

                return UserPhotosHolder(viewHolder)
            }

            override fun onBindViewHolder(holder: UserPhotosHolder, position: Int, foto: Photo) {
                holder.bindFoto(foto)
                holder.cardUser.setOnClickListener {
                    elementClicked(holder.adapterPosition, holder.viewAux, foto)
                }


            }

        }
        inicializarReciclerView()


        addPhotoFloatingB = view.findViewById(R.id.floatSubirFoto)
        addPhotoFloatingB.setOnClickListener {
            subirFotoDialog() }


        return view
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        activity!!.menuInflater.inflate(R.menu.user_options_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_out-> {
                FirebaseAuth.getInstance().signOut()
                val fr = LoginActivity()
                val fc = activity as ChangeListener?
                fc!!.replaceActivity(fr)
                true
            }
            R.id.action_show-> {

                referenceUserDatabase = database!!.reference.child("user")
                accederPerfil()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun elementClicked(id: Int, v: View, foto: Photo) {
        val bundle = Bundle()

        bundle.putString("Comentario", foto.comentario)
        bundle.putString("Foto", "https://i.imgur.com/DvpvklR.png")

        val fr = PhotoDetailFragment()
        fr.arguments = bundle

        val fc = activity as ChangeListener?
        fc!!.replaceFragment(fr)
    }

    override fun onStart() {
        super.onStart()
        mAdapter?.startListening()

    }


    override fun onStop() {
        super.onStop()
        mAdapter?.stopListening()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == OPEN_DOCUMENT_CODE && resultCode == RESULT_OK) {
            if (data != null) {

                path = data.data
                dirFoto!!.text = Editable.Factory.getInstance().newEditable(path!!.lastPathSegment.toString())
            }
        }
    }

    private fun inicializarReciclerView(){
        recyclerFotoList!!.adapter = mAdapter
        recyclerFotoList!!.layoutManager =  GridLayoutManager(activity, 2)
        recyclerFotoList!!.itemAnimator = DefaultItemAnimator()
    }

    private fun accederPerfil(){
        val bundle = Bundle()

        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for( ds: DataSnapshot in dataSnapshot.children){
                    val usuario = ds.getValue(User::class.java)
                    if(usuario!!.correo == encodeString(mAuth!!.currentUser!!.email!!)){

                        bundle.putString("Nombre", usuario.nombre)
                        bundle.putString("Password", usuario.password)
                        bundle.putString("Telefono", usuario.telefono)
                        bundle.putString("Correo", decodeString(usuario.correo))

                        val fr = UserDataFragment()
                        fr.arguments = bundle
                        val fc = activity as ChangeListener?
                        fc!!.replaceFragment(fr)
                        break
                    }

                }

            }

            override fun onCancelled(databaseError: DatabaseError) {}
        }

        referenceUserDatabase.addListenerForSingleValueEvent(valueEventListener)
    }


    private fun subirFotoDialog(){
        val inflater = layoutInflater
        val view = inflater.inflate(R.layout.upload_photo_dialog, null)

        obtenerDir = view.findViewById(R.id.btn_select)
        upload = view.findViewById(R.id.btn_upload)

        comentarioLayout = view.findViewById(R.id.commentPhoto_layout)
        dirFoto = view.findViewById(R.id.directorioPhoto)

        val dialogo = AlertDialog.Builder(activity)
        dialogo.setCancelable(true)
        dialogo.setView(view)
        val show = dialogo.show()
        obtenerDir!!.setOnClickListener {

            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "image/*"
            startActivityForResult(intent, OPEN_DOCUMENT_CODE)
        }

        upload!!.setOnClickListener {

            //Referencia mediante el correo electrónico sin codificarlo
            storageReference = storage!!.reference.child(mAuth!!.currentUser!!.email!!)

            //val file = Uri.fromFile(File(path))
            val fotoRef = storageReference!!.child(path!!.lastPathSegment)
            val uploadTask = fotoRef.putFile(path!!)
            val foto = Photo(mAuth!!.currentUser!!.email!!+"/"+path!!.lastPathSegment, comentarioLayout!!.editText!!.text.toString())

            // Register observers to listen for when the download is done or if it fails
            uploadTask.addOnFailureListener {
                // Handle unsuccessful uploads
            }.addOnSuccessListener {
                referenceFotoDatabase = database!!.reference.child("user").child(encodeString(mAuth!!.currentUser!!.email!!)).child("photos")
                checkPhoto(foto)
                show.dismiss()
            }
        }
    }

    private fun checkPhoto(foto: Photo){
        var existeFoto = false
        var existeDir = false
        var fotoAux: Photo
        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if(dataSnapshot.exists()){
                    existeDir = true
                    for(ds: DataSnapshot in dataSnapshot.children){
                        fotoAux = ds.getValue(Photo::class.java)!!
                        if(fotoAux.foto == foto.foto  && fotoAux.comentario == foto.comentario) {
                            existeFoto = true
                        }
                    }
                }

                if(!existeDir){
                    database!!.reference.child("user").child(encodeString(mAuth!!.currentUser!!.email!!)+"/make").setValue("photos")
                }

                if(!existeFoto) {
                    val key = database!!.reference.child("user").child(encodeString(mAuth!!.currentUser!!.email!!)).child("photos").push().key
                    database!!.reference.child("user").child(encodeString(mAuth!!.currentUser!!.email!!)).child("photos").child(key!!).setValue(foto)
                }else{
                    val key = database!!.reference.child("user").child(encodeString(mAuth!!.currentUser!!.email!!)).child("photos").push().key
                    database!!.reference.child("user").child(encodeString(mAuth!!.currentUser!!.email!!)).child("photos").child(key!!).setValue(foto)
                }

            }

            override fun onCancelled(databaseError: DatabaseError) {}
        }
        referenceFotoDatabase.addListenerForSingleValueEvent(valueEventListener)
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
