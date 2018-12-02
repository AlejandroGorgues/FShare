package com.example.alejandro.fshare.fragments


import android.content.ContentValues.TAG
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
import com.example.alejandro.fshare.*
import com.example.alejandro.fshare.R
import com.example.alejandro.fshare.model.Photo
import com.example.alejandro.fshare.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.*
import java.util.regex.Pattern
import com.google.firebase.auth.EmailAuthProvider









class UserDataFragment : Fragment() {

    private var mAuth: FirebaseAuth? = null

    private var database: FirebaseDatabase? = null

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

    private var referenceUserDelete: DatabaseReference? = null
    private var referenceUserChangeEmail: DatabaseReference? = null
    private var referenceUserChangePassword: DatabaseReference? = null
    private var referenceUserModify: DatabaseReference? = null
    private var referenceUserProfile: DatabaseReference? =  null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_user_data, container, false)

        setHasOptionsMenu(true)

        database = FirebaseDatabase.getInstance()


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

        if(mAuth!!.currentUser!!.email == "administrator@gmail.com"){
            deleteUserButton!!.visibility = View.GONE
            changeEmailButton!!.visibility = View.GONE
            changePasswordButton!!.visibility = View.GONE
            val toolbar = view.findViewById<Toolbar>(R.id.profileToolbar)
            (activity as AdministratorActivity).setSupportActionBar(toolbar)
        }else{
            val toolbar = view.findViewById<Toolbar>(R.id.profileToolbar)
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
                    FirebaseAuth.getInstance().signOut()
                    val fr = LoginActivity()
                    val fc = activity as ChangeListener?
                    fc!!.replaceActivity(fr)
                    true
                }

                else -> super.onOptionsItemSelected(item)
            }
        }else{
            return when (item.itemId) {
                R.id.action_out-> {
                    FirebaseAuth.getInstance().signOut()
                    val fr = LoginActivity()
                    val fc = activity as ChangeListener?
                    fc!!.replaceActivity(fr)
                    true
                }
                R.id.action_show-> {

                    referenceUserProfile = database!!.reference.child("user")
                    accederPerfil()
                    true
                }
                else -> super.onOptionsItemSelected(item)
            }
        }
    }

    private fun esCorreoValido(correo: String): Boolean {
        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            emailLayout!!.error = "Correo electrónico inválido"
            return false
        } else {
            emailLayout!!.error = null
        }

        return true
    }

    private fun esNombreValido(nombre: String): Boolean {
        val patron = Pattern.compile("^[a-zA-Z ]+$")
        if (!patron.matcher(nombre).matches() || nombre.length > 30) {
            nameLayout!!.error = "Nombre inválido"
            return false
        } else {
            nameLayout!!.error = null
        }

        return true
    }

    private fun esTelefonoValido(telefono: String): Boolean {
        if (!Patterns.PHONE.matcher(telefono).matches()) {
            phoneLayout!!.error = "Teléfono inválido"
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
                    modificarUsuario(correo, name, phone, pass)
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

    private fun borrarUsuario(){
        val user = mAuth!!.currentUser
        var usuario: User

        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for( ds: DataSnapshot in dataSnapshot.children){
                    usuario = ds.getValue(User::class.java)!!
                    if(usuario.correo == correo) {
                        val key = dataSnapshot.key
                        val password = usuario.password
                        database!!.reference.child("user").child(key!!).removeValue()
                        val credential = EmailAuthProvider
                                .getCredential(usuario.correo, password)
                        user!!.reauthenticate(credential)
                                .addOnCompleteListener {
                                    user.delete()
                                            .addOnCompleteListener { task ->
                                                if (task.isSuccessful) {
                                                    val fr = LoginActivity()
                                                    val fc = activity as ChangeListener?
                                                    fc!!.replaceActivity(fr)
                                                    Log.e("aqui", "User account deleted.")
                                                }
                                            }
                                }
                    }
                }

            }

            override fun onCancelled(databaseError: DatabaseError) {}
        }

        referenceUserDelete!!.addListenerForSingleValueEvent(valueEventListener)
    }

    private fun modificarUsuario(correoN: String, nameN: String, phoneN: String, passN: String){
        val childUpdatesUser = HashMap<String,User>()
        val usuarioMod = User(nameN, phoneN,correoN, passN)

        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for(ds:DataSnapshot in dataSnapshot.children){
                    val usuario = ds.getValue(User::class.java)
                    if(usuario!!.correo == mAuth!!.currentUser!!.email){
                        val key = ds.key

                        childUpdatesUser["/user/$key"] = usuarioMod
                        database!!.reference.updateChildren(childUpdatesUser as Map<String, Any>)
                        if(mAuth!!.currentUser!!.email == "administrator@gmail.com"){
                            val fr = UserListFragment()
                            val fc = activity as ChangeListener?
                            fc!!.replaceFragment(fr)
                        }else{
                            val fr = AlbumFragment()
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

    private fun changeEmail(email: String){
        val user = mAuth!!.currentUser

        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for(ds:DataSnapshot in dataSnapshot.children){
                    val usuario = ds.getValue(User::class.java)
                    if(usuario!!.correo == mAuth!!.currentUser!!.email){
                        val key = ds.key
                        user!!.updateEmail(email)
                            .addOnCompleteListener { taskUpdate ->
                                if (taskUpdate.isSuccessful) {
                                    referenceUserChangeEmail!!.child(key!!).child("correo").setValue(email)
                                }
                            }
                    }
                }


            }

            override fun onCancelled(databaseError: DatabaseError) {}
        }

        referenceUserChangeEmail!!.addListenerForSingleValueEvent(valueEventListener)

    }

    private fun changePass(password: String){
        val user = mAuth!!.currentUser

        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for(ds:DataSnapshot in dataSnapshot.children){
                    val usuario = ds.getValue(User::class.java)
                    if(usuario!!.correo == mAuth!!.currentUser!!.email){
                        val key = ds.key
                        user!!.updatePassword(password)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        referenceUserChangePassword!!.child(key!!).child("password").setValue(password)
                                        val fr = AlbumFragment()
                                        val fc = activity as ChangeListener?
                                        fc!!.replaceFragment(fr)
                                        Log.d(TAG, "User password updated.")
                                    }
                                }
                    }
                }


            }

            override fun onCancelled(databaseError: DatabaseError) {}
        }

        referenceUserChangePassword!!.addListenerForSingleValueEvent(valueEventListener)
    }

    private fun accederPerfil(){
        val bundle = Bundle()

        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for( ds: DataSnapshot in dataSnapshot.children){
                    val usuario = ds.getValue(User::class.java)
                    if(usuario!!.correo == mAuth!!.currentUser!!.email!!){

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

}
