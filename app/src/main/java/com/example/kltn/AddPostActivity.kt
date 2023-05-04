package com.example.kltn

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.example.kltn.databinding.ActivityAccountSettingsBinding
import com.example.kltn.databinding.ActivityAddPostBinding
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.theartofdev.edmodo.cropper.CropImage

lateinit var binding4: ActivityAddPostBinding
class AddPostActivity : AppCompatActivity() {

    private var myUrl = ""
    private var imageUri: Uri? = null
    private var storageProfilePicRef: StorageReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_post)

        binding4 = ActivityAddPostBinding.inflate(layoutInflater)
        setContentView(binding4.root)

        storageProfilePicRef = FirebaseStorage.getInstance().reference.child("Post Pictures")

        binding4.saveNewPostBtn.setOnClickListener{
            uploadImage()
        }

        CropImage.activity()
            .setAspectRatio(1,1)
            .start(this@AddPostActivity)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK && data!=null)
        {
            val result = CropImage.getActivityResult(data)
            imageUri = result.uri
            binding4.imagePost.setImageURI(imageUri)
        }
    }

    private fun uploadImage() {
        when{
            imageUri == null -> Toast.makeText(this,"Please select image first", Toast.LENGTH_SHORT).show()
            TextUtils.isEmpty(binding4.descriptionPost.text.toString()) -> {
                Toast.makeText(this,"Please write full name first", Toast.LENGTH_SHORT).show()
            }
            else -> {
                val progressDialog = ProgressDialog(this)
                progressDialog.setTitle("Adding New Post")
                progressDialog.setMessage("Please wait, we are adding your picture post...")
                progressDialog.show()

                val fileref = storageProfilePicRef!!.child(System.currentTimeMillis().toString() + ".jpg")

                val uploadTask: StorageTask<*>
                uploadTask = fileref.putFile(imageUri!!)

                uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                    if(task.isSuccessful)
                    {
                        task.exception?.let {
                            throw it
                            progressDialog.dismiss()
                        }
                    }
                    return@Continuation fileref.downloadUrl
                }).addOnCompleteListener (OnCompleteListener<Uri> { task ->
                    if(task.isSuccessful)
                    {
                        val downloadUrl = task.result
                        myUrl = downloadUrl.toString()

                        val ref = FirebaseDatabase.getInstance().reference.child("Posts")
                        val postId = ref.push().key

                        val postMap = HashMap<String,Any>()
                        postMap["postid"]= postId!!
                        postMap["description"]= binding4.descriptionPost.text.toString().toLowerCase()
                        postMap["bio"]= FirebaseAuth.getInstance().currentUser!!.uid
                        postMap["image"]= myUrl

                        ref.child(postId).updateChildren(postMap)

                        Toast.makeText(this,"Post upload successfully",Toast.LENGTH_SHORT).show()

                        val intent = Intent(this@AddPostActivity,MainActivity::class.java)
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