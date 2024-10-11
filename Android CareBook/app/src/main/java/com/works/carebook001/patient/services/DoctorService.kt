package com.works.carebook001.patient.services

import com.google.firebase.firestore.FirebaseFirestore
import com.works.carebook001.doctor.models.DoctorData
class DoctorService {

    fun getDoctors(callback: (List<DoctorData>) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val doctorsRef = db.collection("doctors")

        doctorsRef.get().addOnSuccessListener { querySnapshot ->
            val doctorDataList = mutableListOf<DoctorData>()

            for (document in querySnapshot) {
                val doctorData = document.toObject(DoctorData::class.java)
                if (doctorData != null) {
                    doctorDataList.add(doctorData)
                }
            }
            callback(doctorDataList)
        }.addOnFailureListener {
            callback(emptyList())
        }
    }
}






