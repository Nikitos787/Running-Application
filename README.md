# Running Application

A simple Android application for tracking users runs. The application follows the MVVM architectural pattern and incorporates the following key technologies:

## Features

- RecyclerView: Uses RecyclerView for displaying a list of runs with scrollable functionality.
- Navigation: Utilizes the Android Navigation component for managing fragments and transitions between screens.
- Room: Library for working with SQLite database at the application level, simplifying the handling of local data.
- ViewModel: represent a bridge between ui and data class and holding state for data
- Fragment: represent screen that show different logic base on navigation action
- Google map: Uses for show distance what user is walked(ran)
- Foreground service: For tracking user movement in background with notification
- Glide for show image in recycler view
- Timber: for logging
- Hilt: for implementing dependency injection patter into project
- MPAndroidChart: for showing statistic in chart format

## Architecture

The application is developed following the MVVM (Model-View-ViewModel) architectural pattern. Each screen in the application is represented as a fragment, interacting with respective ViewModels to manage business logic and data presentation.

## Technologies

- Kotlin;
- AndroidX;
- Room;
- Navigation;
- Glide;
- GMS;
- Timber;
- Dagger Hilt
- MPAndroidChart

## Installation

To build and run the application, follow these steps:

1. Clone the repository to your computer.
2. Get API KEY for Maps SDK and put it to resource file string (destination you can find in Manifest)
3. Open the project in your preferred development environment (e.g., Android Studio). 
4. Run the project on your emulator or physical device.
# Running-Application
