package com.example.bustop

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.graphics.toColor
import com.example.bustop.R
import com.example.bustop.databinding.ActivityMainBinding
import android.content.Context

class MainActivity : AppCompatActivity() {
    private lateinit var accEdit: EditText
    private lateinit var pwdEdit: EditText
    private lateinit var binding: ActivityMainBinding

    //加上去的
    private val PREF_NAME = "MyPrefs"
    private val KEY_USERNAME = "username"
    private lateinit var sharedPreferences: SharedPreferences
    //到此


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        title = "Bus"


        val loginButton: Button = findViewById(R.id.login)
        accEdit = findViewById(R.id.textInputEditText)
        pwdEdit = findViewById(R.id.textInputEditText1)

        loginButton.setOnClickListener {
            val acc = accEdit.text.toString()
            val pwd = pwdEdit.text.toString()
            login(acc, pwd)
        }

    }

    private fun login(acc: String, pwd: String) {

        if(acc.isEmpty() || pwd.isEmpty()){

            val context1=applicationContext
            val text1="Please enter all the details correctly."
            val duration1=Toast.LENGTH_SHORT
            Toast.makeText(context1,text1, duration1).show()
        }

        if (acc == "Karen" && pwd == "123") {
            //加上去的
            sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString(KEY_USERNAME, acc)
            editor.apply()
            //到此
            val intent = Intent(this, MainMenu::class.java)
            intent.putExtra("id", acc)
            startActivity(intent)
        }
        else{
            val context=applicationContext
            val text="Username or password is incorrect."
            val duration=Toast.LENGTH_SHORT
            Toast.makeText(context,text, duration).show()
        }
    }
}

