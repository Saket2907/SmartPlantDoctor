package com.aman.smartplantdoctor

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter

class GardenAdapter(
    private val onWaterClick: (Plant) -> Unit
) : ListAdapter<Plant, GardenAdapter.GardenViewHolder>(PlantDiffCallback()) {

    class GardenViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.gardenPlantName)
        val score: TextView = view.findViewById(R.id.gardenHealthScore)
        val healthProgress: com.google.android.material.progressindicator.LinearProgressIndicator = view.findViewById(R.id.healthProgress)
        val watering: TextView = view.findViewById(R.id.gardenWateringInfo)
        val btnWatered: View = view.findViewById(R.id.btnWatered)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GardenViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_garden, parent, false)
        return GardenViewHolder(view)
    }

    override fun onBindViewHolder(holder: GardenViewHolder, position: Int) {
        val plant = getItem(position)
        holder.name.text = plant.name
        holder.score.text = holder.itemView.context.getString(R.string.health_score_format, plant.healthScore)
        holder.healthProgress.progress = plant.healthScore
        
        val nextWaterTime = plant.lastWateredTimestamp + (plant.wateringIntervalDays * 24 * 60 * 60 * 1000L)
        val daysLeft = ((nextWaterTime - System.currentTimeMillis()) / (24 * 60 * 60 * 1000L)).toInt()
        
        holder.watering.text = when {
            daysLeft < 0 -> "Watering overdue!"
            daysLeft == 0 -> "Water today"
            else -> "Water in $daysLeft days"
        }

        holder.btnWatered.setOnClickListener { onWaterClick(plant) }
    }

    class PlantDiffCallback : DiffUtil.ItemCallback<Plant>() {
        override fun areItemsTheSame(oldItem: Plant, newItem: Plant): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Plant, newItem: Plant): Boolean = oldItem == newItem
    }
}