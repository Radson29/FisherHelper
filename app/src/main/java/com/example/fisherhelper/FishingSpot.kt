package com.example.fisherhelper

import android.location.Location
import android.os.Parcel
import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.GeoPoint

data class FishingSpot(
    val name: String?, val latLng: LatLng,
    val currentLocation: LatLng

                     ) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readParcelable(GeoPoint::class.java.classLoader)!!,
        parcel.readParcelable(LatLng::class.java.classLoader)!!
    )
    //to jest test

    private  val OPEN_WEATHER_MAP_API_URL = "https://api.openweathermap.org/data/2.5/weather?"
    private  val OPEN_WEATHER_MAP_API_KEY = "3b76f82e8a13f592648058112756e765"
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeParcelable(latLng, flags)
 parcel.writeParcelable(currentLocation,flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<FishingSpot> {
        override fun createFromParcel(parcel: Parcel): FishingSpot {
            return FishingSpot(parcel)
        }

        override fun newArray(size: Int): Array<FishingSpot?> {
            return arrayOfNulls(size)
        }
    }

    val distanceFromCurrentLocation: Float
        get() {
            val distance = FloatArray(1)
            val latLng2 = LatLng(50.029488, 22.008091)

            Location.distanceBetween(
                 currentLocation.latitude, currentLocation.longitude,
//                latLng2.latitude,latLng2.longitude,
               latLng.latitude, latLng.longitude, distance
            )
            return distance[0] / 1000
        }



}
