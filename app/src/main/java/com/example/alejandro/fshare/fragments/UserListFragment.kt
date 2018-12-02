package com.example.alejandro.fshare.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.*
import com.example.alejandro.fshare.AdministratorActivity
import com.example.alejandro.fshare.ChangeListener
import com.example.alejandro.fshare.LoginActivity

import com.example.alejandro.fshare.R
import com.example.alejandro.fshare.adapter.UserListHolder
import com.example.alejandro.fshare.adapter.ClickListenerUser
import com.example.alejandro.fshare.model.User
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*


class UserListFragment : Fragment(), ClickListenerUser {

    private var database: FirebaseDatabase? = null
    lateinit var mDatabase: DatabaseReference

    private var mAdapter: FirebaseRecyclerAdapter<User, UserListHolder>? = null
    private var recyclerUserList: RecyclerView? = null
    private var query: Query? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_user_list, container, false)

        setHasOptionsMenu(true)
        val toolbar = view.findViewById<Toolbar>(R.id.adminToolbar)
        (activity as AdministratorActivity).setSupportActionBar(toolbar)

        database = FirebaseDatabase.getInstance()
        mDatabase = database!!.reference
        // Inflate the layout for this fragment
        recyclerUserList = view.findViewById(R.id.rvUserList)


        query = mDatabase.child("user")
        val options = FirebaseRecyclerOptions.Builder<User>()
                .setQuery(query as DatabaseReference, User::class.java)
                .build()

        mAdapter = object : FirebaseRecyclerAdapter<User, UserListHolder>(options) {


            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserListHolder {
                val viewHolder = LayoutInflater.from(parent.context)
                        .inflate(R.layout.user_format_holder, parent, false)

                return UserListHolder(viewHolder)
            }

            override fun onBindViewHolder(holder: UserListHolder, position: Int, user: User) {
                holder.bindUser(user)
                holder.cardUser.setOnClickListener {
                    elementClicked(holder.adapterPosition, holder.viewAux, user)
                }


            }

        }
        inicializarReciclerView()

        return view
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        activity!!.menuInflater.inflate(R.menu.admin_options_menu, menu)
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

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun elementClicked(id: Int, v: View, user:User) {
        val bundle = Bundle()

        bundle.putString("Nombre", user.nombre)
        bundle.putString("Password", user.password)
        bundle.putString("Telefono", user.telefono)
        bundle.putString("Correo", user.correo)

       val fr = UserDataFragment()
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

    private fun inicializarReciclerView(){
        recyclerUserList!!.adapter = mAdapter
        recyclerUserList!!.layoutManager = LinearLayoutManager(activity)
        recyclerUserList!!.itemAnimator = DefaultItemAnimator()
    }
}
