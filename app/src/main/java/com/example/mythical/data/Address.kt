package com.example.mythical.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize


@Parcelize
data class Address(
    val addressTittle: String,
    val fullName: String,
    val street: String,
    val phone: String,
    val city: String,
    val state: String
): Parcelable {
    constructor(): this("","","","","","")
}
