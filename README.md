# SmartPlantDoctor (Fungal Detection Device)

SmartPlantDoctor is an AI-powered plant disease diagnostic system that pairs a mobile app with a 3D-printed macro-lens smartphone attachment, enabling farmers and gardeners to detect fungal diseases in plant leaves instantly — no lab or expert required.

## Features

- **On-device AI diagnosis** — captures a leaf image and classifies fungal disease in under 5 seconds, fully offline
- **Custom hardware attachment** — a 3D-printed macro lens clips onto any smartphone camera for close-up, high-detail leaf imaging
- **ResNet-50 CNN model** trained on 18,450 annotated leaf images, achieving 85% classification accuracy across 5 fungal disease categories
- **4-stage image preprocessing pipeline** to improve image quality and model accuracy before inference
- **Firebase integration** for data storage and (optionally) syncing diagnosis history

## Tech Stack

- **Mobile App:** Flutter / Kotlin
- **AI/ML:** TensorFlow Lite, ResNet-50, Transfer Learning
- **Image Processing:** OpenCV, Python (model training)
- **Backend/Storage:** Firebase
- **Hardware:** 3D-printed macro-lens smartphone attachment

## How It Works

1. User attaches the 3D-printed macro lens to their smartphone camera.
2. The app captures a close-up image of the affected leaf.
3. The image passes through a 4-stage preprocessing pipeline (cropping, normalization, noise reduction, resizing).
4. The preprocessed image is run through the on-device TensorFlow Lite model (ResNet-50 based).
5. The app displays the predicted disease category, all processed offline in under 5 seconds.

## Model Details

- **Architecture:** ResNet-50 (Transfer Learning)
- **Dataset:** 18,450 annotated leaf images
- **Categories:** 5 fungal disease classes
- **Accuracy:** 85% on test set
- **Inference:** On-device via TensorFlow Lite, offline-capable

## Setup & Installation

```bash
# Clone the repository
git clone https://github.com/Saket2907/SmartPlantDoctor.git
cd SmartPlantDoctor

# Open in Android Studio
# Sync Gradle files
# Run on an emulator or physical device
```

## Hardware

The macro-lens attachment used for close-up leaf capture is a custom 3D-printed design. [Add link to design files / STL, if available]

## Author

**Saket Kumar**
[LinkedIn](https://linkedin.com/in/saket-kumar-tech) • [GitHub](https://github.com/Saket2907)
