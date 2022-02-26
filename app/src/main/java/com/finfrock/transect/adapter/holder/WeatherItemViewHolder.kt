package com.finfrock.transect.adapter.holder

import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.core.widget.doAfterTextChanged
import com.finfrock.transect.R
import com.finfrock.transect.model.Observation
import com.finfrock.transect.model.WeatherObservation
import com.google.android.material.textfield.TextInputLayout

class WeatherItemViewHolder(view: View,
                            private val observations: MutableList<Observation>): ObservationItemViewHolder(view) {
    companion object {
        val BEAUFORT_OPTIONS = listOf(
            "0 (calm)",
            "1 (1-3 kts; ripples)",
            "2 (4-6 kts; sm wavelets",
            "3 (7-10 kts; lg wavelets)",
            "4 (11-16 kts; some wh.caps)",
            "5 (17-21 kts; many wh.caps)",
            ">5")

        val WEATHER_OPTIONS = listOf(
            "Sunny",
            "Partly Sunny",
            "Overcast",
            "Scattered Showers",
            "Steady Showers",
            "Squalls",
            "Hard Rain",
            "Haze/VOG")
    }

    private val beaufort: TextInputLayout = view.findViewById(R.id.beaufort)
    private val weather: TextInputLayout = view.findViewById(R.id.weather)

    init {
        val beaufortAdapter =
            ArrayAdapter(view.context, R.layout.sighting_list_item, BEAUFORT_OPTIONS)
        (beaufort.editText as? AutoCompleteTextView)?.setAdapter(beaufortAdapter)

        val weatherAdapter = ArrayAdapter(view.context, R.layout.sighting_list_item,
            WEATHER_OPTIONS
        )
        (weather.editText as? AutoCompleteTextView)?.setAdapter(weatherAdapter)

        beaufort.editText?.doAfterTextChanged {
            val item = observations[adapterPosition]
            if (item is WeatherObservation) {
                val index = BEAUFORT_OPTIONS.indexOf(it.toString())
                if (index >= 0) {
                    item.beaufort = index
                } else {
                    item.beaufort = null
                }
            }
        }

        weather.editText?.doAfterTextChanged {
            val item = observations[adapterPosition]
            if (item is WeatherObservation) {
                val index = WEATHER_OPTIONS.indexOf(it.toString())
                if (index >= 0) {
                    item.weather = index
                } else {
                    item.weather = null
                }
            }
        }
    }

    override fun display(obs: Observation) {
        if (obs is WeatherObservation) {
            if (obs.beaufort != null && obs.beaufort!! < 7) {
                (beaufort.editText as? AutoCompleteTextView)?.setText(BEAUFORT_OPTIONS[obs.beaufort!!], false)
            } else {
                (beaufort.editText as? AutoCompleteTextView)?.setText("", false)
            }

            if (obs.weather != null && obs.weather!! < 8) {
                (weather.editText as? AutoCompleteTextView)?.setText(WEATHER_OPTIONS[obs.weather!!], false)
            } else {
                (weather.editText as? AutoCompleteTextView)?.setText("", false)
            }
        }
    }
}
