package com.aman.smartplantdoctor

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.TextView
import android.os.Handler
import android.os.Looper
import kotlin.random.Random

class HomeActivity : AppCompatActivity() {
    private val leafHandler = Handler(Looper.getMainLooper())
    private val leafRunnable = object : Runnable {
        override fun run() {
            startFallingLeafAnimation()
            leafHandler.postDelayed(this, 30000) // Every 30 seconds
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)
        
        // Welcome message animation
        findViewById<TextView>(R.id.welcomeOverlay).apply {
            val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
            text = "Welcome Home, ${user?.displayName ?: "Doctor"}! 🏡"
            animate().alpha(1f).setDuration(1000).withEndAction {
                animate().alpha(0f).setDuration(1000).setStartDelay(1500).start()
            }.start()
        }

        val homeRoot = findViewById<androidx.constraintlayout.widget.ConstraintLayout>(R.id.homeRoot)
        val bottomNav = findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottomNavigation)

        ViewCompat.setOnApplyWindowInsetsListener(homeRoot) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            
            // Apply top padding to the root to avoid status bar overlap
            v.setPadding(0, systemBars.top, 0, 0)
            
            // Apply bottom margin to the BottomNavigationView to stay above system navigation bar
            val params = bottomNav.layoutParams as androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
            params.bottomMargin = systemBars.bottom + (24 * resources.displayMetrics.density).toInt()
            bottomNav.layoutParams = params

            insets
        }

        findViewById<MaterialCardView>(R.id.cardScan).setOnClickListener {
            val intent = Intent(this, ScanActivity::class.java)
            startActivity(intent)
        }

        findViewById<MaterialCardView>(R.id.cardChat).setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java)
            startActivity(intent)
        }

        findViewById<MaterialCardView>(R.id.cardHistory).setOnClickListener {
            val intent = Intent(this, HistoryActivity::class.java)
            startActivity(intent)
        }

        findViewById<MaterialCardView>(R.id.cardGarden).setOnClickListener {
            val intent = Intent(this, GardenActivity::class.java)
            startActivity(intent)
        }

        // Start the leaf animation loop
        leafHandler.post(leafRunnable)

        setupBottomNavigation()
    }

    private fun setupBottomNavigation() {
        val navView = findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottomNavigation)
        
        navView.setOnItemSelectedListener { item ->
            // Unique "Pop" animation on selection
            val itemView = navView.findViewById<android.view.View>(item.itemId)
            itemView?.animate()?.scaleX(1.15f)?.scaleY(1.15f)?.setDuration(150)?.withEndAction {
                itemView.animate()?.scaleX(1.0f)?.scaleY(1.0f)?.setDuration(150)?.start()
            }?.start()

            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_scan -> {
                    startActivity(Intent(this, ScanActivity::class.java))
                    false // Don't highlight since it's a new activity
                }
                R.id.nav_garden -> {
                    startActivity(Intent(this, GardenActivity::class.java))
                    false
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    false
                }
                else -> false
            }
        }
    }

    private fun startFallingLeafAnimation() {
        val rootView = findViewById<androidx.constraintlayout.widget.ConstraintLayout>(R.id.homeRoot) ?: return
        
        // Ensure view is laid out to get valid dimensions
        rootView.post {
            val width = rootView.width
            val height = rootView.height
            if (width <= 0 || height <= 0) return@post
            
            repeat(8) {
                val leaf = ImageView(this).apply {
                    setImageResource(R.drawable.ic_leaf) 
                    alpha = 0.6f
                    val size = Random.nextInt(40, 80)
                    layoutParams = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams(size, size)
                    translationX = Random.nextInt(0, width).toFloat()
                    translationY = -100f
                    rotation = Random.nextInt(0, 360).toFloat()
                }
                
                rootView.addView(leaf)

                val duration = Random.nextLong(6000, 10000)
                leaf.animate()
                    .translationY(height.toFloat() + 100f)
                    .rotationBy(Random.nextInt(360, 1080).toFloat())
                    .translationXBy(Random.nextInt(-200, 200).toFloat())
                    .setDuration(duration)
                    .setInterpolator(LinearInterpolator())
                    .withLayer() // Enable hardware layer for smoothness
                    .withEndAction {
                        rootView.removeView(leaf)
                    }
                    .start()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        leafHandler.removeCallbacks(leafRunnable)
    }
}
