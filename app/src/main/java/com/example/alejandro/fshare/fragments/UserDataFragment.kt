package com.example.alejandro.fshare.fragments


import android.os.Bundle
import android.support.design.widget.TextInputEditText
import android.support.design.widget.TextInputLayout
import android.support.v4.app.Fragment
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.example.alejandro.fshare.ChangeListener
import com.example.alejandro.fshare.LoginActivity
import com.example.alejandro.fshare.R
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
    private var referenceUserModify: DatabaseReference? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_user_data, container, false)

        database = FirebaseDatabase.getInstance()


        deleteUserButton = view.findViewById(R.id.button_deleteUser)
        modUserButton = view.findViewById(R.id.button_modifiedUser)


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
            referenceUserDelete = database!!.reference.child("user").child(mAuth!!.currentUser!!.email!!)
            borrarUsuario(mAuth!!.currentUser!!.email!!, passwordText!!.text.toString())
        }

        modUserButton!!.setOnClickListener {
            validarDatos()
        }

        if(mAuth!!.currentUser!!.email == "administrator@gmail.com"){
            deleteUserButton!!.visibility = View.GONE
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

    private fun validarDatos() {
        val pass = passwordLayout!!.editText!!.text.toString()
        val correo = emailLayout!!.editText!!.text.toString()
        val name = nameLayout!!.editText!!.text.toString()
        val phone = phoneLayout!!.editText!!.text.toString()
        val a = esNombreValido(name)
        val b = esTelefonoValido(phone)
        val e = esCorreoValido(correo)

        if (a && b && e) {
            referenceUserModify = database!!.reference.child("user").child(mAuth!!.currentUser!!.email!!)
            modificarUsuario(correo, name, phone, pass)

        }

    }

    private fun borrarUsuario(email:String, pass: String){
        val user = FirebaseAuth.getInstance().currentUser

        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val key = dataSnapshot.key
                database!!.reference.child("user").child(key!!).removeValue()
                val credential = EmailAuthProvider
                        .getCredential(email, pass)
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

            override fun onCancelled(databaseError: DatabaseError) {}
        }

        referenceUserDelete!!.addListenerForSingleValueEvent(valueEventListener)
    }

    private fun modificarUsuario(correo: String, name: String, phone: String, pass: String){
        val childUpdatesUser = HashMap<String,User>()
        val usuarioMod = User(name, phone,correo)

        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val key = dataSnapshot.key
                childUpdatesUser["/user/$key"] = usuarioMod
                database!!.reference.updateChildren(childUpdatesUser as Map<String, Any>)
                val fr = UserListFragment()
                val fc = activity as ChangeListener?
                fc!!.replaceFragment(fr)
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        }

        referenceUserModify!!.addListenerForSingleValueEvent(valueEventListener)
    }


}
