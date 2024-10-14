# Dream Doodler
Drawing app built for Android devices, built using Kotlin.

## Video Showcase of the app:

https://github.com/user-attachments/assets/15afa2c6-7141-435f-b0d5-02cf2c4179a4

## List of features
* Modifiable pen (color/size/shape)
* Invert the colors of the image
* Change brightness of the image
* Adjustable noise level
* Save & load image to/from device storage
* Share image with other users of the app, backed by Firebase
* Import image shared by other users

## Technical Implementation
* MVVM Architecture: The app uses Model-View-ViewModel for lifecycle awareness. Data persists across screen rotations and is stored in a ViewModel.
* Room Database: Room DB is used to persist user drawings even if the app is closed and reopened, ensuring a smooth user experience.
* Jetpack Navigation: The app employs Jetpack Navigation to seamlessly transition between different screens.
* Jetpack Compose: UI elements are built using Jetpack Compose, offering modern and declarative UI development for Android.
* C++ Image Processing: Image processing operations such as inverting colors, changing brightness, and adding noise are handled using C++ for performance efficiency.
* Firebase Integration: Firebase is used for sharing images with other users, requiring login and user authentication to ensure secure sharing.
* Automated Unit Tests: The project includes automated unit tests covering all features and potential bugs to ensure code reliability and robustness.
