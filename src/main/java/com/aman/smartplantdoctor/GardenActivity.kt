package com.aman.smartplantdoctor

import android.os.Bundle
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class GardenActivity : AppCompatActivity() {
    private lateinit var database: AppDatabase
    private lateinit var gardenAdapter: GardenAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_garden)

        database = AppDatabase.getDatabase(this)
        val recyclerView = findViewById<RecyclerView>(R.id.gardenRecyclerView)
        gardenAdapter = GardenAdapter { plant ->
            lifecycleScope.launch {
                val updatedPlant = plant.copy(lastWateredTimestamp = System.currentTimeMillis())
                database.plantDao().updatePlant(updatedPlant)
                loadPlants()
            }
        }
        recyclerView.adapter = gardenAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        loadPlants()

        findViewById<FloatingActionButton>(R.id.addPlantFab).setOnClickListener {
            showAddPlantDialog()
        }
    }

    private fun loadPlants() {
        lifecycleScope.launch {
            val list = database.plantDao().getAllPlants()
            gardenAdapter.submitList(list)
            updateDashboard(list)
        }
    }

    private fun updateDashboard(plants: List<Plant>) {
        val emptyState = findViewById<android.view.View>(R.id.emptyState)
        if (plants.isEmpty()) {
            emptyState.visibility = android.view.View.VISIBLE
        } else {
            emptyState.visibility = android.view.View.GONE
        }

        val total = plants.size
        val avgHealth = if (plants.isNotEmpty()) plants.map { it.healthScore }.average().toInt() else 0
        val thirsty = plants.count { 
            val nextWaterTime = it.lastWateredTimestamp + (it.wateringIntervalDays * 24 * 60 * 60 * 1000L)
            System.currentTimeMillis() > nextWaterTime
        }

        findViewById<android.widget.TextView>(R.id.countTotalPlants).text = total.toString()
        findViewById<android.widget.TextView>(R.id.avgHealthScore).text = "$avgHealth%"
        findViewById<android.widget.TextView>(R.id.countThirstyPlants).text = thirsty.toString()
    }

    private fun showAddPlantDialog() {
        val layout = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(50, 20, 50, 0)
        }
        
        val nameInput = EditText(this).apply { hint = "Plant Name" }
        val intervalInput = EditText(this).apply { 
            hint = "Watering Interval (Days)"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }
        
        layout.addView(nameInput)
        layout.addView(intervalInput)
        
        AlertDialog.Builder(this)
            .setTitle(R.string.add_new_plant_title)
            .setView(layout)
            .setPositiveButton(R.string.add_button) { _, _ ->
                val name = nameInput.text.toString()
                val intervalStr = intervalInput.text.toString()
                val interval = intervalStr.toIntOrNull() ?: 3
                
                if (name.isNotEmpty()) {
                    lifecycleScope.launch {
                        database.plantDao().insertPlant(Plant(
                            name = name, 
                            species = "Unknown",
                            wateringIntervalDays = interval
                        ))
                        loadPlants()
                    }
                }
            }
            .setNegativeButton(R.string.cancel_button, null)
            .show()
    }
}
