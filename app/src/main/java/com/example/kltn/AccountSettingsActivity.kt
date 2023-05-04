package com.example.kltn

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.icu.number.NumberFormatter.with
import android.icu.number.NumberRangeFormatter.with
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.annotation.RequiresApi
import com.example.kltn.Model.User
import com.example.kltn.databinding.ActivityAccountSettingsBinding
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage

lateinit var binding3:ActivityAccountSettingsBinding
class AccountSettingsActivity : AppCompatActivity() {

    private lateinit var firebaseUser: FirebaseUser
    private var checker = ""
    private var myUrl = ""
    private var imageUri: Uri? = null
    private var storageProfilePicRef: StorageReference? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_settings)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        storageProfilePicRef = FirebaseStorage.getInstance().reference.child("Profile Pictures")

        binding3 = ActivityAccountSettingsBinding.inflate(layoutInflater)
        setContentView(binding3.root)

        binding3.logoutBtn.setOnClickListener {
            FirebaseAuth.getInstance().signOut()

            val intent = Intent(this@AccountSettingsActivity,SignInActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }

        binding3.changeImageTextBtn.setOnClickListener{
            checker = "clicked"
            CropImage.activity()
                .setAspectRatio(1,1)
                .start(this@AccountSettingsActivity)
        }

        binding3.saveInforProfileBtn.setOnClickListener{
            if(checker=="clicked")
            {
                uploadImageAndUpdateInfo()
            }
            else
            {
                updateUserInfoOnly()
            }
        }

        userInfo()
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK && data!=null)
        {
            val result = CropImage.getActivityResult(data)
            imageUri = result.uri
            binding3.profileImageViewProfileFragment.setImageURI(imageUri)
        }
    }

    private fun updateUserInfoOnly() {
        when {
            TextUtils.isEmpty(binding3.fullNameProfileFrag.text.toString()) -> {
                Toast.makeText(this,"Please write full name first", Toast.LENGTH_SHORT).show()
            }
            TextUtils.isEmpty(binding3.usernameProfileFrag.text.toString())  -> {
                Toast.makeText(this,"Please write user name first", Toast.LENGTH_SHORT).show()
            }
            TextUtils.isEmpty(binding3.bioProfileFrag.text.toString()) -> {
                Toast.makeText(this,"Please write bio first", Toast.LENGTH_SHORT).show()
            }
            else -> {
                val userRef = FirebaseDatabase.getInstance().reference.child("Users")

                val userMap = HashMap<String,Any>()
                userMap["fullname"]= binding3.fullNameProfileFrag.text.toString().toLowerCase()
                userMap["username"]= binding3.usernameProfileFrag.text.toString().toLowerCase()
                userMap["bio"]= binding3.bioProfileFrag.text.toString().toLowerCase()

                userRef.child(firebaseUser.uid).updateChildren(userMap)

                Toast.makeText(this,"Account Information has been updated successfully",Toast.LENGTH_SHORT).show()

                val intent = Intent(this@AccountSettingsActivity,MainActivity::class.java)
                //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }
        }
    }

    private fun userInfo(){
        val userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(firebaseUser.uid)

        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if(p0.exists()){
                    val user = p0.getValue<User>(User::class.java)

                    Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile).into(
                        binding3.profileImageViewProfileFragment)
                    binding3.usernameProfileFrag.setText(user!!.getUsername())
                    binding3.fullNameProfileFrag.setText(user!!.getFullname())
                    binding3.bioProfileFrag.setText(user!!.getBio())
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun uploadImageAndUpdateInfo()
    {
        when
        {
            imageUri == null -> Toast.makeText(this,"Please select image first", Toast.LENGTH_SHORT).show()
            TextUtils.isEmpty(binding3.fullNameProfileFrag.text.toString()) -> {
                Toast.makeText(this,"Please write full name first", Toast.LENGTH_SHORT).show()
            }
            TextUtils.isEmpty(binding3.usernameProfileFrag.text.toString())  -> {
                Toast.makeText(this,"Please write user name first", Toast.LENGTH_SHORT).show()
            }
            TextUtils.isEmpty(binding3.bioProfileFrag.text.toString()) -> {
                Toast.makeText(this,"Please write bio first", Toast.LENGTH_SHORT).show()
            }

            else -> {
                val progressDialog = ProgressDialog(this)
                progressDialog.setTitle("Account Settings")
                progressDialog.setMessage("Please wait, we are updating your profile...")
                progressDialog.show()

                val fileref = storageProfilePicRef!!.child(firebaseUser!!.uid + ".jpg")

                val uploadTask:StorageTask<*>
                uploadTask = fileref.putFile(imageUri!!)

                uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot,Task<Uri>> { task ->
                    if(task.isSuccessful)
                    {
                        task.exception?.let {
                            throw it
                            progressDialog.dismiss()
                        }
                    }
                    return@Continuation fileref.downloadUrl
                }).addOnCompleteListener (OnCompleteListener<Uri> {task ->
                    if(task.isSuccessful)
                    {
                        val downloadUrl = task.result
                        myUrl = downloadUrl.toString()

                        val ref = FirebaseDatabase.getInstance().reference.child("Users")

                        val userMap = HashMap<String,Any>()
                        userMap["fullname"]= binding3.fullNameProfileFrag.text.toString().toLowerCase()
                        userMap["username"]= binding3.usernameProfileFrag.text.toString().toLowerCase()
                        userMap["bio"]= binding3.bioProfileFrag.text.toString().toLowerCase()
                        userMap["image"]= myUrl

                        ref.child(firebaseUser.uid).updateChildren(userMap)

                        Toast.makeText(this,"Account Information has been updated successfully",Toast.LENGTH_SHORT).show()

                        val intent = Intent(this@AccountSettingsActivity,MainActivity::class.java)
                        startActivity(intent)
                        finish()
                        progressDialog.dismiss()
                    }
                    else
                    {
                        progressDialog.dismiss()
                    }
                } )
            }
        }
    }

}