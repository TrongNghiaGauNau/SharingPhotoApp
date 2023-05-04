package com.example.kltn.Fragments

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kltn.AccountSettingsActivity
import com.example.kltn.Adapter.UserAdapter
import com.example.kltn.Model.User
import com.example.kltn.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class SearchFragment : Fragment() {

    private var recyclerView:RecyclerView? = null
    private var userAdapter:UserAdapter? = null
    private var mUser:MutableList<User>? = null
    lateinit var search_text:EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view=  inflater.inflate(R.layout.fragment_search, container, false)
        //search_text = view.findViewById<EditText>(R.id.search_edit_text)
        search_text = view.findViewById<EditText>(R.id.search_edit_text)


        recyclerView = view.findViewById(R.id.recycler_view_search)
        recyclerView?.setHasFixedSize(true)
        recyclerView?.layoutManager = LinearLayoutManager(context)

        mUser = ArrayList()
        userAdapter = context?.let{ UserAdapter(it,mUser as ArrayList<User>,true) }
        recyclerView?.adapter = userAdapter


        search_text?.addTextChangedListener (object :TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }



            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if(search_text.text.toString()=="")
                {

                } else{
                    recyclerView?.visibility = View.VISIBLE

                    retrieveUsers()

                    searchUser(s.toString().lowercase())

                }
            }
            override fun afterTextChanged(s: Editable?) {
            }
        })

        return view
    }

    private fun searchUser(input: String) {
        val query = FirebaseDatabase.getInstance().getReference()
            .child("Users")
            .orderByChild("fullname")
            .startAt(input)
            .endAt(input+"\uf8ff")

        query.addValueEventListener(object:ValueEventListener {
            override fun onDataChange(datasnapshot: DataSnapshot) {
                mUser?.clear()
                for(snapshot in datasnapshot.children ){
                    val user = snapshot.getValue(User::class.java)
                    if(user!=null)
                    {
                        mUser?.add(user)
                    }
                }
                userAdapter?.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun retrieveUsers() {
        val userRef = FirebaseDatabase.getInstance().getReference().child("Users")
        userRef.addValueEventListener(object:ValueEventListener {
            override fun onDataChange(datasnapshot: DataSnapshot) {
                if(search_text?.text.toString()==""){
                    mUser?.clear()
                    for(snapshot in datasnapshot.children ){
                        val user = snapshot.getValue(User::class.java)
                        if(user!=null){
                            mUser?.add(user)
                        }
                    }
                    userAdapter?.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }


}