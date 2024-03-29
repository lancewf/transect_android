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
import androidx.lifecycle.ViewModelProvider
import com.finfrock.transect.MyApplication
import com.finfrock.transect.R
import com.finfrock.transect.RunningTransectActivity
import com.finfrock.transect.SavingProgressActivity
import com.finfrock.transect.data.AppDatabase
import com.finfrock.transect.data.DataSource
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.finfrock.transect.model.Observer
import com.finfrock.transect.model.Vessel
import com.finfrock.transect.model.VesselSummary
import com.finfrock.transect.network.TransectApiService
import kotlinx.coroutines.NonDisposableHandle.parent
import javax.inject.Inject

class StartPageFragment : Fragment() {

    private var selectedVessel: Vessel? = null
    private var selectedObserver1: Observer? = null
    private var selectedObserver2: Observer? = null
    private var bearing: Int? = null
    @Inject
    lateinit var database: AppDatabase

    @Inject
    lateinit var transectApiService: TransectApiService
    lateinit var dataSource: DataSource

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.start_frag, container, false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        (activity?.application as MyApplication).appComponent.inject(this)

        dataSource = ViewModelProvider(viewModelStore, DataSource.FACTORY(database, transectApiService))
            .get(DataSource::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val vesselLayout: TextInputLayout = requireView().findViewById(R.id.vessel)
        dataSource.getVessels().observe(viewLifecycleOwner){
            val vesselAdapter = ArrayAdapter(requireView().context, R.layout.list_item, it)
            (vesselLayout.editText as? AutoCompleteTextView)?.setAdapter(vesselAdapter)
        }
        (vesselLayout.editText as? AutoCompleteTextView)?.setOnItemClickListener { parent, _, position, _ ->
            selectedVessel = parent.getItemAtPosition(position) as Vessel
            checkNewTransectButton()
        }

        val observer1Layout: TextInputLayout = requireView().findViewById(R.id.observer1)
        (observer1Layout.editText as? AutoCompleteTextView)?.setOnItemClickListener { parent, _, position, _ ->
            selectedObserver1 = parent.getItemAtPosition(position) as Observer
            checkNewTransectButton()
        }

        val observer2Layout: TextInputLayout = requireView().findViewById(R.id.observer2)
        (observer2Layout.editText as? AutoCompleteTextView)?.setOnItemClickListener { parent, _, position, _ ->
            val selectedObserver = parent.getItemAtPosition(position) as Observer
            selectedObserver2 = if (selectedObserver == Observer.NullObserver) {
                null
            } else {
                selectedObserver
            }
            checkNewTransectButton()
        }

        dataSource.getObservers().observe(viewLifecycleOwner){observers ->
            val observer1Adapter = ArrayAdapter(requireView().context, R.layout.list_item, observers)
            val observerWithNull = listOf(Observer.NullObserver) + observers
            val observer2Adapter = ArrayAdapter(requireView().context, R.layout.list_item, observerWithNull )
            (observer1Layout.editText as? AutoCompleteTextView)?.setAdapter(observer1Adapter)
            (observer2Layout.editText as? AutoCompleteTextView)?.setAdapter(observer2Adapter)
        }
        val bearingTextField: TextInputEditText = requireView().findViewById(R.id.bearing_edit_text)
        bearingTextField.isDirty
        bearingTextField.addTextChangedListener(
            object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    bearing = bearingTextField.text.toString().toIntOrNull()
                    checkNewTransectButton()
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s.isNullOrBlank()) {
                        bearingTextField.error = "can not be empty"
                        return
                    }
                    val bearing = s.toString().toIntOrNull()
                    when {
                        bearing == null -> bearingTextField.error = "not a integer"
                        bearing < 0 -> bearingTextField.error = "less than 0"
                        bearing > 360  -> bearingTextField.error = "greater than 360"
                    }
                }
            }
        )

        val startTransectButton: Button = requireView().findViewById(R.id.start_new_transect)
        startTransectButton.isEnabled = false
        startTransectButton.setOnClickListener {
            bearingTextField.text?.clear()
            (observer1Layout.editText as? AutoCompleteTextView)?.text?.clear()
            (observer2Layout.editText as? AutoCompleteTextView)?.text?.clear()
            (vesselLayout.editText as? AutoCompleteTextView)?.text?.clear()
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
}