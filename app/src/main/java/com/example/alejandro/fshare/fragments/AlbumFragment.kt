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
import android.widget.Toast
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
    private var queryAux: Query? = null
    private var nameImage: Uri? = null

    private var currentEmail: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_album, container, false)

        mAuth = FirebaseAuth.getInstance()

        setHasOptionsMenu(true)
        val toolbar = view.findViewById<Toolbar>(R.id.userToolbar)
        if(mAuth!!.currentUser!!.email!! == "administrator@gmail.com") {

            (activity as AdministratorActivity).setSupportActionBar(toolbar)
        }else{
            (activity as UserActivity).setSupportActionBar(toolbar)
        }

        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()

        referenceFotoListDatabase = database!!.reference.child("photos")

        recyclerFotoList = view.findViewById(R.id.rvfotoList)

        //Selección del correo correspondiente al album de fotos actual
        val bundle = this.arguments
        if(bundle != null){

            currentEmail = if(bundle.getBoolean("admin")) {
                bundle.getString("correoActual")

            }else{
                mAuth!!.currentUser!!.email
            }
        }

        //Selección de las fotos correspondientes al correo actual para rellenar la lista
        query = referenceFotoListDatabase.orderByChild("correo").equalTo(currentEmail)

        //Obtención del objeto de tipo FirebaseRecyclerOptions<Photo!> usado por FirebaseRecyclerAdapter
        val options = FirebaseRecyclerOptions.Builder<Photo>()
                .setQuery(query!!, Photo::class.java)
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
        if(mAuth!!.currentUser!!.email == "administrator@gmail.com") {
            activity!!.menuInflater.inflate(R.menu.admin_options_menu, menu)
        }else{
            activity!!.menuInflater.inflate(R.menu.user_options_menu, menu)
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if(mAuth!!.currentUser!!.email == "administrator@gmail.com") {
            return when (item.itemId) {
                R.id.action_out -> {
                    //Se desconecta de la aplicación
                    FirebaseAuth.getInstance().signOut()
                    val fr = LoginActivity()
                    val fc = activity as ChangeListener?
                    fc!!.replaceActivity(fr)
                    true
                }
                R.id.action_back-> {
                    //Vuelve al fragment anterior
                    val fr = UserListFragment()
                    val fc = activity as ChangeListener?
                    fc!!.replaceFragment(fr)
                    true
                }

                else -> super.onOptionsItemSelected(item)
            }
        }else{
            return when (item.itemId) {
                R.id.action_out-> {
                    //Se desconecta de la aplicación
                    FirebaseAuth.getInstance().signOut()
                    val fr = LoginActivity()
                    val fc = activity as ChangeListener?
                    fc!!.replaceActivity(fr)
                    true
                }
                R.id.action_show-> {
                    //Muestra su perfil
                    referenceUserDatabase = database!!.reference.child("user")
                    accederPerfil()
                    true
                }

                R.id.action_back-> {
                    //Vuelve al fragment anterior
                    FirebaseAuth.getInstance().signOut()
                    val fr = LoginActivity()
                    val fc = activity as ChangeListener?
                    fc!!.replaceActivity(fr)
                    true
                }
                else -> super.onOptionsItemSelected(item)
            }
        }
    }

    //Procede a msotrar la foto y su comentario con la posibilidad de modificarlos
    override fun elementClicked(id: Int, v: View, foto: Photo) {
        val bundle = Bundle()

        bundle.putString("Comentario", foto.comentario)
        bundle.putString("Foto", foto.foto)
        bundle.putString("Uri", foto.uri)
        bundle.putString("correoActual", currentEmail)

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
                //Actualiza los campos de la laerta
                nameImage =  data.data
                dirFoto!!.text = Editable.Factory.getInstance().newEditable(PathClass().getRealPathFromURI(context!!, nameImage!!))
            }
        }
    }

    private fun inicializarReciclerView(){
        recyclerFotoList!!.adapter = mAdapter
        recyclerFotoList!!.layoutManager = GridLayoutManager(activity, 2)
        recyclerFotoList!!.itemAnimator = DefaultItemAnimator()
    }

    //Carga los datos en unbundle para luego ser usuados en el siguiente fragment
    private fun accederPerfil(){
        val bundle = Bundle()

        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for( ds: DataSnapshot in dataSnapshot.children){
                    val usuario = ds.getValue(User::class.java)
                    if(usuario!!.correo == currentEmail){

                        bundle.putString("Nombre", usuario.nombre)
                        bundle.putString("Password", usuario.password)
                        bundle.putString("Telefono", usuario.telefono)
                        bundle.putString("Correo", usuario.correo)

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


    //Muestra una alerta que permite subir la foto
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
        //Muestra y almacena el directorio de la foto seleccionada
        obtenerDir!!.setOnClickListener {

            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "image/*"
            startActivityForResult(intent, OPEN_DOCUMENT_CODE)
        }

        //Procede a cargar la foto en la base de datos
        upload!!.setOnClickListener {
            if( dirFoto!!.text.isEmpty() && comentarioLayout!!.editText!!.text.isEmpty()){
                Toast.makeText(this.context, resources.getString(R.string.informacionCargaFoto), Toast.LENGTH_LONG).show()
            }else {
                storageReference = storage!!.reference.child(currentEmail!!)


                val fotoRef = storageReference!!.child(nameImage!!.lastPathSegment)
                val uploadTask = fotoRef.putFile(nameImage!!)
                uploadTask.addOnFailureListener {}.addOnSuccessListener {
                    storage!!.reference.child(currentEmail + "/" + nameImage!!.lastPathSegment).downloadUrl.addOnSuccessListener { itUrl ->
                        val foto = Photo(itUrl.toString(), comentarioLayout!!.editText!!.text.toString(), currentEmail + "/" + nameImage!!.lastPathSegment, currentEmail!!)

                        referenceFotoDatabase = database!!.reference.child("photos")
                        checkPhoto(foto)
                        show.dismiss()
                    }.addOnFailureListener {
                        Toast.makeText(this.context, resources.getString(R.string.falloCargaFoto), Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    //Comprueba si la foto existía con el fin de que no haya duplicados
    private fun checkPhoto(foto: Photo){
        var existeFoto = false
        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for(ds: DataSnapshot in dataSnapshot.children) {
                        val fotoAux = ds.getValue(Photo::class.java)!!
                        if (fotoAux.foto == foto.foto && foto.correo == fotoAux.correo) {
                            existeFoto = true

                        }
                    }

                if(!existeFoto) {
                   val key = database!!.reference.child("photos").push().key
                    database!!.reference.child("photos").child(key!!).setValue(foto)
                }

            }

            override fun onCancelled(databaseError: DatabaseError) {}
        }
        referenceFotoDatabase.addListenerForSingleValueEvent(valueEventListener)
    }
}
