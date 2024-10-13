package com.works.carebook001.patient


import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.works.carebook001.R
import com.works.carebook001.patient.models.PatientData

class PatientProfileEditActivity : AppCompatActivity() {
    lateinit var edtPName: EditText
    lateinit var edtPSurname: EditText
    lateinit var edtPAge: EditText
    lateinit var edtOldPassword: EditText
    lateinit var edtNewPassword: EditText
    lateinit var btnSaveChanges: Button
    lateinit var imgPatientProfile : ImageView

    lateinit var downloadUri : Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_profile_edit)

        edtPName = findViewById(R.id.editPName)
        edtPSurname = findViewById(R.id.editPSurname)
        edtPAge = findViewById(R.id.editPAge)
        edtOldPassword = findViewById(R.id.editOldPassword)
        edtNewPassword = findViewById(R.id.editNewPassword)
        btnSaveChanges = findViewById(R.id.btnSaveChanges)
        imgPatientProfile = findViewById(R.id.imgPatientProfilePicture)

        val db = FirebaseFirestore.getInstance()
        val user = FirebaseAuth.getInstance().currentUser

        // Pull data from Firestore and assign it to EditTexts
        db.collection("patients")
            .document(user?.email!!)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val patientData = document.toObject(PatientData::class.java)
                    // Assign data to EditTexts
                    edtPName.setText(patientData?.first)
                    edtPSurname.setText(patientData?.last)
                    edtPAge.setText(patientData?.age)
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error getting client data: ${e.message}", e)
            }

        btnSaveChanges.setOnClickListener {
            if (edtPName.text.isNotEmpty() &&
                edtPSurname.text.isNotEmpty() &&
                edtPAge.text.isNotEmpty() &&
                edtOldPassword.text.isNotEmpty() &&
                edtNewPassword.text.isNotEmpty()
            ) {
                // AlertDialog ile kullanıcıyı onay alalım
                AlertDialog.Builder(this).apply {
                    setTitle("Approval")
                    setMessage("Do you want to update?")
                    setPositiveButton("Yes") { _, _ ->
                        val oldPassword = edtOldPassword.text.toString()
                        val newPassword = edtNewPassword.text.toString()
                        val name = edtPName.text.toString()
                        val surname = edtPSurname.text.toString()
                        val age = edtPAge.text.toString()

                        // Check the old password and update it
                        verifyAndUpdate(
                            oldPassword,
                            newPassword,
                            name,
                            surname,
                            age,
                            user?.email!!,
                            downloadUri.toString()
                        ) // images will be adjusted later
                        // Start intent with a specific delay time
                        Handler().postDelayed({
                            val intent = Intent(
                                this@PatientProfileEditActivity,
                                PatientProfileActivity::class.java
                            )
                            startActivity(intent)
                            finish()
                        }, 2000) // 2 seconds delay time
                    }
                    setNegativeButton("No", null)
                }.create().show()
            }else
            {
                Toast.makeText(this,"Please fill in the information completely",Toast.LENGTH_LONG).show()
            }
        }
        // Request access to the gallery when clicking on the image
        imgPatientProfile.setOnClickListener {


                openGallery()
            }
        }




    // Permission code to request user permission to access the gallery
    private val READ_EXTERNAL_STORAGE_PERMISSION = 123
    private val PICK_IMAGE_REQUEST = 123
    //Check the onRequestPermissionsResult method for the results of the requested permissions
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == READ_EXTERNAL_STORAGE_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, gallery can be accessed
                openGallery()
            } else {
                // Permission denied, gallery cannot be accessed
                Toast.makeText(this, "Gallery access denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Called when accessing the gallery
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    // Check onActivityResult method for gallery selection result
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            val selectedImageUri = data.data

            // Loading the selected image with Glide into ImageView

            Glide.with(this).load(selectedImageUri).into(imgPatientProfile)

            // Save the selected image to Firebase Storage
            val user = FirebaseAuth.getInstance().currentUser
            val storageRef = FirebaseStorage.getInstance().reference
            val imageRef = storageRef.child("users/${user?.email}/profile.jpg")

            val uploadTask = imageRef.putFile(selectedImageUri!!)
            uploadTask.continueWithTask { task ->
                if (!task.isSuccessful) {
                    throw task.exception!!
                }
                // Get the image's downloadable URL
                imageRef.downloadUrl
            }.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    downloadUri = task.result
                    // Actions to take when receiving the downloadable URL of the image
                    // For example, saving to Firestore
                    // You can get the URL using downloadUri.toString()
                } else {
                    // Error handling for situations where the image cannot be loaded
                }
            }
        }
    }

    private fun verifyAndUpdate(
        oldPassword: String,
        newPassword: String,
        first: String,
        last: String,
        age: String,
        email: String,
        image: String?
    ) {
        val user = FirebaseAuth.getInstance().currentUser

        val credential = EmailAuthProvider.getCredential(user?.email ?: "", oldPassword)
        user?.reauthenticate(credential)?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                user.updatePassword(newPassword).addOnCompleteListener { updateTask ->
                    if (updateTask.isSuccessful) {
                        // Now update other information in Firestore
                        updateClientInFirestore(
                            user.uid,
                            first,
                            last,
                            age,
                            email,
                            newPassword,
                            image
                        )
                    } else {
                        Toast.makeText(
                            this,
                            "Password could not be updated: ${updateTask.exception?.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } else {
                Toast.makeText(
                    this,
                    "Old password is wrong: ${task.exception?.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }


    private fun updateClientInFirestore(
        userId: String,
        first: String,
        last: String,
        age: String,
        email: String,
        password: String,
        image: String?
    ) {
        val db = FirebaseFirestore.getInstance()

        val clientInfo = PatientData(
            UID = userId,
            first = first,
            last = last,
            age = age,
            email = email,
            password = password,
            image = image
        )

        db.collection("patients")
            .document(email)
            .set(clientInfo)
            .addOnSuccessListener {
                Toast.makeText(this, "Information updated successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "An error occurred while updating information: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
}


