package com.works.carebook001.patient

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.works.carebook001.R
import com.works.carebook001.doctor.models.AppointmentData
import java.util.Calendar

class AppointmentActivity : AppCompatActivity() {
    lateinit var txtAppName: TextView
    lateinit var txtAppSurname: TextView
    lateinit var txtAppAge: TextView
    lateinit var txtAppField: TextView
    lateinit var txtAppHour: TextView
    lateinit var btnSelectHour: ImageButton
    lateinit var btnSelectDate: ImageButton
    lateinit var btnMakeApp: Button
    lateinit var editTxtAppNote: EditText
    var Date = ""
    var selectedHour = ""
    lateinit var ImgApp: ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_appointment)

        txtAppName = findViewById(R.id.txtAppName)
        txtAppSurname = findViewById(R.id.txtAppSurname)
        txtAppAge = findViewById(R.id.txtAppAge)
        txtAppField = findViewById(R.id.txtAppField)
        txtAppHour = findViewById(R.id.txtAppHour)
        btnSelectHour = findViewById(R.id.btnSelectHour)
        btnSelectDate = findViewById(R.id.btnSelectDate)
        ImgApp = findViewById(R.id.ImgApp)
        btnMakeApp = findViewById(R.id.btnMakeApp)
        editTxtAppNote = findViewById(R.id.editTxtAppNote)

        val doctorName = intent.getStringExtra("name")
        val doctorSurname = intent.getStringExtra("surname")
        val doctorAge = intent.getStringExtra("age")
        val doctorField = intent.getStringExtra("field")
        val doctorImage = intent.getStringExtra("image")
        val patientImage = intent.getStringExtra("patientImage")
        val patientFullName = intent.getStringExtra("patientName")




        txtAppName.text = "Name : " + doctorName
        txtAppSurname.text = "Surname : " + doctorSurname
        txtAppAge.text = "Age : " + doctorAge
        txtAppField.text = "Area : " + doctorField
        Glide.with(this).load(doctorImage).into(ImgApp)

        val currentDate = Calendar.getInstance()
        val year = currentDate.get(Calendar.YEAR)
        val month = currentDate.get(Calendar.MONTH)
        val dayOfMonth = currentDate.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            DatePickerDialog.OnDateSetListener { _, selectedYear, selectedMonth, selectedDayOfMonth ->
                // Use selected date

                val selectedDate = Calendar.getInstance()
                selectedDate.set(selectedYear, selectedMonth, selectedDayOfMonth)
                val dayOfWeek = selectedDate.get(Calendar.DAY_OF_WEEK)


                if (dayOfWeek == Calendar.SUNDAY) {
                    Toast.makeText(this, "No work on Sundays, cannot be selected", Toast.LENGTH_LONG)
                        .show()
                } else {
                    // Your transactions
                    var ay = "${selectedMonth + 1}"
                    if (selectedMonth + 1 < 10) {
                        ay = "0${selectedMonth + 1}"
                    }

                    Date = "$selectedDayOfMonth.$ay.$selectedYear"
                }

            },
            year,
            month,
            dayOfMonth
        )

        // Set days before today as minimum date
        val minDate = Calendar.getInstance()
        minDate.add(Calendar.DAY_OF_MONTH, 0)
        datePickerDialog.datePicker.minDate = minDate.timeInMillis

        // Set 20 days from today as the maximum date
        val maxDate = Calendar.getInstance()
        maxDate.add(Calendar.DAY_OF_MONTH, 20)
        datePickerDialog.datePicker.maxDate = maxDate.timeInMillis

        btnSelectDate.setOnClickListener {
            datePickerDialog.show()
        }


        val mTimePicker: TimePickerDialog
        val mCurrentTime = Calendar.getInstance()
        val hour = mCurrentTime.get(Calendar.HOUR_OF_DAY)
        val minute = mCurrentTime.get(Calendar.MINUTE)

        mTimePicker = TimePickerDialog(this, object : TimePickerDialog.OnTimeSetListener {

            override fun onTimeSet(p0: TimePicker?, hour: Int, minute: Int) {
                val roundedMinute = (Math.round(minute.toFloat() / 15) * 15) % 60
                //This rounds the entered value to the nearest multiple of 15.
                // IMPORTANT: For example, between 5.45 and 6.00, be careful that the time closest to 6.00 is rounded to 5.00.
                if (hour < 9 || hour >= 17) {
                    Toast.makeText(
                        this@AppointmentActivity,
                        "Please choose a time during business hours (9.00 - 17.00)",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    selectedHour = String.format("%d:%d", hour, roundedMinute)
                    txtAppHour.setText(
                        "History: " + Date + "\nMoment: " + String.format(
                            "%d:%d",
                            hour,
                            roundedMinute
                        )
                    )
                }

            }

        }, hour, minute, true)


        btnSelectHour.setOnClickListener {
            if (Date.isEmpty()) {
                Toast.makeText(this, "Please select date first", Toast.LENGTH_LONG).show()
            } else {
                mTimePicker.show()
            }


        }
        btnMakeApp.setOnClickListener {
            val patientEmail = FirebaseAuth.getInstance().currentUser?.email
            val doctorEmail = intent.getStringExtra("email")
            val patientImage = patientImage
            val doctorImage = doctorImage
            val appointmentNote = editTxtAppNote.text.toString()
            val appointmentDate = Date
            val appointmentHour = selectedHour

            if (patientEmail != null && appointmentDate.isNotEmpty() && appointmentHour.isNotEmpty()) {
                val doctorFullname = doctorName + " " + doctorSurname
                val appointmentInfo = AppointmentData(
                    null,
                    doctorEmail,
                    patientEmail,
                    patientFullName,
                    patientImage,
                    doctorFullname,
                    doctorImage,
                    doctorField,
                    appointmentNote,
                    appointmentDate,
                    appointmentHour
                )
                addAppointmentToFirestore(patientEmail,doctorEmail!!,appointmentInfo)
                Toast.makeText(this, "Your appointment has been created successfully", Toast.LENGTH_LONG).show()
                val intent = Intent(this, PatientHomePageActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(
                    this,
                    "Please fill in the required information completely",
                    Toast.LENGTH_LONG
                ).show()
            }


        }
    }
    fun addAppointmentToFirestore(
        patientEmail: String,
        doctorEmail: String,
        appointment: AppointmentData
    ) {
        val db = FirebaseFirestore.getInstance()

        // Create a document based on the patient email under appointments
        val patientRef = db.collection("appointments").document(patientEmail)

        // Create a subcollection called patientAppointments under this document
        // and add new appointment to this subcollection
        val newAppointmentRef = patientRef.collection("patientAppointments").document()

        // Add an appointment to the doctor with the same document ID
        val doctorRef = db.collection("doctorAppointments").document(doctorEmail)
        val newDoctorAppointmentRef =
            doctorRef.collection("appointments").document(newAppointmentRef.id)

        // Set appointment data
        newAppointmentRef.set(appointment)
            .addOnSuccessListener {
                newDoctorAppointmentRef.set(appointment)
                    .addOnSuccessListener {
                        Log.d("AppointmentActivity", "The appointment has been added successfully.")
                    }
                    .addOnFailureListener { e ->
                        Log.w("AppointmentActivity", "An error occurred while adding a doctor's appointment", e)
                    }
            }
            .addOnFailureListener { e ->
                Log.w("AppointmentActivity", "An error occurred while adding an appointment", e)
            }
    }

}