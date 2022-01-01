package com.finfrock.transect

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.core.content.ContentProviderCompat.requireContext
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        val bearingTextField: TextInputEditText = findViewById(R.id.bearing_edit_text)
        bearingTextField.addTextChangedListener(BearingTextWatcher(bearingTextField))

        val vesselLayout: TextInputLayout = findViewById(R.id.vessel)
        val vesselNames = listOf("Aloha Kai", "CaneFire II", "Kai Kanani", "Kohola", "Ohua", "Trilogy V")
        val vesselAdapter = ArrayAdapter(this, R.layout.list_item, vesselNames)
        (vesselLayout.editText as? AutoCompleteTextView)?.setAdapter(vesselAdapter)

        val observer1Layout: TextInputLayout = findViewById(R.id.observer1)
        val observerItems = listOf("Ed Lyman", "Grant Thompson", "Jason Moore", "Lance", "Rachel Finn")
        val observerAdapter = ArrayAdapter(this, R.layout.list_item, observerItems)
        (observer1Layout.editText as? AutoCompleteTextView)?.setAdapter(observerAdapter)

        val observer2Layout: TextInputLayout = findViewById(R.id.observer2)
        (observer2Layout.editText as? AutoCompleteTextView)?.setAdapter(observerAdapter)
    }

    class BearingTextWatcher(private val textInput: TextInputEditText) : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            val bearing = p0.toString().toIntOrNull()
            when {
                p0.isNullOrBlank() -> this.textInput.error = "can not be empty"
                bearing == null -> this.textInput.error = "not a integer"
                bearing < 0 -> this.textInput.error = "less than 0"
                bearing > 360  -> this.textInput.error = "greater than 360"
            }
        }

        override fun afterTextChanged(p0: Editable?) {
        }

    }
}
