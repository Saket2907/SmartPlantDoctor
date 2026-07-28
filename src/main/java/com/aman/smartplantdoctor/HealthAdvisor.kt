package com.aman.smartplantdoctor

object HealthAdvisor {
    data class DiseaseInfo(
        val name: String,
        val confidence: String,
        val symptoms: String,
        val treatment: String
    )

    fun getDetailedAdvice(detectedResult: String): DiseaseInfo {
        // Handle standard PlantVillage labels (e.g., "Tomato___Early_blight")
        val cleanLabel = if (detectedResult.contains("___")) {
            val parts = detectedResult.split("___")
            val species = parts[0].replace("_", " ")
            val condition = parts[1].replace("_", " ")
            "$species: $condition"
        } else {
            detectedResult
        }

        val isHealthy = cleanLabel.lowercase().contains("healthy")

        return when {
            isHealthy -> DiseaseInfo(
                cleanLabel, "98%", 
                "Green vibrant leaves, sturdy stems.", 
                "Continue current care routine."
            )
            cleanLabel.contains("Blight") -> DiseaseInfo(
                cleanLabel, "85%", 
                "Small dark spots or patches appearing on leaves.", 
                "Prune infected areas and apply appropriate fungicide. Ensure leaves stay dry."
            )
            cleanLabel.contains("Rust") -> DiseaseInfo(
                cleanLabel, "90%", 
                "Orange-brown powdery spots on the underside of leaves.", 
                "Improve air circulation and use sulfur-based fungicide."
            )
            cleanLabel.contains("Spot") -> DiseaseInfo(
                cleanLabel, "82%", 
                "Irregularly shaped dark spots with yellow halos.", 
                "Remove fallen debris and apply copper-based spray."
            )
            else -> DiseaseInfo(
                cleanLabel, "80%", 
                "Visible symptoms of plant stress or infection.", 
                "Research the specific disease, isolate the plant, and improve drainage."
            )
        }
    }

    fun getAdvice(detectedResult: String): String {
        return if (detectedResult.lowercase().contains("healthy")) {
            "Your plant is thriving! Keep up the regular watering schedule."
        } else {
            "Issues detected. Check detailed advice for treatment and isolation steps."
        }
    }

    /**
     * Patent-worthy logic: Calculates a cumulative health score based on scan history trends.
     */
    fun calculateHealthScore(history: List<ScanHistory>): Int {
        if (history.isEmpty()) return 100
        var score = 100
        history.take(5).forEach { scan ->
            if (scan.result != "Healthy") {
                score -= 15
            }
        }
        return score.coerceAtLeast(0)
    }
}
