package com.example.simplecurrencyconverter

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.set
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {

    // Part #4
    // - Added value retrofit, apiService
    // - Added currentRates to store currency rates

    // Part #5:
    // - Added view references as values
    // - Added btnConvert functionality

    private val retrofit = Retrofit.Builder()
        // Initialize retrofit value (Part #4)
        .baseUrl("https://open.er-api.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    // Initialize apiService value(Part #4)
    private val apiService = retrofit.create(CurrencyApiService::class.java)

    // Variable to store currency rates (Part #4)
    private var currentRates: Map<String, Double>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // View references
        val btnConvert = findViewById<Button>(R.id.btnConvert)
        val btnSwap = findViewById<Button>(R.id.btnSwap)
        val btnClear = findViewById<Button>(R.id.btnClear)
        val etFromAmount = findViewById<EditText>(R.id.etFromAmount)
        val etToAmount = findViewById<EditText>(R.id.etToAmount)
        val spinnerFrom = findViewById<Spinner>(R.id.spinnerFrom)
        val spinnerTo = findViewById<Spinner>(R.id.spinnerTo)

        apiService.getRates("USD").enqueue(object : Callback<ExchangeRateResponse> {
            // Call the api on create so we have currency rates to store
            override fun onResponse(call: Call<ExchangeRateResponse>, response: Response<ExchangeRateResponse>) {
                if (response.isSuccessful) {
                    // Get the currency rates and store it to the spinners
                    currentRates = response.body()?.rates
                    currentRates?.let { rates ->
                        val currencyList = rates.keys.sorted()
                        val adapter = ArrayAdapter(
                            this@MainActivity,
                            android.R.layout.simple_spinner_item,
                            currencyList
                        )
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        spinnerFrom.adapter = adapter
                        spinnerTo.adapter = adapter
                    }
                }
            }

            override fun onFailure(call: Call<ExchangeRateResponse>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Failed to load rates. Check your internet connection.", Toast.LENGTH_SHORT).show()
            }
        })

        btnConvert.setOnClickListener {
            // Button convert on click event handler
            val amount = etFromAmount.text.toString().toDoubleOrNull()
            val fromCurrency = spinnerFrom.selectedItem.toString()
            val toCurrency = spinnerTo.selectedItem.toString()

            if (amount == null) {
                Toast.makeText(this@MainActivity, "Please enter a valid number.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val fromRate = currentRates?.get(fromCurrency)
            val toRate = currentRates?.get(toCurrency)

            if (fromRate != null && toRate != null) {
                val converted = amount / fromRate * toRate
                etToAmount.setText("%.2f".format(converted))
            } else {
                Toast.makeText(this@MainActivity, "Rate not available.", Toast.LENGTH_SHORT).show()
            }
        }

        btnSwap.setOnClickListener {
            // Swaps spinners and editTexts content
            val spinnerFromIndex = spinnerFrom.selectedItemPosition
            val spinnerToIndex = spinnerTo.selectedItemPosition
            spinnerFrom.setSelection(spinnerToIndex)
            spinnerTo.setSelection(spinnerFromIndex)

            val fromAmount = etFromAmount.text.toString()
            val toAmount = etToAmount.text.toString()
            etFromAmount.setText(toAmount)
            etToAmount.setText(fromAmount)
        }

        btnClear.setOnClickListener {
            // Clears all editTexts and spinners
            etFromAmount.text.clear()
            etToAmount.text.clear()
            spinnerFrom.setSelection(0)
            spinnerTo.setSelection(0)
        }

    }
}