package com.finfrock.transect.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import androidx.fragment.app.Fragment
import com.finfrock.transect.R
import com.finfrock.transect.RunningTransectActivity
import com.finfrock.transect.data.DataSource
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.finfrock.transect.model.Observer
import com.finfrock.transect.model.VesselSummary

class StartPageFragment : Fragment() {

    private var selectedVessel: VesselSummary? = null
    private var selectedObserver1: Observer? = null
    private var selectedObserver2: Observer? = null
    private var bearing: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.start_frag, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val vesselLayout: TextInputLayout = requireView().findViewById(R.id.vessel)
        val vessels = DataSource.loadVesselSummaries()
        val vesselAdapter = ArrayAdapter(requireView().context, R.layout.list_item, vessels)
        (vesselLayout.editText as? AutoCompleteTextView)?.setAdapter(vesselAdapter)
        (vesselLayout.editText as? AutoCompleteTextView)?.setOnItemClickListener { parent, _, position, _ ->
            selectedVessel = parent.getItemAtPosition(position) as VesselSummary
            checkNewTransectButton()
        }

        val observer1Layout: TextInputLayout = requireView().findViewById(R.id.observer1)
        val observers = DataSource.loadObservers()
        val observerAdapter = ArrayAdapter(requireView().context, R.layout.list_item, observers)
        (observer1Layout.editText as? AutoCompleteTextView)?.setAdapter(observerAdapter)
        (observer1Layout.editText as? AutoCompleteTextView)?.setOnItemClickListener { parent, _, position, _ ->
            selectedObserver1 = parent.getItemAtPosition(position) as Observer
            checkNewTransectButton()
        }

        val observer2Layout: TextInputLayout = requireView().findViewById(R.id.observer2)
        (observer2Layout.editText as? AutoCompleteTextView)?.setAdapter(observerAdapter)
        (observer2Layout.editText as? AutoCompleteTextView)?.setOnItemClickListener { parent, _, position, _ ->
            selectedObserver2 = parent.getItemAtPosition(position) as Observer
            checkNewTransectButton()
        }

        val bearingTextField: TextInputEditText = requireView().findViewById(R.id.bearing_edit_text)
        bearingTextField.isDirty
        bearingTextField.addTextChangedListener(BearingTextWatcher(bearingTextField))
        bearingTextField.addTextChangedListener(
            object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    bearing = bearingTextField.text.toString().toInt()
                    checkNewTransectButton()
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { }
            }
        )

        val startTransectButton: Button = requireView().findViewById(R.id.start_new_transect)
        startTransectButton.isEnabled = false
        startTransectButton.setOnClickListener {
            startTransectActivity(view.context)
        }
    }

    private fun startTransectActivity(context: Context) {
        val intent = Intent(context, RunningTransectActivity::class.java)
        if (selectedVessel != null) {
            intent.putExtra(RunningTransectActivity.VESSEL_ID, selectedVessel!!.id)
        }
        if (selectedObserver1 != null) {
            intent.putExtra(RunningTransectActivity.OBSERVER1_ID, selectedObserver1!!.id)
        }
        if (selectedObserver2 != null) {
            intent.putExtra(RunningTransectActivity.OBSERVER2_ID, selectedObserver2!!.id)
        }
        if (bearing != null) {
            intent.putExtra(RunningTransectActivity.BEARING, bearing)
        }

        context.startActivity(intent)
    }

    private fun checkNewTransectButton() {
        val startTransectButton: Button = requireView().findViewById(R.id.start_new_transect)

        startTransectButton.isEnabled = selectedVessel != null &&
                selectedObserver1 != null &&
                bearing != null
    }

    private class BearingTextWatcher(private val textInput: TextInputEditText) : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }
        override fun afterTextChanged(p0: Editable?) { }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            if (p0.isNullOrBlank()) {
                this.textInput.error = "can not be empty"
                return
            }
            val bearing = p0.toString().toIntOrNull()
            when {
                bearing == null -> this.textInput.error = "not a integer"
                bearing < 0 -> this.textInput.error = "less than 0"
                bearing > 360  -> this.textInput.error = "greater than 360"
            }
        }
    }
}