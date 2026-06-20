package com.psm.medreminder

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView

class calendar : AppCompatActivity() {


    lateinit var currentMonthTextView: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar_test)
        currentMonthTextView = findViewById(R.id.month)
        currentMonthTextView.text = ""

        val array = arrayOf("No medicine scheduled")
        val adapter = ArrayAdapter(this,
                R.layout.activity_calendar_test, array)
        val listView: ListView = findViewById(R.id.textView2)
        listView.setAdapter(adapter)
    }

}
