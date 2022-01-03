package com.finfrock.transect

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputLayout

class SummaryPageFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.summary_frag, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val vesselLayout: TextInputLayout = requireView().findViewById(R.id.vessel)
        val vesselNames = listOf("Aloha Kai", "CaneFire II", "Kai Kanani", "Kohola", "Ohua", "Trilogy V")
        val vesselAdapter = ArrayAdapter(requireView().context, R.layout.list_item, vesselNames)
        (vesselLayout.editText as? AutoCompleteTextView)?.setAdapter(vesselAdapter)
    }
}