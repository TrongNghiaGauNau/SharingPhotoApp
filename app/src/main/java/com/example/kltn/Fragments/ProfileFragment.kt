package com.example.kltn.Fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.example.kltn.AccountSettingsActivity
import com.example.kltn.Model.User
import com.example.kltn.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso


class ProfileFragment : Fragment() {
    private lateinit var profileID:String
    private lateinit var firebaseUser:FirebaseUser

    private lateinit var edit_account_setting_btn:Button
    private lateinit var total_followers:TextView
    private lateinit var total_following:TextView
    private lateinit var pro_image_profile_frag: ImageView
    private lateinit var profile_fragment_username: TextView
    private lateinit var full_name_profile_frag: TextView
    private lateinit var bio_profile_frag: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        edit_account_setting_btn = view.findViewById<Button>(R.id.edit_account_setting_btn)
        total_followers = view.findViewById<TextView>(R.id.total_followers)
        total_following = view.findViewById<TextView>(R.id.total_following)
        pro_image_profile_frag = view.findViewById<ImageView>(R.id.pro_image_profile_frag)
        profile_fragment_username = view.findViewById<TextView>(R.id.profile_fragment_username)
        full_name_profile_frag = view.findViewById<TextView>(R.id.full_name_profile_frag)
        bio_profile_frag = view.findViewById<TextView>(R.id.bio_profile_frag)


        val pref = context?.getSharedPreferences("PREFS",Context.MODE_PRIVATE)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        if(pref != null){
            this.profileID = pref.getString("profileID","none")!!
        }
        if(profileID==firebaseUser.uid){
            edit_account_setting_btn.text = "Edit Profile"
        }
        else if(profileID!=firebaseUser.uid){
            checkFollowAndFollowing()
        }


        edit_account_setting_btn.setOnClickListener {
            val getButtonText = edit_account_setting_btn.text.toString()

            when {
                getButtonText == "Edit Profile" -> startActivity( Intent(context,AccountSettingsActivity::class.java))
                getButtonText == "Follow" -> {
                    firebaseUser?.uid.let { it ->
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(it.toString())
                            .child("Following").child(profileID)
                            .setValue(true)
                    }
                    firebaseUser?.uid.let { it ->
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(profileID)
                            .child("Followers").child(it.toString())
                            .setValue(true)
                    }
                }

                getButtonText == "Following" -> {
                    firebaseUser?.uid.let { it ->
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(it.toString())
                            .child("Following").child(profileID)
                            .removeValue()
                    }
                    firebaseUser?.uid.let { it ->
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(profileID)
                            .child("Followers").child(it.toString())
                            .removeValue()
                    }
                }
            }
        }

        getFollower()
        getFollowings()
        userInfo()

        return view
    }

    private fun checkFollowAndFollowing() {
        val followingRef = firebaseUser?.uid.let { it ->
            FirebaseDatabase.getInstance().reference
                .child("Follow").child(it.toString())
                .child("Following")
        }
        if(followingRef != null){
            followingRef.addValueEventListener(object : ValueEventListener{
                override fun onDataChange(p0: DataSnapshot) {
                    if(p0.child(profileID).exists()){
                        edit_account_setting_btn.text = "Following"
                    }
                    else{
                        edit_account_setting_btn.text = "Follow"
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
        }
    }

    private fun getFollower(){
        val followersRef = FirebaseDatabase.getInstance().reference
                .child("Follow").child(profileID)
                .child("Followers")

        followersRef.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                if(p0.exists()){
                    total_followers.text = p0.childrenCount.toString()
                }
            }
            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun getFollowings(){
        val followersRef =  FirebaseDatabase.getInstance().reference
                .child("Follow").child(profileID)
                .child("Followings")

        followersRef.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                if(p0.exists()){
                    total_following.text = p0.childrenCount.toString()
                }
            }
            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun userInfo(){
        val userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(profileID)

        userRef.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                if(p0.exists()){
                    val user = p0.getValue<User>(User::class.java)

                    Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile).into(pro_image_profile_frag)
                    profile_fragment_username.text = user!!.getUsername()
                    full_name_profile_frag.text = user!!.getFullname()
                    bio_profile_frag.text = user!!.getBio()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    override fun onStop() {
        super.onStop()

        val pref = context?.getSharedPreferences("PREFS",Context.MODE_PRIVATE)?.edit()
        pref?.putString("profileID",firebaseUser.uid)
        pref?.apply()
    }

    override fun onPause() {
        super.onPause()

        val pref = context?.getSharedPreferences("PREFS",Context.MODE_PRIVATE)?.edit()
        pref?.putString("profileID",firebaseUser.uid)
        pref?.apply()
    }

    override fun onDestroy() {
        super.onDestroy()

        val pref = context?.getSharedPreferences("PREFS",Context.MODE_PRIVATE)?.edit()
        pref?.putString("profileID",firebaseUser.uid)
        pref?.apply()
    }
}