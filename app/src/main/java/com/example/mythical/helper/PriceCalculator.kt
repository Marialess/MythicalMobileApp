package com.example.mythical.helper

fun Float?.getProductPrice(price: Float): Float{

    //this->porcentaje
    if (this == null)
        return price
    val remainingPricePercentage = 1f - this
    val priceAfterOffer = remainingPricePercentage * price

    return priceAfterOffer

}