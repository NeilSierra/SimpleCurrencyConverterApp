package com.example.simplecurrencyconverter

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

// Part #3 - Added ExchangeRateResponse and CurrencyApiService
data class ExchangeRateResponse(
    // For the JSON to kotlin object conversion
    val result: String,
    val base_code: String,
    val rates: Map<String, Double>
)

interface CurrencyApiService {
    // For the retrofit api calling
    @GET("v6/latest/{base}")
    fun getRates(@Path("base") base: String): Call<ExchangeRateResponse>
}
