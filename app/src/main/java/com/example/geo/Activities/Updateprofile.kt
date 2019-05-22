package com.example.geo.Activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.example.geo.Models.User
import com.example.geo.Models.UserDetails
import com.example.geo.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.thomashaertel.widget.MultiSpinner
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_updateprofile.*

class Updateprofile : MenuActivity(), AdapterView.OnItemSelectedListener {
    private val LOGTAG = "InstaPost"
    private lateinit var mAuth: FirebaseAuth
    var REQ_CODE = 1
    var spinner: MultiSpinner? = null
    var adapter: ArrayAdapter<String>? = null
    //current user related fields
    var nationality = ""
    var languages: MutableMap<String, String> = mutableMapOf()
    var lang_array = arrayOf<String>()//emptyArray<String>()
    lateinit var user: User
    lateinit var listener: ValueEventListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_updateprofile)
        user = intent.getSerializableExtra("userobj") as User
        progressbaru.visibility = View.GONE
        createSpinners()
        nationalitySpinneru.onItemSelectedListener = this

        update.setOnClickListener { updateUserDatabase() }
        unameText.setText(user.userName)
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View, pos: Int, id: Long) {
        if (parent.id == R.id.nationalitySpinneru)
            nationality = parent.getItemAtPosition(pos).toString()
    }

    override fun onNothingSelected(parent: AdapterView<*>) {
        if (parent.id == R.id.nationalitySpinneru)
            nationality = user.nationality
    }

    private val onSelectedListener = MultiSpinner.MultiSpinnerListener {
        // Do something here with the selected items
        it.forEachIndexed { index, element ->
            if (element)
                languages[lang_array[index]] = lang_array[index]
        }
    }

    private fun createSpinners() {

        ArrayAdapter.createFromResource(
            this,
            R.array.countries_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            nationalitySpinneru.adapter = adapter
            adapter?.getPosition(user.nationality)?.let { nationalitySpinneru.setSelection(it) }
        }

        adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item)



        lang_array = resources.getStringArray(R.array.languages_array)
        for (language in lang_array)
            adapter?.add(language)

        languageSpinneru.setAdapter(adapter, false, onSelectedListener)

        val selectedItems = BooleanArray(adapter?.count ?: 0)

        lang_array.forEachIndexed { index, lang ->
            if(user.languages[lang].equals(lang))
                selectedItems[index] = true
        }

        languageSpinneru.selected = selectedItems
    }

    private fun updateUserDatabase() {
        if (validFields()) {
            UserDetails.user = user
            Log.i("LOGTAG", user.nickName)
            FirebaseDatabase.getInstance().getReference("/Users/")
                .child(user.nickName)
                .setValue(user).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.i("LOGTAG", "Updated Successfully!")
                        showToast("Updated Successfully!")
                    } else {
                        Log.i("LOGTAG", "Upload Failure")
                        showToast(task.exception?.message ?: "Registration failure")
                    }
                }
        }
    }

    private fun validFields(): Boolean {


        if (unameText.text.toString().isNullOrEmpty() || unameText.text.toString().isNullOrBlank()){
            showToast("User name can not be empty")
            return false
        }

        user.userName = unameText.text.toString()
        if(!nationality.isEmpty())
            user.nationality = nationality
        if(!languages.isNullOrEmpty())
            user.languages = languages
        return true
    }

    private fun showToast(text: String) {
        val toast = Toast.makeText(
            this,
            text,
            Toast.LENGTH_SHORT
        )
        toast.show()
    }
}