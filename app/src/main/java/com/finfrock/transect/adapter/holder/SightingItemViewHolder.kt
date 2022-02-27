package com.finfrock.transect.adapter.holder

import android.view.View
import android.widget.ImageView
import android.widget.RadioGroup
import com.finfrock.transect.R
import com.finfrock.transect.model.*
import com.finfrock.transect.view.ToggleButtonGroupTableLayout

class SightingItemViewHolder(view: View,
                             private val observations: ObservationBuilder
): ObservationItemViewHolder(view)  {
    private val countOptions: ToggleButtonGroupTableLayout = view.findViewById(R.id.count_options)
    private val countOptionsErrorIcon: ImageView = view.findViewById(R.id.icon_count_error)
    private val distanceOptions: ToggleButtonGroupTableLayout = view.findViewById(R.id.distance_options)
    private val distanceOptionsErrorIcon: ImageView = view.findViewById(R.id.icon_distance_error)
    private val bearingOptions: ToggleButtonGroupTableLayout = view.findViewById(R.id.bearing_options)
    private val bearingOptionsErrorIcon: ImageView = view.findViewById(R.id.icon_bearing_error)
    private val groupType: RadioGroup = view.findViewById(R.id.group_type_options)
    private val groupTypeOptionsErrorIcon: ImageView = view.findViewById(R.id.icon_group_type_error)

    init {
        groupType.setOnCheckedChangeListener { _, id -> observations.update(adapterPosition) { obs ->
                if (obs is SightingMutable) {
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

                    if (obs.groupType != null) {
                        groupTypeOptionsErrorIcon.visibility = View.INVISIBLE
                    } else {
                        groupTypeOptionsErrorIcon.visibility = View.VISIBLE
                    }
                }

                obs
            }
        }

        bearingOptions.setOnCheckedChangeListener { _, id -> observations.update(adapterPosition) { obs ->
                if (obs is SightingMutable) {
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

                    if (obs.bearing != null) {
                        bearingOptionsErrorIcon.visibility = View.INVISIBLE
                    } else {
                        bearingOptionsErrorIcon.visibility = View.VISIBLE
                    }
                }
                obs
            }
        }

        distanceOptions.setOnCheckedChangeListener { _, id -> observations.update(adapterPosition) { obs ->
                if (obs is SightingMutable) {
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
                    if (obs.distanceKm != null) {
                        distanceOptionsErrorIcon.visibility = View.INVISIBLE
                    } else {
                        distanceOptionsErrorIcon.visibility = View.VISIBLE
                    }
                }

                obs
            }
        }

        countOptions.setOnCheckedChangeListener { _, id -> observations.update(adapterPosition) { obs ->
                if (obs is SightingMutable) {
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
                        else -> {
                            obs.count = null
                            countOptionsErrorIcon.visibility = View.VISIBLE
                        }
                    }

                    if (obs.count != null) {
                        countOptionsErrorIcon.visibility = View.INVISIBLE
                    } else {
                        countOptionsErrorIcon.visibility = View.VISIBLE
                    }
                }
                obs
            }
        }
    }

    override fun display(obs: ObservationNullable) {
        if (obs is SightingNullable) {
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
                countOptionsErrorIcon.visibility = View.INVISIBLE
            } else {
                countOptionsErrorIcon.visibility = View.VISIBLE
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
                distanceOptionsErrorIcon.visibility = View.INVISIBLE
            } else {
                distanceOptions.clearCheck()
                distanceOptionsErrorIcon.visibility = View.VISIBLE
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
                bearingOptionsErrorIcon.visibility = View.INVISIBLE
            } else {
                bearingOptions.clearCheck()
                bearingOptionsErrorIcon.visibility = View.VISIBLE
            }

            when (obs.groupType) {
                GroupType.MC -> groupType.check(R.id.option_mc)
                GroupType.MCE -> groupType.check(R.id.option_mce)
                GroupType.CG -> groupType.check(R.id.option_cg)
                GroupType.UNKNOWN -> groupType.check(R.id.option_unknown)
                else -> groupType.clearCheck()
            }

            if (obs.groupType != null) {
                groupTypeOptionsErrorIcon.visibility = View.INVISIBLE
            } else {
                groupTypeOptionsErrorIcon.visibility = View.VISIBLE
            }
        }
    }
}
