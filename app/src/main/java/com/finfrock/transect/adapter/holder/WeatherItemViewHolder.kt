package com.finfrock.transect.adapter.holder

import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import com.finfrock.transect.R
import com.finfrock.transect.model.*

class WeatherItemViewHolder(view: View,
                            private val observations: ObservationBuilder
): ObservationItemViewHolder(view) {
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
    private val beaufortErrorIcon: ImageView = view.findViewById(R.id.icon_beaufort_error)
    private val weather: AutoCompleteTextView = view.findViewById(R.id.weather_edit)
    private val weatherErrorIcon: ImageView = view.findViewById(R.id.icon_weather_error)

    init {
        val beaufortAdapter =
            ArrayAdapter(view.context, R.layout.sighting_list_item, BEAUFORT_OPTIONS)
        beaufort.setAdapter(beaufortAdapter)

        val weatherAdapter = ArrayAdapter(view.context, R.layout.sighting_list_item,
            WEATHER_OPTIONS
        )
        weather.setAdapter(weatherAdapter)

        beaufort.doAfterTextChanged {
            observations.updateFromIndex(adapterPosition) { obs ->
                if (obs is WeatherObservationMutable) {
                    val index = BEAUFORT_OPTIONS.indexOf(it.toString())
                    if (index >= 0) {
                        obs.beaufort = index
                        beaufortErrorIcon.visibility = View.INVISIBLE
                    } else {
                        Toast.makeText(view.context, "beaufort inner set", Toast.LENGTH_SHORT).show()
                        obs.beaufort = null
                        beaufortErrorIcon.visibility = View.VISIBLE
                    }
                }

                obs
            }
        }

        weather.doAfterTextChanged {
            Toast.makeText(view.context, "weather $adapterPosition", Toast.LENGTH_SHORT).show()
            observations.updateFromIndex(adapterPosition) { obs ->
                if (obs is WeatherObservationMutable) {
                    val index = WEATHER_OPTIONS.indexOf(it.toString())
                    if (index >= 0) {
                        obs.weather = index
                        weatherErrorIcon.visibility = View.INVISIBLE
                    } else {
                        obs.weather = null
                        weatherErrorIcon.visibility = View.VISIBLE
                    }
                }

                obs
            }
        }
    }

    override fun display(obs: ObservationNullable) {
        if (obs is WeatherObservationNullable) {
            if (obs.beaufort != null && obs.beaufort < 7) {
                beaufort.setText(BEAUFORT_OPTIONS[obs.beaufort], false)
                beaufortErrorIcon.visibility = View.INVISIBLE
            } else {
                beaufort.setText("", false)
                beaufortErrorIcon.visibility = View.VISIBLE
            }

            if (obs.weather != null && obs.weather < 8) {
                weather.setText(WEATHER_OPTIONS[obs.weather], false)
                weatherErrorIcon.visibility = View.INVISIBLE
            } else {
                weather.setText("", false)
                weatherErrorIcon.visibility = View.VISIBLE
            }
        }
    }
}
