package com.example.alejandro.fshare.fragments

import android.os.Bundle
import android.support.design.widget.TextInputEditText
import android.support.design.widget.TextInputLayout
import android.support.v4.app.Fragment
import android.support.v7.widget.Toolbar
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.view.*
import android.widget.Button
import android.widget.Toast
import com.example.alejandro.fshare.*
import com.example.alejandro.fshare.R
import com.example.alejandro.fshare.model.Photo
import com.example.alejandro.fshare.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.*
import java.util.regex.Pattern
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference


class UserDataFragment : Fragment() {

    private var mAuth: FirebaseAuth? = null

    private var database: FirebaseDatabase? = null
    private var storage: FirebaseStorage? = null

    private var deleteUserButton: Button? = null
    private var modUserButton: Button? = null
    private var changePasswordButton: Button? = null
    private var changeEmailButton: Button? = null


    private var emailLayout: TextInputLayout? = null
    private var passwordLayout: TextInputLayout? = null
    private var phoneLayout: TextInputLayout? = null
    private var nameLayout: TextInputLayout? = null

    private var emailText: TextInputEditText? = null
    private var passwordText: TextInputEditText? = null
    private var phoneText: TextInputEditText? = null
    private var nameText: TextInputEditText? = null


    private var nombre: String? = null
    private var password: String? = null
    private var telefono: String? = null
    private var correo: String? = null
    private var currentEmail: String? = null

    private var referenceUserDelete: DatabaseReference? = null
    private var referenceUserChangeEmail: DatabaseReference? = null
    private var referenceUserChangePassword: DatabaseReference? = null
    private var referenceUserModify: DatabaseReference? = null
    private var referenceUserProfile: DatabaseReference? =  null
    private var referenceUserPhotoModify: DatabaseReference? = null
    private var referenceUserPhotoDelete: DatabaseReference? = null

    private var storageDeletePhoto: StorageReference? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_user_data, container, false)

        setHasOptionsMenu(true)
        val toolbar = view.findViewById<Toolbar>(R.id.profileToolbar)

        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()


        deleteUserButton = view.findViewById(R.id.button_deleteUser)
        modUserButton = view.findViewById(R.id.button_modifiedUser)
        changePasswordButton = view.findViewById(R.id.button_modifiedPassword)
        changeEmailButton = view.findViewById(R.id.button_modifiedEmail)


        emailLayout = view.findViewById(R.id.emailUser_layout)
        passwordLayout = view.findViewById(R.id.passwordUser_layout)
        nameLayout = view.findViewById(R.id.nameUser_layout)
        phoneLayout = view.findViewById(R.id.phoneUser_layout)


        emailText = view.findViewById(R.id.emailUser)
        passwordText = view.findViewById(R.id.passwordUser)
        nameText = view.findViewById(R.id.nameUser)
        phoneText = view.findViewById(R.id.phoneUser)

        mAuth = FirebaseAuth.getInstance()

        val bundle = this.arguments
        if (bundle != null) {
            nombre = bundle.getString("Nombre")
            password = bundle.getString("Password")
            telefono = bundle.getString("Telefono")
            correo = bundle.getString("Correo")
        }

        currentEmail = correo

        nameText!!.setText(nombre)
        passwordText!!.setText(password)
        phoneText!!.setText(telefono)
        emailText!!.setText(correo)

        deleteUserButton!!.setOnClickListener {
            referenceUserDelete = database!!.reference.child("user")
            borrarUsuario()
        }

        modUserButton!!.setOnClickListener {
            validarDatos(1)
        }

        changeEmailButton!!.setOnClickListener {
            validarDatos(2)
        }



        changePasswordButton!!.setOnClickListener {
            validarDatos(3)

        }

        //Si es administrador, se esconden algunos botones y se asocia una toolbar diferente
        //de la toolbar del usuario
        if(mAuth!!.currentUser!!.email == "administrator@gmail.com"){
            deleteUserButton!!.visibility = View.GONE
            changeEmailButton!!.visibility = View.GONE
            changePasswordButton!!.visibility = View.GONE
            (activity as AdministratorActivity).setSupportActionBar(toolbar)
        }else{
            (activity as UserActivity).setSupportActionBar(toolbar)
        }

        nameText!!.addTextChangedListener(
                object : TextWatcher {
                    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                    }

                    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                        esNombreValido(s.toString())
                    }

                    override fun afterTextChanged(p0: Editable?) {

                    }


                })

        phoneText!!.addTextChangedListener(
                object : TextWatcher {
                    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                    }

                    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                        esTelefonoValido(s.toString())
                    }

                    override fun afterTextChanged(p0: Editable?) {

                    }


                })



        emailText!!.addTextChangedListener(
                object : TextWatcher {
                    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                    }

                    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                        esCorreoValido(s.toString())
                    }

                    override fun afterTextChanged(p0: Editable?) {

                    }


                })


        passwordText!!.addTextChangedListener(
                object : TextWatcher {
                    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                    }

                    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                        passwordLayout!!.error = null
                    }

                    override fun afterTextChanged(p0: Editable?) {

                    }


                })

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
                    referenceUserProfile = database!!.reference.child("user")
                    accederPerfil()
                    true
                }

                R.id.action_back-> {
                    //Vuelve al fragment anterior
                    val fr = AlbumFragment()
                    fr.arguments = passData(false, mAuth!!.currentUser!!.email!!)
                    val fc = activity as ChangeListener?
                    fc!!.replaceFragment(fr)
                    true
                }
                else -> super.onOptionsItemSelected(item)
            }
        }
    }

    private fun esCorreoValido(correo: String): Boolean {
        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            emailLayout!!.error = resources.getString(R.string.CorreoInvalido)
            return false
        } else {
            emailLayout!!.error = null
        }

        return true
    }

    private fun esNombreValido(nombre: String): Boolean {
        val patron = Pattern.compile("^[a-zA-Z0-9 ]+$")
        if (!patron.matcher(nombre).matches() || nombre.length > 30) {
            nameLayout!!.error = resources.getString(R.string.NombreInvalido)
            return false
        } else {
            nameLayout!!.error = null
        }

        return true
    }

    private fun esTelefonoValido(telefono: String): Boolean {
        if (!Patterns.PHONE.matcher(telefono).matches()) {
            phoneLayout!!.error = resources.getString(R.string.TelefonoInvalido)
            return false
        } else {
            phoneLayout!!.error = null
        }

        return true
    }

    private fun validarDatos(tipo: Int) {
        val pass = passwordLayout!!.editText!!.text.toString()
        val correo = emailLayout!!.editText!!.text.toString()
        val name = nameLayout!!.editText!!.text.toString()
        val phone = phoneLayout!!.editText!!.text.toString()
        val a = esNombreValido(name)
        val b = esTelefonoValido(phone)
        val e = esCorreoValido(correo)

        if (a && b && e) {
            when (tipo) {
                1 -> {
                    referenceUserModify = database!!.reference.child("user")
                    modificarUsuario(name, phone)
                }
                2 -> {
                    referenceUserChangeEmail = database!!.reference.child("user")
                    changeEmail(correo)
                }
                else -> {

                    referenceUserChangePassword = database!!.reference.child("user")
                    changePass(pass)
                }
            }


        }

    }

    //Borra un usuari de la base de datos
    private fun borrarUsuario(){
        val user = mAuth!!.currentUser
        var usuario: User

        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for( ds: DataSnapshot in dataSnapshot.children){
                    usuario = ds.getValue(User::class.java)!!
                    if(usuario.correo == correo) {
                        val key = ds.key
                        val password = usuario.password
                        //Borra las fotos y referencias de las fotos que el usuario ha subido
                        referenceUserDelete!!.child(key!!).removeValue()
                        referenceUserPhotoDelete = database!!.reference.child("photos")
                        deletePhoto(usuario.correo, password, user!!)
                    }
                }

            }

            override fun onCancelled(databaseError: DatabaseError) {}
        }

        referenceUserDelete!!.addListenerForSingleValueEvent(valueEventListener)
    }

    //Borra la foto de Storage y sus referencias en la base de datos
    private fun deletePhoto(correo: String, password: String, user: FirebaseUser){
        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for(ds:DataSnapshot in dataSnapshot.children){
                    val foto = ds.getValue(Photo::class.java)
                    if(foto!!.correo == correo){

                        storageDeletePhoto = storage!!.reference.child(foto.uri)
                        storageDeletePhoto!!.delete().addOnSuccessListener {

                            val keyPhoto = ds.key
                            referenceUserPhotoDelete!!.child(keyPhoto!!).removeValue()
                        }.addOnFailureListener {}
                    }
                }
                val credential = EmailAuthProvider
                        .getCredential(correo, password)
                user.reauthenticate(credential)
                        .addOnCompleteListener {
                            user.delete()
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {

                                            val fr = LoginActivity()
                                            val fc = activity as ChangeListener?
                                            fc!!.replaceActivity(fr)
                                        }
                                    }
                        }
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        }
        referenceUserPhotoDelete!!.addListenerForSingleValueEvent(valueEventListener)
    }

    //Modifica los datos del usuario
    private fun modificarUsuario(nameN: String, phoneN: String){
        val childUpdatesUser = HashMap<String,User>()
        val usuarioMod = User(nameN, phoneN,currentEmail!!, password!!)
        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for(ds:DataSnapshot in dataSnapshot.children){
                    val usuario = ds.getValue(User::class.java)
                    if(usuario!!.correo == currentEmail){
                        val key = ds.key
                        childUpdatesUser["/user/$key"] = usuarioMod
                        //Actualiza los valores
                        database!!.reference.updateChildren(childUpdatesUser as Map<String, Any>)
                        if(mAuth!!.currentUser!!.email == "administrator@gmail.com"){
                            val fr = UserListFragment()
                            val fc = activity as ChangeListener?
                            fc!!.replaceFragment(fr)
                        }else{
                            val fr = AlbumFragment()
                            fr.arguments = passData(false,  currentEmail!!)
                            val fc = activity as ChangeListener?
                            fc!!.replaceFragment(fr)
                        }
                    }
                }


            }

            override fun onCancelled(databaseError: DatabaseError) {}
        }
        referenceUserModify!!.addListenerForSingleValueEvent(valueEventListener)

    }

    //Cambia el correo
    private fun changeEmail(email: String){
        val user = mAuth!!.currentUser
        var existe = false
        var key = ""
        var usuarioAnterior = ""

        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (ds: DataSnapshot in dataSnapshot.children) {
                    val usuario = ds.getValue(User::class.java)
                    usuarioAnterior = usuario!!.correo
                    if (email == usuarioAnterior) {
                        existe = true
                    }
                    if (usuarioAnterior == user!!.email) {
                        key = ds.key!!
                    }
                }
                if (!existe) {
                    user!!.updateEmail(email)
                            .addOnCompleteListener { taskUpdate ->
                                if (taskUpdate.isSuccessful) {
                                    referenceUserChangeEmail!!.child(key).child("correo").setValue(email)
                                    referenceUserPhotoModify = database!!.reference.child("photos")
                                    changePhotoEmail(email, usuarioAnterior)
                                }

                            }
                } else {
                    Toast.makeText(context, resources.getString(R.string.correoEnUso), Toast.LENGTH_LONG).show()
                }
            }
                    override fun onCancelled(databaseError: DatabaseError) {}
                }

        referenceUserChangeEmail!!.addListenerForSingleValueEvent(valueEventListener)

    }

    //Cambia la contraseña
    private fun changePass(password: String){
        val user = mAuth!!.currentUser

        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for(ds:DataSnapshot in dataSnapshot.children){
                    val usuario = ds.getValue(User::class.java)
                    if(usuario!!.correo == user!!.email){
                        val key = ds.key
                        user.updatePassword(password)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        referenceUserChangePassword!!.child(key!!).child("password").setValue(password)
                                        val fr = AlbumFragment()
                                        fr.arguments = passData(false,  user.email!!)
                                        val fc = activity as ChangeListener?
                                        fc!!.replaceFragment(fr)
                                    }
                                }
                    }
                }


            }

            override fun onCancelled(databaseError: DatabaseError) {}
        }

        referenceUserChangePassword!!.addListenerForSingleValueEvent(valueEventListener)
    }

    //Carga el fragment que muestra el perfil
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

        referenceUserProfile!!.addListenerForSingleValueEvent(valueEventListener)
    }

    //Devuelve un bundle con el correo y un booleano en el que avisa si el usuario es administrador
    //o no
    private fun passData(admin: Boolean, correo: String): Bundle{
        val bundle = Bundle()
        bundle.putString("correcoActual", correo)
        bundle.putBoolean("admin", admin)
        return bundle
    }

    //Cambia el correo al que hacía referencia la foto
    private fun changePhotoEmail(email: String, emailAnterior: String){

        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for(ds:DataSnapshot in dataSnapshot.children){
                    val foto = ds.getValue(Photo::class.java)
                    if(emailAnterior == foto!!.correo){
                        val key = ds.key
                        referenceUserPhotoModify!!.child(key!!).child("correo").setValue(email)
                    }
                }
                val fr = AlbumFragment()
                fr.arguments = passData(false,  email)
                val fc = activity as ChangeListener?
                fc!!.replaceFragment(fr)
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        }

        referenceUserPhotoModify!!.addListenerForSingleValueEvent(valueEventListener)


    }


}
