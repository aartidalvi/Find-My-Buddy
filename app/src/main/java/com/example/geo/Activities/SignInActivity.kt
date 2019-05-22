package com.example.geo.Activities

import android.content.Intent
import android.icu.text.DateTimePatternGenerator.PatternInfo.OK
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.example.geo.R
import com.example.geo.Models.User
import com.example.geo.Models.UserDetails
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_main.*
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_sign_in.*
import com.thomashaertel.widget.MultiSpinner

class SignInActivity : AppCompatActivity() , AdapterView.OnItemSelectedListener{

    private val LOGTAG = "InstaPost"
    private lateinit var mAuth: FirebaseAuth
    var REQ_CODE = 1
    var spinner: MultiSpinner? = null
    var adapter: ArrayAdapter<String>? = null

    //current user related fields
    var nationality = ""
    var password = ""
    var languages: MutableMap<String, String> = mutableMapOf()
    var lang_array = arrayOf<String>()//emptyArray<String>()
    lateinit var user: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        showInstructions()
        mAuth = FirebaseAuth.getInstance()
        progressbar.visibility = View.GONE
        createSpinners()
        nationalitySpinner.onItemSelectedListener = this
        signUpButton.setOnClickListener { RegisterUser() }
        signInButton.setOnClickListener { signIn() }
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View, pos: Int, id: Long) {
        if(parent.id == R.id.nationalitySpinner)
            nationality = parent.getItemAtPosition(pos).toString()
    }

    override fun onNothingSelected(parent: AdapterView<*>) {
        if(parent.id == R.id.nationalitySpinner)
            nationality = ""
    }

    private val onSelectedListener = MultiSpinner.MultiSpinnerListener {
        // Do something here with the selected items
        it.forEachIndexed{ index, element ->
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
            nationalitySpinner.adapter = adapter
        }

        adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item)
        lang_array = resources.getStringArray(R.array.languages_array)
        for (language in lang_array)
            adapter?.add(language)

        languageSpinner.setAdapter(adapter, false, onSelectedListener)

        val selectedItems = BooleanArray(adapter?.count ?:0)
        selectedItems[0] = true
        languageSpinner.selected = selectedItems
    }

    private fun showInstructions() {
        val instructions = "Welcome to Find My Buddy!\nSign up with us, if you haven't already, we need you to fill languages that you speak and your nationality to find you buddies who you can chat with!\nAlso,use a nickname of your choice and we'll take care of protecting your privacy\nHope you find your buddy!!"
        val builder = AlertDialog.Builder(this)
        builder.setMessage(instructions)
            .setCancelable(false)
            .setPositiveButton("OK") { _, _ ->
            }
        val alert = builder.create()
        alert.show()
    }

    private fun signIn() {
        setContentView(R.layout.activity_sign_in)
        ssignInButton.setOnClickListener{ signInUser()}
    }

    private fun validSignInFields(signInEmail: String, signInpassword: String) : Boolean{
        if(signInEmail.isNullOrBlank() || signInEmail.isNullOrEmpty() ||signInpassword.isNullOrBlank() || signInpassword.isNullOrEmpty()) {
            showToast("All fields are mandatory")
            return false
        }
        return true
    }

    private fun signInUser () {
        var signInEmail = signInEmailText.text.toString()
        var signInpassword = signInPasswordText.text.toString()
        if(validSignInFields(signInEmail,signInpassword)) {
            mAuth.signInWithEmailAndPassword(signInEmail, signInpassword)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Log.d(LOGTAG, "signInWithEmail:success")
                        updateCurrentFields(signInEmail)

                    } else {
                        Log.e(LOGTAG, "signInWithEmail:failure", task.exception)
                        showToast("Authentication failed.")
                    }
                }
        }
    }
    private fun updateCurrentFields(email: String) {
        var databaseRef = FirebaseDatabase.getInstance().getReference("/Users/")

        val query3 = databaseRef
            .orderByChild("emailID")
            .equalTo(email)

        query3.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (snapshot in dataSnapshot.children) {
                        var current = snapshot.getValue(User::class.java)
                        if(current!=null) {
                            user = current
                            startActivity(getMapActivityIntent())
                        }
                    }

                }
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        })

    }

    private fun getMapActivityIntent(): Intent {

        var bundle = Bundle()
        bundle.putSerializable("userobj",user)
        UserDetails.user = user

        return Intent(this@SignInActivity, MapsActivity::class.java).apply {
            putExtras(bundle)
        }.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
    }

fun RegisterUser() {
    user = User(
        nameText.text.toString(),
        nicknameText.text.toString(),
        emailIDText.text.toString(),
        nationality,
        languages,
        false
    )

if (validFields()) {

    var databaseRef = FirebaseDatabase.getInstance().getReference("/Users/")

    val query3 = databaseRef
        .orderByChild("nickName")
        .equalTo(user.nickName)

    query3.addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            if (dataSnapshot.exists()) {
                showToast("Nickname already in use")
            }
            else {
                signUp()
            }

        }
        override fun onCancelled(databaseError: DatabaseError) {}
    })
}
}


private fun signUp() {
//save in database

progressbar.visibility = View.VISIBLE
mAuth.createUserWithEmailAndPassword(user.emailID, password).addOnCompleteListener(object : OnCompleteListener<AuthResult> {
    override fun onComplete(task: Task<AuthResult>) {
        if (task.isSuccessful) {
            FirebaseDatabase.getInstance().getReference("/Users/")
                .child(user.nickName)
                .setValue(user).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        progressbar.visibility = View.GONE
                        Log.i(LOGTAG, "Registration Suceess")
                        showToast("Registration Suceess")
                        startActivity(getMapActivityIntent())
                        UserDetails.user =user

                    } else {
                        Log.i(LOGTAG, "Registration Failure")
                        showToast(task.exception?.message ?: "Registration failure")
                    }
                }
        } else {
            Log.i(LOGTAG, "Registration Failure2")
            showToast(task.exception?.message ?: "Something went wrong with signup")
        }
    }
})
}

override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
super.onActivityResult(requestCode, resultCode, data)
if (requestCode == REQ_CODE) {
    setContentView(R.layout.activity_sign_in)
}
}

private fun validFields(): Boolean {
    password = passwordText.text.toString()

if (user.emailID.isEmpty() || user.userName.isEmpty() || user.nickName.isEmpty() || password.isEmpty() || nationality.isEmpty() || languages.isNullOrEmpty()) {
    showToast("All fields are mandatory")
    return false
}

if (!Patterns.EMAIL_ADDRESS.matcher(user.emailID).matches()) {
    showToast("Enter valid email id")
    return false
}
if (password.length < 8) {
    showToast("Enter valid password of length >= 8")
    return false
}

return true
}


private fun showToast(text: String) {
val toast = Toast.makeText(
    applicationContext,
    text,
    Toast.LENGTH_SHORT
)
toast.show()
}
}
