package com.example.fisherhelper

import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.fisherhelper.databinding.ActivityMapsBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import kotlinx.android.synthetic.main.activity_maps.*


class MapsActivity : AppCompatActivity(), OnMapReadyCallback, OnMapsSdkInitializedCallback {


    private lateinit var mMap: GoogleMap

    private lateinit var binding: ActivityMapsBinding

    private lateinit var currentLocation: Location
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val permissionCode =101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapsInitializer.initialize(applicationContext, MapsInitializer.Renderer.LATEST, this)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)



getCurrentLocationUser()

getMarkersFromFireStore()
    }

    private fun getMarkersFromFireStore() {

        val db = FirebaseFirestore.getInstance()
        val ref = db.collection("coordinates")
         ref.get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                // document exists
                val geopoint = document.getGeoPoint("geoPoint")
                val name= document.get("name")
                val latLng = LatLng(geopoint!!.latitude, geopoint!!.longitude)
                mMap.addMarker(MarkerOptions().position(latLng).title(name as String?).icon(BitmapDescriptorFactory.fromResource(R.drawable.gear)))
            }

        }

    }



    private fun getCurrentLocationUser() {
        if(ActivityCompat.checkSelfPermission(
                this,android.Manifest.permission.ACCESS_FINE_LOCATION)!=
            PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                (this,android.Manifest.permission.ACCESS_COARSE_LOCATION)!=
            PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),permissionCode)
            return
        }
        val getLocation= fusedLocationProviderClient.lastLocation.addOnSuccessListener {
                location ->
            if(location != null){
                currentLocation =location
                Toast.makeText(applicationContext, currentLocation.latitude.toString()+"" +
                        currentLocation.longitude.toString(), Toast.LENGTH_SHORT).show()

                val mapFragment = supportFragmentManager
                    .findFragmentById(R.id.map) as SupportMapFragment
                mapFragment.getMapAsync(this)
            }
        }


    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            permissionCode ->if(grantResults.isNotEmpty() && grantResults[0]==
                PackageManager.PERMISSION_GRANTED){
                getCurrentLocationUser()
            }
        }
    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
//        val latLng = LatLng(currentLocation.latitude ,currentLocation.longitude)
        val latLng = LatLng(50.029488, 22.008091) //example location of rzeszow
        val location = currentLocation.latitude.toString()
        val location1 = currentLocation.longitude.toString()
        val markerOptions = MarkerOptions().position(latLng)
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
            .title("Twoja lokalizacja $location $location1")
        googleMap?.animateCamera(CameraUpdateFactory.newLatLng(latLng))
        googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17f))
        googleMap?.addMarker(markerOptions)

        val db = FirebaseFirestore.getInstance()
        // pobranie referencji do kolekcji w Firestore
        val collectionReference = db.collection("coordinates")


// utworzenie obiektu GoogleMap za pomocą MapView

        // wyświetlenie markera na mapie
        val marker = googleMap.addMarker(MarkerOptions().position(LatLng(0.0, 0.0)).title("Czy tutaj chcesz dodać łowisko?"))

        // nasłuchiwanie kliknięć na mapie
        mMap.setOnMapClickListener { latLng ->
            // ustawienie pozycji markera na klikniętym miejscu na mapie
            marker!!.position = latLng
        }



        add_marker_button.setOnClickListener {
            // pobranie koordynatów markera
            val latLng = marker!!.position

            // pobranie nazwy markera z pola tekstowego
            val markerName = marker_name_input.text.toString()

            // utworzenie obiektu GeoPoint z koordynatami markera
            val markerGeoPoint = GeoPoint(latLng.latitude, latLng.longitude)
            collectionReference.add(
                mapOf(
                    "name" to markerName,
                    "geoPoint" to markerGeoPoint
                )
            )
                .addOnSuccessListener {
                    // wyświetlenie komunikatu o powodzeniu dodawania markera
                    getMarkersFromFireStore()
                 val toast=   Toast.makeText(this, "Łowisko dodane pomyślnie", Toast.LENGTH_SHORT).show()
//                    val view = toast.view
//                    view!!.setBackgroundColor(Color.BLUE)
//                    toast.show()
                    marker.position = LatLng(0.0, 0.0)
                    // wyczyszczenie pola tekstowego z nazwą markera
                    marker_name_input.setText("")

                }
                .addOnFailureListener { exception ->
                    // wyświetlenie komunikatu o błędzie podczas dodawania markera
                    Toast.makeText(
                        this,
                        "Błąd podczas dodawania markera: ${exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }


        }
    }

    override fun onMapsSdkInitialized(renderer: MapsInitializer.Renderer) {
        when (renderer) {
            MapsInitializer.Renderer.LATEST -> Log.d("MapsDemo", "The latest version of the renderer is used.")
            MapsInitializer.Renderer.LEGACY -> Log.d("MapsDemo", "The legacy version of the renderer is used.")
        }
    }
}