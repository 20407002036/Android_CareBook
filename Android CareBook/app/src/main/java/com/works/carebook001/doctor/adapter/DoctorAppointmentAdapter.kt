package com.works.carebook001.doctor.adapter

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.works.carebook001.R
import com.works.carebook001.doctor.models.DoctorAppointmentData


class DoctorAppointmentAdapter(private val context: Activity, private val list:List<DoctorAppointmentData>) : ArrayAdapter<DoctorAppointmentData>(context,
    R.layout.custom_doctor_appointment, list)
{
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val rootView = context.layoutInflater.inflate(R.layout.custom_doctor_appointment,null,true)
        val r_appName = rootView.findViewById<TextView>(R.id.r_appDName)
        val r_appDate = rootView.findViewById<TextView>(R.id.r_appDDate)
        val r_appHour = rootView.findViewById<TextView>(R.id.r_appDHour)
        val r_appNote = rootView.findViewById<TextView>(R.id.r_appDNote)
        val r_appImg = rootView.findViewById<ImageView>(R.id.r_appDImg)

        val appointment = list.get(position)
        r_appName.text = appointment.patientName
        r_appDate.text = "Date : " + appointment.date
        r_appHour.text = "Time : " + appointment.hour
        r_appNote.text = "Note : " + appointment.note

        Glide.with(context).load(appointment.patientImg).into(r_appImg)


        return rootView
    }

}