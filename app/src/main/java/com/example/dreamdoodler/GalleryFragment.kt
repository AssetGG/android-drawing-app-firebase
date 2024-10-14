package com.example.dreamdoodler

import android.app.AlertDialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.dreamdoodler.databinding.FragmentGalleryBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import java.io.ByteArrayOutputStream
import java.util.Date
import java.util.UUID


/**
 * Fragment to display the Images shared
 * by other users and also upload the current drawing
 */
class GalleryFragment : Fragment() {

    private lateinit var binding : FragmentGalleryBinding
    private val viewModel: DrawingViewModel by activityViewModels {
        DrawingViewModelFactory((requireActivity().application as DrawingApplication).repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGalleryBinding.inflate(inflater, container, false)

        displaySharedImages()

        binding.uploadButton.setOnClickListener {
            val bitmap = viewModel.bitmap.value
            val currentUser = Firebase.auth.currentUser

            if (bitmap != null && currentUser != null) {
                uploadImage(bitmap, currentUser.uid, "Placeholder Title"){
                    AlertDialog.Builder(context)
                        .setTitle("Success!")
                        .setMessage("Your drawing was uploaded to the Firebase.")
                        .setPositiveButton("OK") { dialog, which -> dialog.dismiss() }
                        .show()
                }
            } else {
                AlertDialog.Builder(context)
                    .setTitle("Upload Failed")
                    .setMessage("You need to be logged in to upload images.")
                    .setPositiveButton("OK") { dialog, which -> dialog.dismiss() }
                    .show()
            }
        }

        return binding.root
    }

    /**
     * Uploads an image to Firebase Storage and, on success, stores the image metadata in Firestore.
     *
     * @param bitmap
     * @param userId
     * @param imageTitle
     */
    private fun uploadImage(bitmap: Bitmap, userId: String, imageTitle: String, onComplete: () -> Unit) {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
        val data = baos.toByteArray()

        val storageRef = Firebase.storage.reference.child("images/$userId/${UUID.randomUUID()}.png")

        storageRef.putBytes(data).addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                val imageUrl = uri.toString()
                saveImageMetadata(imageTitle, userId, imageUrl, onComplete)
            }
        }.addOnFailureListener {
            Log.e("Upload Image", "Failure: ${it.message}")
        }
    }


    /**
     * Saves image metadata to Firestore. The metadata includes the image title,
     * the user ID of the author, the image URL, and the timestamp of the upload.
     *
     * @param title
     * @param userId
     * @param imageUrl
     */
    private fun saveImageMetadata(title: String, userId: String, imageUrl: String, onComplete: () -> Unit) {
        val db = Firebase.firestore

        val imageInfo = mapOf(
            "title" to title,
            "authorId" to userId,
            "time" to Date()
        )
        db.collection("users/").document(userId)
            .set(imageInfo)
            .addOnSuccessListener {
                Log.d("Firestore", "Image metadata saved successfully")
                onComplete()
            }
            .addOnFailureListener {
                Log.e("Firestore", "Failed to save image metadata", it)
            }

    }

    /**
     * Retrieves all of the images stored on the Firebase and
     * then creates an ImageView to display them.
     * If any of the images is clicked the user gets its copy on
     * their canvas.
     * Also contains the button to upload the
     */
    private fun displaySharedImages() {
        val storageRef = Firebase.storage.reference.child("images/")
        storageRef.listAll()
            .addOnSuccessListener { listResult ->
                listResult.prefixes.forEach { userFolder ->
                    userFolder.listAll()
                        .addOnSuccessListener { fileList ->
                            fileList.items.forEach { fileRef ->
                                fileRef.getBytes(Long.MAX_VALUE).addOnSuccessListener { bytes ->
                                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                    val imageView = ImageView(context).apply {
                                        layoutParams = LinearLayout.LayoutParams(
                                            LinearLayout.LayoutParams.MATCH_PARENT,
                                            LinearLayout.LayoutParams.WRAP_CONTENT
                                        )
                                        adjustViewBounds = true
                                        setPadding(8, 8, 8, 8)
                                        isClickable = true
                                        setOnClickListener {
                                            viewModel.setBitmap(bitmap)
                                            findNavController().popBackStack()
                                        }
                                        setImageBitmap(bitmap)
                                    }

                                    binding.galleryLinearLayout.addView(imageView)
                                }.addOnFailureListener { exception ->
                                    Log.e("GalleryFragment", "Error downloading image", exception)
                                }
                            }
                        }
                        .addOnFailureListener { exception ->
                            Log.e("GalleryFragment", "Error listing images in user folder", exception)
                        }
                }
            }
            .addOnFailureListener { exception ->
                Log.e("GalleryFragment", "Error listing user folders", exception)
            }
    }
}