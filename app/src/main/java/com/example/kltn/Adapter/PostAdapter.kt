package com.example.kltn.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.example.kltn.Model.Post
import com.example.kltn.Model.User
import com.example.kltn.R
import com.example.kltn.binding3
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class PostAdapter(private val mContext:Context,
                  private val mPost: List<Post>):RecyclerView.Adapter<PostAdapter.ViewHolder>()
{
    private var firebaseUser: FirebaseUser? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.post_layout,parent,false)
        return PostAdapter.ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        firebaseUser = FirebaseAuth.getInstance().currentUser

        val post = mPost[position]

        Picasso.get().load(post.getPostimage()).into(holder.postImage)

        publisherInfo(holder.profileImage,holder.userName,holder.publisher,post.getPublisher())
    }

    override fun getItemCount(): Int {
        return mPost.size
    }


    class ViewHolder(@NonNull itemView: View):RecyclerView.ViewHolder(itemView){
        var profileImage:CircleImageView= itemView.findViewById(R.id.user_profile_image_post)
        var postImage:ImageView= itemView.findViewById(R.id.post_image_home)
        var likeButton:ImageView= itemView.findViewById(R.id.post_image_like_btn)
        var commentButton:ImageView= itemView.findViewById(R.id.post_image_comment_btn)
        var saveButton:ImageView = itemView.findViewById(R.id.post_save_comment_btn)
        var userName:TextView = itemView.findViewById(R.id.user_name_post)
        var likes:TextView= itemView.findViewById(R.id.likes)
        var publisher:TextView= itemView.findViewById(R.id.publisher)
        var description:TextView = itemView.findViewById(R.id.description)
        var comments:TextView= itemView.findViewById(R.id.comments)

    }

    private fun publisherInfo(profileImage: CircleImageView, userName: TextView, publisher: TextView, publisherID: String) {
        val userRef = FirebaseDatabase.getInstance().reference.child("Users").child(publisherID)

        userRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                if(p0.exists())
                {
                    val user = p0.getValue<User>(User::class.java)

                    Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile).into(profileImage)
                    userName.text = user!!.getUsername()
                    publisher.text = user!!.getFullname()

                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

}