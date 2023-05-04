package com.example.kltn

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.example.kltn.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

lateinit var binding2:ActivitySignUpBinding
class SignUpActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        binding2 = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding2.root)

        binding2.signinLinkBtn.setOnClickListener {
            startActivity(Intent(this,SignInActivity::class.java))
        }

        binding2.signupBtn.setOnClickListener {
            CreateAccout()
        }
    }

    private fun CreateAccout() {
        val fullname = binding2.fullnameSignup.text.toString()
        val username = binding2.usernameSignup.text.toString()
        val email = binding2.emailSignup.text.toString()
        val password = binding2.passwordSignup.text.toString()

        when{
            TextUtils.isEmpty(fullname) -> Toast.makeText(this,"full name is required",Toast.LENGTH_SHORT).show()
            TextUtils.isEmpty(username) -> Toast.makeText(this,"username is required",Toast.LENGTH_SHORT).show()
            TextUtils.isEmpty(email) -> Toast.makeText(this,"email is required",Toast.LENGTH_SHORT).show()
            TextUtils.isEmpty(password) -> Toast.makeText(this,"passwordis required",Toast.LENGTH_SHORT).show()
            else -> {
                val progressDialog = ProgressDialog(this@SignUpActivity)
                progressDialog.setTitle("SignUp")
                progressDialog.setMessage("Please wait, this may take a while...")
                progressDialog.setCanceledOnTouchOutside(false)
                progressDialog.show()

                val mAuth:FirebaseAuth = FirebaseAuth.getInstance()

                mAuth.createUserWithEmailAndPassword(email,password)
                    .addOnCompleteListener{task ->
                        if(task.isSuccessful)
                        {
                            saveUserInfo(fullname,username,email,progressDialog)
                        }
                        else
                        {
                            val message = task.exception!!.toString()
                            Toast.makeText(this,"Error: $message",Toast.LENGTH_SHORT)
                            mAuth.signOut()
                            progressDialog.dismiss()
                        }
                    }
            }
        }

    }

    private fun saveUserInfo(fullname: String, username: String, email: String,progressDialog : ProgressDialog) {
        val currentUserID = FirebaseAuth.getInstance().currentUser!!.uid
        val usersRef: DatabaseReference = FirebaseDatabase.getInstance().reference.child("Users")

        val userMap = HashMap<String,Any>()
        userMap["uid"]=currentUserID
        userMap["fullname"]=fullname.toLowerCase()
        userMap["username"]=username.toLowerCase()
        userMap["email"]=email
        userMap["bio"]="Toi la Bui Trong Nghia"
        userMap["image"]="https://firebasestorage.googleapis.com/v0/b/kltn-6b045.appspot.com/o/Defaut%20Image%2Fprofile.png?alt=media&token=38054a02-b8a9-4731-9fe4-05d03303ccf7"

        usersRef.child(currentUserID).setValue(userMap)
            .addOnCompleteListener{ task ->
                if(task.isSuccessful)
                {
                    progressDialog.dismiss()
                    Toast.makeText(this,"Account has been created successfully",Toast.LENGTH_SHORT).show()

                    FirebaseDatabase.getInstance().reference
                        .child("Follow").child(currentUserID)
                        .child("Following").child(currentUserID)
                        .setValue(true)

                    val intent = Intent(this@SignUpActivity,MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish()
                }
                else
                {
                    val message = task.exception!!.toString()
                    Toast.makeText(this,"Error: $message",Toast.LENGTH_SHORT)
                    FirebaseAuth.getInstance().signOut()
                    progressDialog.dismiss()
                }

            }
    }
}