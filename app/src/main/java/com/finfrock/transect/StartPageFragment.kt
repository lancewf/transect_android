package com.finfrock.transect

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.finfrock.transect.data.DataSource
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import com.finfrock.transect.model.Observer
import com.finfrock.transect.model.VesselSummary


class StartPageFragment : Fragment() {

    private var selectedVessel: VesselSummary? = null
    private var selectedObserver1: Observer? = null
    private var selectedObserver2: Observer? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.start_frag, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bearingTextField: TextInputEditText = requireView().findViewById(R.id.bearing_edit_text)
        bearingTextField.addTextChangedListener(BearingTextWatcher(bearingTextField))

        val vesselLayout: TextInputLayout = requireView().findViewById(R.id.vessel)
        val vessels = DataSource.loadVesselSummaries()
        val vesselAdapter = ArrayAdapter(requireView().context, R.layout.list_item, vessels)
        (vesselLayout.editText as? AutoCompleteTextView)?.setAdapter(vesselAdapter)
        (vesselLayout.editText as? AutoCompleteTextView)?.setOnItemClickListener { parent, view, position, id ->
            selectedVessel = parent.getItemAtPosition(position) as VesselSummary
            checkNewTransectButton()
        }

        val observer1Layout: TextInputLayout = requireView().findViewById(R.id.observer1)
        val observers = DataSource.loadObservers()
        val observerAdapter = ArrayAdapter(requireView().context, R.layout.list_item, observers)
        (observer1Layout.editText as? AutoCompleteTextView)?.setAdapter(observerAdapter)
        (observer1Layout.editText as? AutoCompleteTextView)?.setOnItemClickListener { parent, view, position, id ->
            selectedObserver1 = parent.getItemAtPosition(position) as Observer
            checkNewTransectButton()
        }

        val observer2Layout: TextInputLayout = requireView().findViewById(R.id.observer2)
        (observer2Layout.editText as? AutoCompleteTextView)?.setAdapter(observerAdapter)
        (observer2Layout.editText as? AutoCompleteTextView)?.setOnItemClickListener { parent, view, position, id ->
            selectedObserver2 = parent.getItemAtPosition(position) as Observer
            checkNewTransectButton()
        }

        val bearingEditText: TextInputEditText = requireView().findViewById(R.id.bearing_edit_text)

        bearingEditText.addTextChangedListener(
            object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    checkNewTransectButton()
                }

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }
            }
        )
        bearingEditText.isDirty

        val startTransectButton: Button = requireView().findViewById(R.id.start_new_transect)
        startTransectButton.setEnabled(false)
        startTransectButton.setOnClickListener {
            val intent = Intent(view.context, RunningTransectActivity::class.java)
            if (selectedVessel != null) {
                intent.putExtra(RunningTransectActivity.VESSEL_ID, selectedVessel!!.id)
            }
            if (selectedObserver1 != null) {
                intent.putExtra(RunningTransectActivity.OBSERVER1_ID, selectedObserver1!!.id)
            }
            if (selectedObserver2 != null) {
                intent.putExtra(RunningTransectActivity.OBSERVER2_ID, selectedObserver2!!.id)
            }
            if (!bearingEditText.text.toString()?.isEmpty()!!) {
                intent.putExtra(RunningTransectActivity.BEARING, bearingEditText.text.toString().toInt())
            }
            view.context.startActivity(intent)
        }

    }

    private fun checkNewTransectButton() {
        val startTransectButton: Button = requireView().findViewById(R.id.start_new_transect)
        val bearingEditText: TextInputEditText = requireView().findViewById(R.id.bearing_edit_text)

        if (selectedVessel != null && selectedObserver1 != null && !bearingEditText.text.toString()?.isEmpty()!!) {
            startTransectButton.setEnabled(true)
        } else {
            startTransectButton.setEnabled(false)
        }
    }

    private class BearingTextWatcher(private val textInput: TextInputEditText) : TextWatcher {
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