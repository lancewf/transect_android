package com.finfrock.transect.adapter.holder

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.core.widget.doAfterTextChanged
import androidx.core.widget.doOnTextChanged
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

    private val beaufort: AutoCompleteTextView = view.findViewById(R.id.beaufort_edit)
    private val weather: AutoCompleteTextView = view.findViewById(R.id.weather_edit)
    private val listeners = mutableListOf<(Boolean) -> Unit>()

    init {
        val beaufortAdapter =
            ArrayAdapter(view.context, R.layout.sighting_list_item, BEAUFORT_OPTIONS)
        beaufort.setAdapter(beaufortAdapter)

        val weatherAdapter = ArrayAdapter(view.context, R.layout.sighting_list_item,
            WEATHER_OPTIONS
        )
        weather.setAdapter(weatherAdapter)

        beaufort.doAfterTextChanged {
            val item = observations[adapterPosition]
            if (item is WeatherObservation) {
                val index = BEAUFORT_OPTIONS.indexOf(it.toString())
                if (index >= 0) {
                    item.beaufort = index
                    beaufort.error = null
                } else {
                    item.beaufort = null
                    beaufort.error = "Can not be blank"
                }
                setStatus()
            }
        }

        weather.doAfterTextChanged {
            val item = observations[adapterPosition]
            if (item is WeatherObservation) {
                val index = WEATHER_OPTIONS.indexOf(it.toString())
                if (index >= 0) {
                    weather.error = null
                    item.weather = index
                } else {
                    weather.error = "Can not be blank"
                    item.weather = null
                }
                setStatus()
            }
        }
    }

    override fun display(obs: Observation) {
        if (obs is WeatherObservation) {
            if (obs.beaufort != null && obs.beaufort!! < 7) {
                beaufort.setText(BEAUFORT_OPTIONS[obs.beaufort!!], false)
            } else {
                beaufort.setText("", false)
            }

            if (obs.weather != null && obs.weather!! < 8) {
                weather.setText(WEATHER_OPTIONS[obs.weather!!], false)
                weather.error = null
            } else {
                weather.setText("", false)
                weather.error = "Can not be blank"
            }
            setStatus()
        }
    }

    private fun setStatus() {
        if (weather.error == null && beaufort.error == null) {
            listeners.forEach{it(true)}
        } else {
            listeners.forEach{it(false)}
        }
    }

    override fun onErrorStatusChanged(listener: (Boolean) -> Unit) {
        listeners.add(listener)
    }
}
