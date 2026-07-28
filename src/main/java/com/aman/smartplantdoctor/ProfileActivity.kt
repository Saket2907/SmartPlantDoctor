package com.aman.smartplantdoctor

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class ProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)
        
        val user = FirebaseAuth.getInstance().currentUser
        findViewById<TextView>(R.id.profileName).text = user?.displayName ?: "Green Doctor"
        findViewById<TextView>(R.id.profileEmail).text = user?.email ?: "Join our community"

        findViewById<MaterialButton>(R.id.btnLogout).setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        loadStats()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun loadStats() {
        val database = AppDatabase.getDatabase(this)
        lifecycleScope.launch {
            // Fetch data in parallel or sequentially but away from main thread
            val scans = database.scanDao().getAllScans()
            val plants = database.plantDao().getAllPlants()
            
            // Batch UI updates
            val healthyCount = scans.count { it.result == "Healthy" }
            
            findViewById<TextView>(R.id.statScans).text = scans.size.toString()
            findViewById<TextView>(R.id.statHealthy).text = healthyCount.toString()
            findViewById<TextView>(R.id.statPlants).text = plants.size.toString()
        }
    }
}
