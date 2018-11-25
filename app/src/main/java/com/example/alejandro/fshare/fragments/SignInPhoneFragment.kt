package com.example.alejandro.fshare.fragments


import android.os.Bundle
import android.support.design.widget.TextInputEditText
import android.support.design.widget.TextInputLayout
import android.support.v4.app.Fragment
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.example.alejandro.fshare.ChangeListener

import com.example.alejandro.fshare.R
import java.util.concurrent.TimeUnit
import com.example.alejandro.fshare.LoginActivity
import com.example.alejandro.fshare.UserActivity
import com.example.alejandro.fshare.model.Code
import com.example.alejandro.fshare.model.User
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.database.*
import java.util.regex.Pattern


class SignInPhoneFragment : Fragment() {

    private var mAuth: FirebaseAuth? = null
    private var database: FirebaseDatabase? = null

    private var verificationPId: String? = null
    private var mResendToken: PhoneAuthProvider.ForceResendingToken? = null

    private var signInButton: Button? = null
    private var signUpButton: Button? = null
    private var resendCode: Button? = null

    private var nameLayout: TextInputLayout? = null
    private var emailLayout: TextInputLayout? = null
    private var phoneLayout: TextInputLayout? = null
    private var codeLayout: TextInputLayout? = null

    private var nameText: TextInputEditText? = null
    private var emailText: TextInputEditText? = null
    private var phoneText: TextInputEditText? = null
    private var codeText: TextInputEditText? = null

    private var code: String? = null

    private var referenceUser: DatabaseReference? = null
    private var referenceUserExist: DatabaseReference? = null

    private lateinit var mCallbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_sign_in_phone, container, false)

        database = FirebaseDatabase.getInstance()
        referenceUser = database!!.getReference("user")
        referenceUserExist = database!!.getReference("user")

        signInButton = view.findViewById(R.id.button_SignInUser)
        signUpButton = view.findViewById(R.id.button_createUser)
        resendCode = view.findViewById(R.id.button_resendCode)

        nameLayout = view.findViewById(R.id.name_layout)
        emailLayout = view.findViewById(R.id.email_layout)
        phoneLayout = view.findViewById(R.id.phone_layout)
        codeLayout = view.findViewById(R.id.code_layout)

        nameText = view.findViewById(R.id.name)
        emailText = view.findViewById(R.id.email)
        phoneText = view.findViewById(R.id.phone)
        codeText = view.findViewById(R.id.code)

        mAuth = FirebaseAuth.getInstance()

        signInButton!!.setOnClickListener {

            validarDatos(0)
        }

        signUpButton!!.setOnClickListener {
            validarDatos(1)
        }

        resendCode!!.setOnClickListener {
            resendVerificationCode(phoneLayout!!.editText!!.text.toString(), mResendToken)
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

        emailText!!.addTextChangedListener(
                object : TextWatcher {
                    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                    }

                    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                        esTelefonoValido(s.toString())
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

        mCallbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.
                //Log.d(TAG, "onVerificationCompleted:$credential")

                checkUser(emailLayout!!.editText!!.text.toString(),phoneLayout!!.editText!!.text.toString())

            }

            override fun onVerificationFailed(e: FirebaseException) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                //Log.w(TAG, "onVerificationFailed", e)

                if (e is FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                    // ...
                } else if (e is FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    // ...
                }

                // Show a message and update the UI
                // ...
            }

            override fun onCodeSent(verificationId: String?,
                                    token: PhoneAuthProvider.ForceResendingToken) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                //Log.d(TAG, "onCodeSent:" + verificationId!!)

                // Save verification ID and resending token so we can use them later
                verificationPId = verificationId
                mResendToken = token



            }
        }

        // Inflate the layout for this fragment
        return view
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

    private fun esCorreoValido(correo: String): Boolean {
        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            emailLayout!!.error = "Correo electrónico inválido"
            return false
        } else {
            emailLayout!!.error = null
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
        val telefono = phoneLayout!!.editText!!.text.toString()
        val nombre = nameLayout!!.editText!!.text.toString()
        val correo = emailLayout!!.editText!!.text.toString()
        val a = esNombreValido(nombre)
        val b = esCorreoValido(correo)
        val e = esTelefonoValido(telefono)

        if (a && b && e) {
            if(tipo == 0) {
                checkUserForLogin(telefono)
            }else{
                signUp(telefono)
            }
        }

    }

    private fun resendVerificationCode(phoneNumber: String,
                                       token: PhoneAuthProvider.ForceResendingToken?) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber, // Phone number to verify
                60, // Timeout duration
                TimeUnit.SECONDS, // Unit of timeout
                activity as LoginActivity, // Activity (for callback binding)
                mCallbacks, // OnVerificationStateChangedCallbacks
                token)             // ForceResendingToken from callbacks
    }

    private fun signUp(telefono: String) {

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                telefono,      // Phone number to verify
                60,               // Timeout duration
                TimeUnit.SECONDS, // Unit of timeout
                activity as LoginActivity,             // Activity (for callback binding)
                mCallbacks)       // OnVerificationStateChangedCallbacks
    }

    private fun signIn(credential: PhoneAuthCredential) {
        mAuth!!.signInWithCredential(credential)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val fr = UserActivity()
                        val fc = activity as ChangeListener?
                        fc!!.replaceActivity(fr)
                        if (task.exception is FirebaseAuthInvalidCredentialsException) {
                            // The verification code entered was invalid

                        }
                        // Sign in failed
                    }
                }
    }



    private fun checkUser(correo:String, phone: String){
        var repetido = false
        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (ds in dataSnapshot.children) {
                    val userAux = ds.getValue(User::class.java)
                    if(userAux!!.correo == correo && userAux.codigo.tipo == "telefono" && userAux.telefono == phone){
                        repetido = true
                        break
                    }

                }
                if(!repetido) {
                    database!!.reference.setValue("users")
                    val key = database!!.getReference("users").push().key
                    val codigo = Code("telefono", codeLayout!!.editText!!.text.toString(), verificationPId)
                    val usuario = User(nameLayout!!.editText!!.text.toString(), phoneLayout!!.editText!!.text.toString(), emailLayout!!.editText!!.text.toString(), codigo)
                    database!!.getReference("users").child(key!!).setValue(usuario)
                    signIn(PhoneAuthProvider.getCredential(verificationPId!!, code!!))
                }

            }

            override fun onCancelled(databaseError: DatabaseError) {}
        }
        referenceUser!!.addListenerForSingleValueEvent(valueEventListener)
    }

    private fun checkUserForLogin(phone:String){
        var repetido = false
        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (ds in dataSnapshot.children) {
                    val userAux = ds.getValue(User::class.java)
                    if(userAux!!.codigo.tipo == "telefono" && userAux.telefono == phone){
                        verificationPId = userAux.codigo.verificationPId
                        code = userAux.codigo.codeA
                        repetido = true
                        break
                    }

                }
                if(repetido){
                    signIn(PhoneAuthProvider.getCredential(verificationPId!!, code!!))
                }

            }

            override fun onCancelled(databaseError: DatabaseError) {}
        }
        referenceUserExist!!.addListenerForSingleValueEvent(valueEventListener)
    }
}
