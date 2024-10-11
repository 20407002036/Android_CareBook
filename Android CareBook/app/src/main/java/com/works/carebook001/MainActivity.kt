package com.works.carebook001

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import com.works.carebook001.doctor.DoctorLoginActivity
import com.works.carebook001.patient.PatientLoginActivity

class MainActivity : AppCompatActivity() {
    lateinit var patientButton : ImageView
    lateinit var doctorButton : ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        patientButton = findViewById(R.id.patientButton)
        doctorButton = findViewById(R.id.doctorButton)

        patientButton.setOnClickListener {
            val intent = Intent(this@MainActivity,PatientLoginActivity::class.java)
            startActivity(intent)

        }

        doctorButton.setOnClickListener {
            val intent = Intent(this@MainActivity,DoctorLoginActivity::class.java)
            startActivity(intent)

        }
    }
}