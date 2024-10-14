package com.works.carebook001.doctor.services

import com.google.firebase.firestore.FirebaseFirestore
import com.works.carebook001.doctor.models.DoctorAppointmentData

class DoctorAppointmentService {
    private val db = FirebaseFirestore.getInstance()

    fun getAppointmentsForDoctor(
        doctorEmail: String,
        callback: (List<DoctorAppointmentData>) -> Unit
    ) {
        db.collection("doctorAppointments")
            .document(doctorEmail)
            .collection("appointments")
            .get()
            .addOnSuccessListener { documents ->
                val appointmentsList = documents.mapNotNull { document ->
                    val appointment = document.toObject(DoctorAppointmentData::class.java)
                    appointment?.copy(id = document.id)
                }
                callback(appointmentsList)
            }
    }

    fun deleteAppointment(doctorEmail: String, patientEmail: String, appointmentId: String, callback: (Boolean) -> Unit) {
        // Delete from Doctor collection
        db.collection("doctorAppointments")
            .document(doctorEmail)
            .collection("appointments")
            .document(appointmentId)
            .delete()
            .addOnSuccessListener {
                // Also delete from patient collection
                db.collection("appointments")
                    .document(patientEmail)
                    .collection("patientAppointments")
                    .document(appointmentId)
                    .delete()
                    .addOnSuccessListener {
                        callback(true)
                    }
                    .addOnFailureListener {
                        callback(false)
                    }
            }
            .addOnFailureListener {
                callback(false)
            }
    }
}