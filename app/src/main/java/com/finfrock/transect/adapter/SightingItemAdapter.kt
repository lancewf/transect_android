package com.finfrock.transect.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.RecyclerView
import com.finfrock.transect.LocationProxyLike
import com.finfrock.transect.R
import com.finfrock.transect.ToggleButtonGroupTableLayout
import com.finfrock.transect.model.GroupType
import com.finfrock.transect.model.Sighting
import com.google.android.material.textfield.TextInputLayout
import java.time.LocalDateTime

class SightingItemAdapter(private val dataset: MutableList<Sighting>,
                          private val locationProxy: LocationProxyLike
): RecyclerView.Adapter<SightingItemAdapter.ItemViewHolder>() {

    class ItemViewHolder(view: View): RecyclerView.ViewHolder(view) {
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
        val countOptions: ToggleButtonGroupTableLayout = view.findViewById(R.id.count_options)
        val distanceOptions: ToggleButtonGroupTableLayout = view.findViewById(R.id.distance_options)
        val bearingOptions: ToggleButtonGroupTableLayout = view.findViewById(R.id.bearing_options)
        val groupType: RadioGroup = view.findViewById(R.id.group_type_options)
        val beaufort: TextInputLayout = view.findViewById(R.id.beaufort)
        val weather: TextInputLayout = view.findViewById(R.id.weather)

        init {
            val beaufortAdapter =
                ArrayAdapter(view.context, R.layout.sighting_list_item, BEAUFORT_OPTIONS)
            (beaufort.editText as? AutoCompleteTextView)?.setAdapter(beaufortAdapter)

            val weatherAdapter = ArrayAdapter(view.context, R.layout.sighting_list_item, WEATHER_OPTIONS)
            (weather.editText as? AutoCompleteTextView)?.setAdapter(weatherAdapter)
        }
    }

    fun addNewSighting() {
        locationProxy.getLocation().addOnSuccessListener {
            dataset.add(Sighting(
                datetime = LocalDateTime.now(),
                location = it
            ))
            notifyItemInserted(itemCount -1 )
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val adapterLayout =
            LayoutInflater.from(parent.context).inflate(R.layout.sighting_item, parent, false)

        val holder = ItemViewHolder(adapterLayout)

        holder.groupType.setOnCheckedChangeListener { _, id ->
            val sighting = dataset[holder.adapterPosition]
            when(id) {
                R.id.option_mc ->
                    sighting.groupType = GroupType.MC
                R.id.option_mce ->
                    sighting.groupType = GroupType.MCE
                R.id.option_cg ->
                    sighting.groupType = GroupType.CG
                R.id.option_unknown ->
                    sighting.groupType = GroupType.UNKNOWN
                else ->
                    sighting.groupType = null
            }
        }

        holder.bearingOptions.setOnCheckedChangeListener { _, id ->
            val sighting = dataset[holder.adapterPosition]
            when(id) {
                R.id.option_nine ->
                    sighting.bearing = 315
                R.id.option_10 ->
                    sighting.bearing = 330
                R.id.option_11 ->
                    sighting.bearing = 345
                R.id.option_12 ->
                    sighting.bearing = 0
                R.id.option_1 ->
                    sighting.bearing = 15
                R.id.option_2 ->
                    sighting.bearing = 30
                R.id.option_3 ->
                    sighting.bearing = 45
                else ->
                    sighting.bearing = null
            }
        }

        holder.distanceOptions.setOnCheckedChangeListener { _, id ->
            val sighting = dataset[holder.adapterPosition]
            when(id) {
                R.id.option_with_50m ->
                    sighting.distanceKm = 0.05
                R.id.option_100m ->
                    sighting.distanceKm = 0.1
                R.id.option_200m ->
                    sighting.distanceKm = 0.2
                R.id.option_300m ->
                    sighting.distanceKm = 0.3
                R.id.option_quarter_mile ->
                    sighting.distanceKm = 0.4
                R.id.option_half_mile ->
                    sighting.distanceKm = 0.8
                R.id.option_three_forth_mile ->
                    sighting.distanceKm = 1.2
                R.id.option_one_mile ->
                    sighting.distanceKm = 1.6
                R.id.option_greater_mile ->
                    sighting.distanceKm = 2.0
                else ->
                    sighting.distanceKm = null
            }
        }

        holder.countOptions.setOnCheckedChangeListener { _, id ->
            val sighting = dataset[holder.adapterPosition]
            when(id) {
                R.id.option_one_count ->
                    sighting.count = 1
                R.id.option_two_count ->
                    sighting.count = 2
                R.id.option_three_count ->
                    sighting.count = 3
                R.id.option_four_count ->
                    sighting.count = 4
                R.id.option_five_seven_count ->
                    sighting.count = 6
                R.id.option_eight_ten_count ->
                    sighting.count = 9
                R.id.option_eleven_thirteen_count ->
                    sighting.count = 12
                R.id.option_greater_thirteen_count ->
                    sighting.count = 14
                else ->
                    sighting.count = null
            }
        }

        holder.beaufort.editText?.doAfterTextChanged {
            val sighting = dataset[holder.adapterPosition]
            val index = ItemViewHolder.BEAUFORT_OPTIONS.indexOf(it.toString())
            if ( index >= 0 ) {
                sighting.beaufort = index
            } else {
                sighting.beaufort = null
            }
        }

        holder.weather.editText?.doAfterTextChanged {
            val sighting = dataset[holder.adapterPosition]
            val index = ItemViewHolder.WEATHER_OPTIONS.indexOf(it.toString())
            if ( index >= 0 ) {
                sighting.weather = index
            } else {
                sighting.weather = null
            }
        }
        return holder
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val sighting = dataset[position]

        if (sighting.count != null) {
            when {
                sighting.count == 1 -> holder.countOptions.check(R.id.option_one_count)
                sighting.count == 2 -> holder.countOptions.check(R.id.option_two_count)
                sighting.count == 3 -> holder.countOptions.check(R.id.option_three_count)
                sighting.count == 4 -> holder.countOptions.check(R.id.option_four_count)
                sighting.count in 5..7 -> holder.countOptions.check(R.id.option_five_seven_count)
                sighting.count in 8..10 -> holder.countOptions.check(R.id.option_eight_ten_count)
                sighting.count in 11..13 -> holder.countOptions.check(R.id.option_eleven_thirteen_count)
                sighting.count!! > 13 -> holder.countOptions.check(R.id.option_greater_thirteen_count)
                else -> holder.countOptions.clearCheck()
            }
        } else {
            holder.countOptions.clearCheck()
        }

        if (sighting.distanceKm != null) {
            when {
                sighting.distanceKm!! <= 0.06 -> holder.distanceOptions.check(R.id.option_with_50m)
                sighting.distanceKm!! <= 0.11 -> holder.distanceOptions.check(R.id.option_100m)
                sighting.distanceKm!! <= 0.21 -> holder.distanceOptions.check(R.id.option_200m)
                sighting.distanceKm!! <= 0.31 -> holder.distanceOptions.check(R.id.option_300m)
                sighting.distanceKm!! <= 0.41 -> holder.distanceOptions.check(R.id.option_quarter_mile)
                sighting.distanceKm!! <= 0.81 -> holder.distanceOptions.check(R.id.option_half_mile)
                sighting.distanceKm!! <= 1.21 -> holder.distanceOptions.check(R.id.option_three_forth_mile)
                sighting.distanceKm!! <= 1.61 -> holder.distanceOptions.check(R.id.option_one_mile)
                sighting.distanceKm!! > 1.61 -> holder.distanceOptions.check(R.id.option_greater_mile)
                else -> holder.distanceOptions.clearCheck()
            }
        } else {
            holder.distanceOptions.clearCheck()
        }

        if (sighting.bearing != null) {
            when {
                sighting.bearing in 180..321 -> holder.bearingOptions.check(R.id.option_nine)
                sighting.bearing in 322..336 -> holder.bearingOptions.check(R.id.option_10)
                sighting.bearing in 337..351 -> holder.bearingOptions.check(R.id.option_11)
                sighting.bearing!! > 351 || sighting.bearing!! < 7 -> holder.bearingOptions.check(R.id.option_12)
                sighting.bearing in 7..21 -> holder.bearingOptions.check(R.id.option_1)
                sighting.bearing in 22..36 -> holder.bearingOptions.check(R.id.option_2)
                sighting.bearing in 37..179 -> holder.bearingOptions.check(R.id.option_3)
                else -> holder.bearingOptions.clearCheck()
            }
        } else {
            holder.bearingOptions.clearCheck()
        }

        when(sighting.groupType) {
            GroupType.MC -> holder.groupType.check(R.id.option_mc)
            GroupType.MCE -> holder.groupType.check(R.id.option_mce)
            GroupType.CG -> holder.groupType.check(R.id.option_cg)
            GroupType.UNKNOWN -> holder.groupType.check(R.id.option_unknown)
            else -> holder.groupType.clearCheck()
        }

        if (sighting.beaufort != null && sighting.beaufort!! < 7) {
            (holder.beaufort.editText as? AutoCompleteTextView)?.setText(ItemViewHolder.BEAUFORT_OPTIONS[sighting.beaufort!!], false)
        } else {
            (holder.beaufort.editText as? AutoCompleteTextView)?.setText("", false)
        }

        if (sighting.weather != null && sighting.weather!! < 8) {
            (holder.weather.editText as? AutoCompleteTextView)?.setText(ItemViewHolder.WEATHER_OPTIONS[sighting.weather!!], false)
        } else {
            (holder.weather.editText as? AutoCompleteTextView)?.setText("", false)
        }
    }

    override fun getItemCount() = dataset.size

}