package com.example.alejandro.fshare.fragments


import android.content.Intent
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
import android.util.Log
import android.util.Patterns
import android.widget.Button
import android.widget.Toast
import com.example.alejandro.fshare.AdministratorActivity
import com.example.alejandro.fshare.ChangeListener
import com.example.alejandro.fshare.UserActivity
import com.google.firebase.database.*


class SignInEmailFragment : Fragment() {

    private var mAuth: FirebaseAuth? = null

    private var database: FirebaseDatabase? = null

    private var signInButton: Button? = null
    private var signUpButton: Button? = null
    private var atrasButton: Button? = null


    private var emailLayout: TextInputLayout? = null
    private var passwordLayout: TextInputLayout? = null

    private var emailText: TextInputEditText? = null
    private var passwordText: TextInputEditText? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_sign_in_email, container, false)

        database = FirebaseDatabase.getInstance()


        signInButton = view.findViewById(R.id.button_SignInUser)
        signUpButton = view.findViewById(R.id.button_createUser)
        atrasButton = view.findViewById(R.id.button_atras)


        emailLayout = view.findViewById(R.id.email_layout)
        passwordLayout = view.findViewById(R.id.password_layout)


        emailText = view.findViewById(R.id.email)
        passwordText = view.findViewById(R.id.password)


        mAuth = FirebaseAuth.getInstance()
        signInButton!!.setOnClickListener {

            validarDatos()
        }

        signUpButton!!.setOnClickListener {
            val fr = SignUpEmailFragment()
            val fc = activity as ChangeListener?
            fc!!.replaceFragment(fr)
        }

        atrasButton!!.setOnClickListener {
            val fr = LoginSelectionFragment()
            val fc = activity as ChangeListener?
            fc!!.replaceFragment(fr)
        }

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
            emailLayout!!.error = resources.getString(R.string.CorreoInvalido)
            return false
        } else {
            emailLayout!!.error = null
        }

        return true
    }


    private fun validarDatos() {
        val pass = passwordLayout!!.editText!!.text.toString()
        val correo = emailLayout!!.editText!!.text.toString()
        val e = esCorreoValido(correo)

        if (e) {
            signIn(pass, correo)
        }

    }

    //Accede a la pantalla correspondiente
        private fun signIn(pass: String, correo: String) {
        mAuth!!.signInWithEmailAndPassword(correo, pass).addOnCompleteListener { task: Task<AuthResult> ->
            if (task.isSuccessful) {

                if(pass == "administrator" && correo == "administrator@gmail.com") {
                    val fr = AdministratorActivity()
                    val fc = activity as ChangeListener?
                    fc!!.replaceActivity(fr)
                }else{
                    val intent = Intent()
                    intent.putExtra("correoActual", correo)
                    intent.putExtra("admin", false)

                    val fr = UserActivity()
                    fr.intent = intent
                    val fc = activity as ChangeListener?
                    fc!!.replaceActivity(fr)
                }

            } else {
                Toast.makeText(this.context,resources.getString(R.string.falloRegistro), Toast.LENGTH_LONG).show()
            }
        }
    }
}

