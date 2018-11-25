package com.example.alejandro.fshare.fragments


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.alejandro.fshare.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.AuthResult
import com.google.android.gms.tasks.Task
import android.support.design.widget.TextInputEditText
import android.support.design.widget.TextInputLayout
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.widget.Button
import com.example.alejandro.fshare.ChangeListener
import com.example.alejandro.fshare.UserActivity
import com.example.alejandro.fshare.model.Code
import com.example.alejandro.fshare.model.User
import com.google.firebase.database.*
import java.util.regex.Pattern


class SignInEmailFragment : Fragment() {

    private var mAuth: FirebaseAuth? = null
    private var database: FirebaseDatabase? = null

    private var signInButton: Button? = null
    private var signUpButton: Button? = null


    private var emailLayout: TextInputLayout? = null
    private var passwordLayout: TextInputLayout? = null
    private var phoneLayout: TextInputLayout? = null
    private var nameLayout: TextInputLayout? = null

    private var emailText: TextInputEditText? = null
    private var passwordText: TextInputEditText? = null
    private var phoneText: TextInputEditText? = null
    private var nameText: TextInputEditText? = null

    private var referenceUser: DatabaseReference? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_sign_in_email, container, false)

        database = FirebaseDatabase.getInstance()
        referenceUser = database!!.getReference("user")

        signInButton = view.findViewById(R.id.button_SignInUser)
        signUpButton = view.findViewById(R.id.button_createUser)


        emailLayout = view.findViewById(R.id.email_layout)
        passwordLayout = view.findViewById(R.id.password_layout)
        nameLayout = view.findViewById(R.id.name_layout)
        phoneLayout = view.findViewById(R.id.phone_layout)


        emailText = view.findViewById(R.id.email)
        passwordText = view.findViewById(R.id.password)
        nameText = view.findViewById(R.id.name)
        phoneText = view.findViewById(R.id.phone)

        mAuth = FirebaseAuth.getInstance()



        signInButton!!.setOnClickListener {

            validarDatos(0)
        }

        signUpButton!!.setOnClickListener {
            validarDatos(1)
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

        // Inflate the layout for this fragment
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

    private fun validarDatos(tipo: Int) {
        val pass = passwordLayout!!.editText!!.text.toString()
        val correo = emailLayout!!.editText!!.text.toString()
        val name = nameLayout!!.editText!!.text.toString()
        val phone = phoneLayout!!.editText!!.text.toString()
        val a = esNombreValido(name)
        val b = esTelefonoValido(phone)
        val e = esCorreoValido(correo)

        if (a && b && e) {
            if(tipo == 0) {

                signIn(pass, correo)
            }else{
                checkUser(pass, correo)
            }
        }

    }

    private fun signUp(pass: String, correo: String) {
        mAuth!!.createUserWithEmailAndPassword(correo, pass).addOnCompleteListener { task: Task<AuthResult> ->
            if (task.isSuccessful) {
                //Registration OK
                val fr = UserActivity()
                val fc = activity as ChangeListener?
                fc!!.replaceActivity(fr)
            } else {
                //Registration error
            }
        }
    }

        private fun signIn(pass: String, correo: String) {
        mAuth!!.signInWithEmailAndPassword(correo, pass).addOnCompleteListener { task: Task<AuthResult> ->
            if (task.isSuccessful) {
                //Registration OK
                val fr = UserActivity()
                val fc = activity as ChangeListener?
                fc!!.replaceActivity(fr)
            } else {
                //Registration error
            }
        }


    }

    private fun checkUser(pass: String, correo: String){
        var repetido = false
        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (ds in dataSnapshot.children) {
                    if(ds.child("correo").getValue(String::class.java) == correo){
                        repetido = true
                        break
                    }

                }
                if(!repetido) {
                    database!!.reference.setValue("users")
                    val key = database!!.getReference("users").push().key
                    val codigo = Code("correo", pass, "")
                    val usuario = User(nameLayout!!.editText!!.text.toString(), phoneLayout!!.editText!!.text.toString(), emailLayout!!.editText!!.text.toString(), codigo)
                    database!!.getReference("users").child(key!!).setValue(usuario)
                    signUp(pass, correo)
                }

            }

            override fun onCancelled(databaseError: DatabaseError) {}
        }
        referenceUser!!.addListenerForSingleValueEvent(valueEventListener)
    }
}
