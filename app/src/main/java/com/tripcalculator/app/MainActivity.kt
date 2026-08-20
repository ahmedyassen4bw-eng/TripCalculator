package com.tripcalculator.app

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var etMinRate: EditText
    private lateinit var btnSaveMinRate: Button
    private lateinit var btnClear: Button
    private lateinit var etKm: EditText
    private lateinit var etAmount: EditText
    private lateinit var tvStatus: TextView
    private lateinit var tvResultDetails: TextView

    private val PREFS_NAME = "TripCalcPrefs"
    private val KEY_MIN_RATE = "min_rate"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        setContentView(R.layout.activity_main)

        initViews()
        loadSavedMinRate()
        setupTextWatchers()

        btnSaveMinRate.setOnClickListener {
            val minRateStr = etMinRate.text.toString()
            if (minRateStr.isNotEmpty()) {
                val minRate = minRateStr.toFloatOrNull()
                if (minRate != null && minRate > 0) {
                    val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    prefs.edit().putFloat(KEY_MIN_RATE, minRate).apply()
                    Toast.makeText(this, "تم حفظ الحد الأدنى بنجاح", Toast.LENGTH_SHORT).show()
                    calculate()
                } else {
                    Toast.makeText(this, "يرجى إدخال حد أدنى صحيح", Toast.LENGTH_SHORT).show()
                }
            }
        }

        btnClear.setOnClickListener {
            etAmount.setText("")
            etKm.setText("")
            tvStatus.visibility = View.GONE
            tvResultDetails.visibility = View.GONE
        }
    }

    private fun initViews() {
        etMinRate = findViewById(R.id.etMinRate)
        btnSaveMinRate = findViewById(R.id.btnSaveMinRate)
        btnClear = findViewById(R.id.btnClear)
        etKm = findViewById(R.id.etKm)
        etAmount = findViewById(R.id.etAmount)
        tvStatus = findViewById(R.id.tvStatus)
        tvResultDetails = findViewById(R.id.tvResultDetails)
    }

    private fun loadSavedMinRate() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedMin = prefs.getFloat(KEY_MIN_RATE, -1f)
        if (savedMin != -1f) {
            etMinRate.setText(String.format(Locale.US, "%.1f", savedMin))
        }
    }

    private fun setupTextWatchers() {
        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                calculate()
            }
            override fun afterTextChanged(s: Editable?) {}
        }

        etAmount.addTextChangedListener(watcher)
        etKm.addTextChangedListener(watcher)
    }

    private fun calculate() {
        val amountStr = etAmount.text.toString()
        val kmStr = etKm.text.toString()
        val minRateStr = etMinRate.text.toString()

        val amount = amountStr.toDoubleOrNull()
        val km = kmStr.toDoubleOrNull()
        val minRate = minRateStr.toDoubleOrNull()

        if (amount == null || km == null || minRate == null || km <= 0) {
            tvStatus.visibility = View.GONE
            tvResultDetails.visibility = View.GONE
            return
        }

        val actualRate = amount / km
        val diff = actualRate - minRate

        tvStatus.visibility = View.VISIBLE
        tvResultDetails.visibility = View.VISIBLE

        if (actualRate >= minRate) {
            tvStatus.text = "الرحلة مربحة ✅"
            tvStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark))
        } else {
            tvStatus.text = "الرحلة خاسرة ❌"
            tvStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark))
        }

        val detailsText = """
            سعر الكيلومتر: ${String.format(Locale.US, "%.2f", actualRate)} جنيه/كم
            الحد الأدنى: ${String.format(Locale.US, "%.2f", minRate)} جنيه/كم
            الفرق: ${String.format(Locale.US, "%.2f", diff)} جنيه/كم
            -----------------------------
            إجمالي الرحلة: $amount جنيه  |  المسافة: $km كم
        """.trimIndent()

        tvResultDetails.text = detailsText
    }
}
