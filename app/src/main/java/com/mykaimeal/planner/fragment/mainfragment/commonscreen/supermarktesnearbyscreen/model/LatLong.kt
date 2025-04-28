package com.mykaimeal.planner.fragment.mainfragment.commonscreen.supermarktesnearbyscreen.model

import android.os.Parcel
import android.os.Parcelable

data class LatLng(val latitude: Double, val longitude: Double) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readDouble(),
        parcel.readDouble()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeDouble(latitude)
        parcel.writeDouble(longitude)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<LatLng> {
        override fun createFromParcel(parcel: Parcel): LatLng {
            return LatLng(parcel)
        }

        override fun newArray(size: Int): Array<LatLng?> {
            return arrayOfNulls(size)
        }
    }
}
