package com.example.geo.Activities

import android.Manifest
import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.Location
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.util.Log
import android.widget.Toast
import com.example.geo.R
import com.example.geo.Models.User
import com.example.geo.Models.UserDetails
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.*

class MapsActivity : MenuActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    // 1
    private lateinit var locationCallback: LocationCallback
    // 2
    private lateinit var locationRequest: LocationRequest
    private var locationUpdateState = false
    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    //private lateinit var lastLocation: Location
    lateinit var user : User
    private var rv_list = mutableListOf<User>()
    var mAlreadyStartedService = false
    lateinit var databaseRef: DatabaseReference
    lateinit var buddyQuery : Query
    lateinit var listener: ValueEventListener


    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        // 3
        private const val REQUEST_CHECK_SETTINGS = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(intent!=null && intent.getSerializableExtra("userobj")!=null) {
            user = intent.getSerializableExtra("userobj") as User
            //UserDetails.user = user
        }
        /*else {
            user = UserDetails.user
        }*/
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        /*if (!mAlreadyStartedService)
        {
            val intent = Intent(this, BuddyLocationTrackingService::class.java)
            val bundle = Bundle()
            bundle.putSerializable("userobj", user)
            //bundle.putSerializable("activityobj",this)
            intent.putExtras(bundle)
            startService(intent)
            mAlreadyStartedService = true
        }*/

        setupLocationCallback()
        createLocationRequest()

    }


    /*override fun onDestroy() {
        val intent = Intent(this, SensorRestarterBroadcastReceiver::class.java)
        val bundle = Bundle()
        bundle.putSerializable("userobj", user)
        //bundle.putSerializable("activityobj",this)
        intent.putExtras(bundle)
        startService(intent)

        super.onDestroy()
    }*/

    private fun getChatActivityIntent(buddytitle: String): Intent {
        var bundle = Bundle()
        bundle.putSerializable("userobj",user)

        return Intent(this@MapsActivity, ChatActivity::class.java).apply {

            UserDetails.chatwithEmail = buddytitle.split(":")[1]
            UserDetails.userEmail = user.emailID
            UserDetails.chatwithID = buddytitle.split(":")[0]
            UserDetails.userID = user.nickName
            UserDetails.user =user

            putExtras(bundle)
            putExtra("user", user.emailID)
            putExtra("buddy",buddytitle.split(":")[1])
            putExtra("caller", "Maps")

        }.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        if(!marker.title.isNullOrEmpty() && !marker.title.isNullOrBlank()){
            if(marker.title.contains(user.emailID)) {
                showToast("You can't chat with yourself! ;)")
                return false
            }
            showToast("Let's get you connected with "+marker.title.split(":")[0])
            startActivity(getChatActivityIntent(marker.title))
            return true
        }
        return false
    }

    private fun setupLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)
                //lastLocation = p0.lastLocation
                //updateLocationInFireBase(p0.lastLocation)
                updateLocationInFireBase(p0.lastLocation)
                val currentLatLng = LatLng(p0.lastLocation.latitude, p0.lastLocation.longitude)
                placeMarkerOnMap(getUserMarker(currentLatLng))
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 8f))
                placeMarkersOnMap()
            }
        }
    }

    private fun startLocationUpdates() {
        //1
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            Thread.sleep(2000)
            return
        }
        //2
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    private fun createLocationRequest() {
        // 1
        locationRequest = LocationRequest()
        // 2
        locationRequest.interval = 1200000
        // 3
        locationRequest.fastestInterval = 1200000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)

        // 4
        val client = LocationServices.getSettingsClient(this)
        val task = client.checkLocationSettings(builder.build())

        // 5
        task.addOnSuccessListener {
            locationUpdateState = true
            startLocationUpdates()
        }
        task.addOnFailureListener { e ->
            // 6
            if (e is ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    e.startResolutionForResult(this@MapsActivity,
                        REQUEST_CHECK_SETTINGS
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == Activity.RESULT_OK) {
                locationUpdateState = true
                startLocationUpdates()
            }
        }
    }

    /*override fun onPause() {
        super.onPause()
        if(listener!=null)
            buddyQuery.removeEventListener(listener)
    }*/
    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.uiSettings.isZoomControlsEnabled = true
        map.setOnMarkerClickListener(this)
        setUpMap()
    }

    private fun setUpMap() {
        fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
            // Got last known location. In some rare situations this can be null.
            if (location != null){
//                lastLocation = location
                val currentLatLng = LatLng(location.latitude, location.longitude)
                updateLocationInFireBase(location)
                placeMarkerOnMap(getUserMarker(currentLatLng))
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 12f))
                placeMarkersOnMap()
            }
        }

        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            Thread.sleep(2000)

            if (ActivityCompat.checkSelfPermission(this,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                showToast("Access to location should be granted!")
                return
                //TODO: First time doesn't place marker
            }
        }
        map.isMyLocationEnabled = true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun appInForeground(context: Context): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningAppProcesses = activityManager.runningAppProcesses ?: return false
        return runningAppProcesses.any { it.processName == context.packageName && it.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND }
    }

    private fun isCommonLang(user1: User,user2:User) : Boolean{
        return (user1.languages.keys.intersect(user2.languages.keys).isNotEmpty())
    }
    private fun placeMarkersOnMap() {
        rv_list.clear()
        databaseRef = FirebaseDatabase.getInstance().getReference("/Users/")

        buddyQuery = databaseRef
            .orderByChild("haslocation")
            .equalTo(true)

        listener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (snapshot in dataSnapshot.children) {
                        val currentUser = snapshot.getValue(User::class.java)
                        if(currentUser!=null && currentUser.emailID!= user.emailID && currentUser.latitude?:0 != 0 && currentUser.longitude?:0 !=0) {
                            if((user.nationality.equals(currentUser.nationality) || isCommonLang(user,currentUser)))
                            placeMarkerOnMap(
                                getBuddyMarker(
                                    currentUser.nickName + ":" + currentUser.emailID,
                                    LatLng(currentUser.latitude, currentUser.longitude)
                                )
                            )
                        }
                    }
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        }
        buddyQuery.addValueEventListener(listener)

    }

    private fun updateLocationInFireBase(location: Location){
        user.haslocation = true
        if(user.latitude == location.latitude && user.longitude == location.longitude) {
            return
        }

        user.latitude = location.latitude
        user.longitude = location.longitude
        updateUserDatabase()
    }

    private fun updateUserDatabase() {

        if(user.nickName.isBlank() || user.nickName.isEmpty())
            return

        Log.i("LOGTAG",user.nickName)
        FirebaseDatabase.getInstance().getReference("/Users/")
            .child(user.nickName)
            .setValue(user).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.i("LOGTAG", "Upload Suceess")

                } else {
                    Log.i("LOGTAG", "Upload Failure")


                }
            }
    }

    private fun showToast(text: String) {
        val toast = Toast.makeText(
            applicationContext,
            text,
            Toast.LENGTH_SHORT
        )
        toast.show()
    }

    private fun getUserMarker(location: LatLng?): MarkerOptions? {
        if(location != null) {
            return MarkerOptions().position(location).title(user.nickName+":"+user.emailID)
        }
        return null
    }

    private fun getBuddyMarker(nickname_emailID:String, location: LatLng?): MarkerOptions? {
        if(location != null) {
            val markerOptions = MarkerOptions().position(location).title(nickname_emailID)
            markerOptions.icon(
                BitmapDescriptorFactory.fromBitmap(
                    BitmapFactory.decodeResource(resources, R.mipmap.ic_user_location)
                )
            )
            return markerOptions
        }
        return null
    }
    private fun placeMarkerOnMap(markerOptions: MarkerOptions?) {
        if (markerOptions != null)
        {
            map.addMarker(markerOptions)
        }
    }
}
