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
import android.view.*
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.example.alejandro.fshare.*
import com.example.alejandro.fshare.R
import com.example.alejandro.fshare.model.Photo
import com.example.alejandro.fshare.model.User
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
    private lateinit var referenceUserDatabase: DatabaseReference

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
        val toolbar = view.findViewById<Toolbar>(R.id.photoToolbar)
        //Comprueba si el usuario actual es el administrador o no para cargar una toolbar específica
        if(mAuth!!.currentUser!!.email!! == "administrator@gmail.com") {
            (activity as AdministratorActivity).setSupportActionBar(toolbar)
        }else{
            (activity as UserActivity).setSupportActionBar(toolbar)
        }

        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()

        comentarioLayout = view.findViewById(R.id.comentario_layout)
        fotoView = view.findViewById(R.id.fotoDetail)

        changePhoto = view.findViewById(R.id.button_changePhoto)
        saveChanges = view.findViewById(R.id.button_savePhoto)
        deletePhoto = view.findViewById(R.id.button_deletePhoto)
        cancel = view.findViewById(R.id.button_cancel)

        //Carga los elementos con los datos pasados por medio del bundle
        val bundle = this.arguments
        if(bundle != null){
            comentarioAux = bundle.getString("Comentario")
            comentarioLayout!!.editText!!.text = Editable.Factory.getInstance().newEditable(comentarioAux)
            uri = Uri.parse(bundle.getString("Uri"))
            url = bundle.getString("Foto")
            GlideApp
                    .with(this)
                    .load(url)
                    .centerCrop()
                    .apply(RequestOptions().circleCrop())
                    .into(fotoView!!)
            currentEmail = bundle.getString("correoActual")
        }

        //Cambia la foto actual
        changePhoto!!.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "image/*"
            startActivityForResult(intent, OPEN_DOCUMENT_CODE)
        }

        //Guarda todos los cambios en la base de datos
        saveChanges!!.setOnClickListener {
            if(cambiarImagen) {
                //Si quiere cambiar la imagen
                storageReference = storage!!.reference.child(currentEmail!!)
                val fotoRef = storageReference!!.child(uri2!!.lastPathSegment)
                val uploadTask = fotoRef.putFile(uri2!!)
                uploadTask.addOnFailureListener {
                    // Handle unsuccessful uploads
                }.addOnSuccessListener {
                    storage!!.reference.child(currentEmail + "/" + uri2!!.lastPathSegment).downloadUrl.addOnSuccessListener { itUrl ->
                        val foto = Photo(itUrl.toString(), comentarioLayout!!.editText!!.text.toString(), currentEmail + "/" + uri2!!.lastPathSegment, currentEmail!!)
                        referenceModifyFotoDatabase = database!!.reference.child("photos")
                        checkPhoto(foto)
                        passData()
                    }.addOnFailureListener {
                        Toast.makeText(this.context,resources.getString(R.string.fallGuardarFoto), Toast.LENGTH_LONG).show()
                    }
                }
            }else{
                //Si solo quiere cambiar el comentario
                if(comentarioLayout!!.editText!!.text.toString() != comentarioAux){
                    referenceFotoComentarioDatabase = database!!.reference.child("photos")
                    changeComment()
                }
            }
        }

        //Borra la foto actual
        deletePhoto!!.setOnClickListener {

            storageReferencePhoto = storage!!.reference.child(uri!!.toString())
            storageReferencePhoto!!.delete().addOnSuccessListener {
                // File deleted successfully
                referenceDeleteFotoDatabase = database!!.reference.child("photos")
                deletePhotoDB()

            }.addOnFailureListener {}
        }

        cancel!!.setOnClickListener {
            passData()
        }

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
                    val fr = AlbumFragment()
                    fr.arguments = passData(true)
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
                    val fr = AlbumFragment()
                    fr.arguments = passData(false)
                    val fc = activity as ChangeListener?
                    fc!!.replaceFragment(fr)
                    true
                }
                else -> super.onOptionsItemSelected(item)
            }
        }
    }

    //Cambia la imagen mostrada en la pantalla
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == OPEN_DOCUMENT_CODE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                cambiarImagen = true
                uri2 = data.data
                GlideApp
                        .with(this)
                        .load(data.data)
                        .centerCrop()
                        .apply(RequestOptions().circleCrop())
                        .into(fotoView!!)
            }
        }
    }

    //Borra la foto de la base de datos
    private fun deletePhotoDB(){
        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for( ds: DataSnapshot in dataSnapshot.children){
                    val foto = ds.getValue(Photo::class.java)
                    if(foto!!.foto == url && foto.correo == currentEmail){
                        val key = ds.key
                        referenceDeleteFotoDatabase.child(key!!).removeValue()

                       passData()
                        break
                    }
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        }
        referenceDeleteFotoDatabase.addListenerForSingleValueEvent(valueEventListener)
    }

    //Comprueba si la foto ya estaba antes en el base de datos para cambiar el objeto
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

    //Cambia el comentario de la base de datos
    private fun changeComment(){
        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for(ds: DataSnapshot in dataSnapshot.children){
                    val fotoAux = ds.getValue(Photo::class.java)!!
                    if(fotoAux.foto == url && fotoAux.correo == currentEmail) {
                        referenceFotoComentarioDatabase.child(ds.key.toString()).child("comentario").setValue(comentarioLayout!!.editText!!.text.toString())
                        passData()
                    }
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        }
        referenceFotoComentarioDatabase.addListenerForSingleValueEvent(valueEventListener)
    }

    //Guarda el correo actual y si es administrador en un bundle que guardará para ser cargado en el
    //fragmento AlbumFragment
    private fun passData(){
        val bundle = Bundle()
        if(mAuth!!.currentUser!!.email == "administrator@gmail.com"){
            bundle.putBoolean("admin", true)
        }else{
            bundle.putBoolean("admin", false)
        }

        bundle.putString("correoActual", currentEmail)
        val fr = AlbumFragment()
        fr.arguments = bundle
        val fc = activity as ChangeListener?
        fc!!.replaceFragment(fr)
    }

    //Guarda los datos del perfil actual para mostrarlos en la siguiente pantalla
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

    //Devuelve un bundle con el correo actual y si es administrador o no
    private fun passData(admin: Boolean): Bundle{
        val bundle = Bundle()
        bundle.putString("correcoActual", currentEmail)
        bundle.putBoolean("admin", admin)
        return bundle
    }
}
