package com.example.bustop

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
//import android.widget.SearchView
import androidx.appcompat.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import com.example.bustop.databinding.ActivityMainBinding
import com.example.bustop.databinding.ActivityMainMenuBinding
import java.util.Locale

class MainMenu : AppCompatActivity() {
    private lateinit var clickButton: Button
    private lateinit var binding: ActivityMainMenuBinding
    private lateinit var listView: ListView
    private lateinit var searchView: SearchView

    private lateinit var sharedPreferences: SharedPreferences
    private val PREF_NAME = "MyPrefs"
    private val KEY_USERNAME = "username"

    private lateinit var arrayAdapter: ArrayAdapter<String>
    private lateinit var listItems: Array<String>
    private lateinit var originalList: Array<String>
    private var filteredList: ArrayList<String> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val name = sharedPreferences.getString(KEY_USERNAME, "")

        Toast.makeText(this, "Hello $name", Toast.LENGTH_SHORT).show()
        val t: TextView = findViewById(R.id.textView3)
        if (!name.isNullOrEmpty()) {
            t.text = name
        }

        clickButton = binding.btnBusReminder
        clickButton.setOnClickListener(View.OnClickListener {
            val intent = Intent(this, BusReminder::class.java)
            intent.putExtra("fromPage", "MainMenu")
            intent.putExtra("username", name)
            startActivity(intent)
        })

        listView = findViewById(R.id.listview1)
        searchView = binding.root.findViewById(R.id.SearchView)

        listItems = resources.getStringArray(R.array.busnumber)
        originalList = listItems.clone()
        filteredList.addAll(originalList.asList())

        arrayAdapter = ArrayAdapter<String>(
            this,
            R.layout.list_item,
            R.id.listName,
            filteredList
        )

        listView.adapter = arrayAdapter

        listView.setOnItemClickListener { parent, view, position, id ->
            when (position) {
                0, 1, 2, 3, 4, 5 -> {
                    startActivity(Intent(this, Busleft::class.java))
                }
            }
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { text ->
                    filteredList.clear()
                    if (text.isEmpty()) {
                        filteredList.addAll(originalList)
                    } else {
                        originalList.forEach { item ->
                            if (item.contains(text)) {
                                filteredList.add(item)
                            }
                        }
                    }
                    arrayAdapter.notifyDataSetChanged()
                }
                return true
            }
        })
    }
}



//class MainMenu : AppCompatActivity() {
//    private lateinit var clickButton: Button
//    private lateinit var binding: ActivityMainMenuBinding
//    private lateinit var listView: ListView
//    private lateinit var textView1: TextView
//    private lateinit var searchView: SearchView
//
//    //加上去的
//    private lateinit var sharedPreferences: SharedPreferences
//    private val PREF_NAME = "MyPrefs"
//    private val KEY_USERNAME = "username"
//
//    //到此
//    var listAdapter: ListAdapter? = null
//    var arrayAdapter: ArrayAdapter<String>? = null
//    private lateinit var listItems: Array<String>
//    private lateinit var originalList: Array<String>
//    private var filteredList: ArrayList<String> = ArrayList()
//
//
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        binding = ActivityMainMenuBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        val intent = intent
//        //加上去的
//        sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
//        val name = sharedPreferences.getString(KEY_USERNAME, "")
//        //到此
//
////        val name = intent.getStringExtra("id")
//        Toast.makeText(this, "Hello " + name, Toast.LENGTH_SHORT).show()
//        val t: TextView = findViewById(R.id.textView3)
////        t.setText(name + "!")
//
//        //加上去的
//        if (!name.isNullOrEmpty()) {
//            t.text = "$name!"
//        }
//        //到此
//
//        clickButton = binding.btnBusReminder
//        clickButton.setOnClickListener(View.OnClickListener {
//            val intent = Intent()
//            intent.setClass(this, BusReminder::class.java)
//            intent.putExtra("fromPage", "MainMenu")
//            intent.putExtra("username", name)
//            startActivity(intent)
//        })
//
//        val listView: ListView = findViewById(R.id.listview1)
//        val listItems = resources.getStringArray(R.array.busnumber)
//
//
//        val arrayAdapter =
//            object : ArrayAdapter<String>(this, R.layout.list_item, R.id.listName, listItems) {
//                override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
//                    val view = super.getView(position, convertView, parent)
//                    val listName = view.findViewById<TextView>(R.id.listName)
//                    listName.text = listItems[position]
//
//                    // 監聽器
//                    view.setOnClickListener {
//                        when (position) {
//                            0, 1, 2, 3, 4, 5 -> {
//                                startActivity(Intent(this@MainMenu, Busleft::class.java))
//                            }
//                            // 添加其他位置的情况
//                        }
//
//                    }
//
//                    return view
//                }
//            }
//        listView.adapter = arrayAdapter
//    }
//}
