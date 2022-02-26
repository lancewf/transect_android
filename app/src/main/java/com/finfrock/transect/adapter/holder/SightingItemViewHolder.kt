package com.finfrock.transect.adapter.holder

import android.view.View
import android.widget.RadioGroup
import com.finfrock.transect.R
import com.finfrock.transect.view.ToggleButtonGroupTableLayout
import com.finfrock.transect.model.GroupType
import com.finfrock.transect.model.Observation
import com.finfrock.transect.model.Sighting

class SightingItemViewHolder(view: View,
                             private val observations: MutableList<Observation>): ObservationItemViewHolder(view)  {
    private val countOptions: ToggleButtonGroupTableLayout = view.findViewById(R.id.count_options)
    private val distanceOptions: ToggleButtonGroupTableLayout = view.findViewById(R.id.distance_options)
    private val bearingOptions: ToggleButtonGroupTableLayout = view.findViewById(R.id.bearing_options)
    private val groupType: RadioGroup = view.findViewById(R.id.group_type_options)

    init {
        groupType.setOnCheckedChangeListener { _, id ->
            val obs = observations[adapterPosition]
            if (obs is Sighting) {
                when (id) {
                    R.id.option_mc ->
                        obs.groupType = GroupType.MC
                    R.id.option_mce ->
                        obs.groupType = GroupType.MCE
                    R.id.option_cg ->
                        obs.groupType = GroupType.CG
                    R.id.option_unknown ->
                        obs.groupType = GroupType.UNKNOWN
                    else ->
                        obs.groupType = null
                }
            }
        }

        bearingOptions.setOnCheckedChangeListener { _, id ->
            val obs = observations[adapterPosition]
            if (obs is Sighting) {
                when (id) {
                    R.id.option_nine ->
                        obs.bearing = 315
                    R.id.option_10 ->
                        obs.bearing = 330
                    R.id.option_11 ->
                        obs.bearing = 345
                    R.id.option_12 ->
                        obs.bearing = 0
                    R.id.option_1 ->
                        obs.bearing = 15
                    R.id.option_2 ->
                        obs.bearing = 30
                    R.id.option_3 ->
                        obs.bearing = 45
                    else ->
                        obs.bearing = null
                }
            }
        }

        distanceOptions.setOnCheckedChangeListener { _, id ->
            val obs = observations[adapterPosition]
            if (obs is Sighting) {
                when (id) {
                    R.id.option_with_50m ->
                        obs.distanceKm = 0.05
                    R.id.option_100m ->
                        obs.distanceKm = 0.1
                    R.id.option_200m ->
                        obs.distanceKm = 0.2
                    R.id.option_300m ->
                        obs.distanceKm = 0.3
                    R.id.option_quarter_mile ->
                        obs.distanceKm = 0.4
                    R.id.option_half_mile ->
                        obs.distanceKm = 0.8
                    R.id.option_three_forth_mile ->
                        obs.distanceKm = 1.2
                    R.id.option_one_mile ->
                        obs.distanceKm = 1.6
                    R.id.option_greater_mile ->
                        obs.distanceKm = 2.0
                    else ->
                        obs.distanceKm = null
                }
            }
        }

        countOptions.setOnCheckedChangeListener { _, id ->
            val obs = observations[adapterPosition]
            if (obs is Sighting) {
                when (id) {
                    R.id.option_one_count ->
                        obs.count = 1
                    R.id.option_two_count ->
                        obs.count = 2
                    R.id.option_three_count ->
                        obs.count = 3
                    R.id.option_four_count ->
                        obs.count = 4
                    R.id.option_five_seven_count ->
                        obs.count = 6
                    R.id.option_eight_ten_count ->
                        obs.count = 9
                    R.id.option_eleven_thirteen_count ->
                        obs.count = 12
                    R.id.option_greater_thirteen_count ->
                        obs.count = 14
                    else ->
                        obs.count = null
                }
            }
        }
    }

    override fun display(obs: Observation) {
        if (obs is Sighting) {
            if (obs.count != null) {
                when {
                    obs.count == 1 -> countOptions.check(R.id.option_one_count)
                    obs.count == 2 -> countOptions.check(R.id.option_two_count)
                    obs.count == 3 -> countOptions.check(R.id.option_three_count)
                    obs.count == 4 -> countOptions.check(R.id.option_four_count)
                    obs.count in 5..7 -> countOptions.check(R.id.option_five_seven_count)
                    obs.count in 8..10 -> countOptions.check(R.id.option_eight_ten_count)
                    obs.count in 11..13 -> countOptions.check(R.id.option_eleven_thirteen_count)
                    obs.count!! > 13 -> countOptions.check(R.id.option_greater_thirteen_count)
                    else -> countOptions.clearCheck()
                }
            } else {
                countOptions.clearCheck()
            }

            if (obs.distanceKm != null) {
                when {
                    obs.distanceKm!! <= 0.06 -> distanceOptions.check(R.id.option_with_50m)
                    obs.distanceKm!! <= 0.11 -> distanceOptions.check(R.id.option_100m)
                    obs.distanceKm!! <= 0.21 -> distanceOptions.check(R.id.option_200m)
                    obs.distanceKm!! <= 0.31 -> distanceOptions.check(R.id.option_300m)
                    obs.distanceKm!! <= 0.41 -> distanceOptions.check(R.id.option_quarter_mile)
                    obs.distanceKm!! <= 0.81 -> distanceOptions.check(R.id.option_half_mile)
                    obs.distanceKm!! <= 1.21 -> distanceOptions.check(R.id.option_three_forth_mile)
                    obs.distanceKm!! <= 1.61 -> distanceOptions.check(R.id.option_one_mile)
                    obs.distanceKm!! > 1.61 -> distanceOptions.check(R.id.option_greater_mile)
                    else -> distanceOptions.clearCheck()
                }
            } else {
                distanceOptions.clearCheck()
            }

            if (obs.bearing != null) {
                when {
                    obs.bearing in 180..321 -> bearingOptions.check(R.id.option_nine)
                    obs.bearing in 322..336 -> bearingOptions.check(R.id.option_10)
                    obs.bearing in 337..351 -> bearingOptions.check(R.id.option_11)
                    obs.bearing!! > 351 || obs.bearing!! < 7 -> bearingOptions.check(
                        R.id.option_12
                    )
                    obs.bearing in 7..21 -> bearingOptions.check(R.id.option_1)
                    obs.bearing in 22..36 -> bearingOptions.check(R.id.option_2)
                    obs.bearing in 37..179 -> bearingOptions.check(R.id.option_3)
                    else -> bearingOptions.clearCheck()
                }
            } else {
                bearingOptions.clearCheck()
            }

            when (obs.groupType) {
                GroupType.MC -> groupType.check(R.id.option_mc)
                GroupType.MCE -> groupType.check(R.id.option_mce)
                GroupType.CG -> groupType.check(R.id.option_cg)
                GroupType.UNKNOWN -> groupType.check(R.id.option_unknown)
                else -> groupType.clearCheck()
            }
        }
    }
}
