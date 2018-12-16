# Description
This repository contains an application that semi-automatically generates 3D models of physical objects from a series of images taken from different viewpoints. The image below shows the 3D model generation process. Each step of this process is described below.

![Process diagram](./docs_images/process-diagram.png)

## Images

Talk about the turntable.
In order to generate 3D models, the application uses two types of images: a calibration image and an object image. Each one of the object images used in the application must be paired with a corresponding calibration image. Our application uses 12 pairs of calibration and object images to generate 3D models. A chessboard pattern of 10x7 is used for the calibration images. The images below show two pairs of calibration and object images. These pairs correspond to  The calibration images are used to compute the extrinsic parameters of the camera used to capture the object images. 

![Process diagram](./docs_images/ObjectAndChessboardPairs.png)

## Camera Calibration

## Silhouette extraction

![Process diagram](./docs_images/ObjectsAndSilhouettes.png)

## Octree Model Generation

## Model Rendering

![Render model with different levels](./docs_images/OctreeModelWithDifferentLevels.png)

![Voxel coloring](./docs_images/VoxelColoring.png)

# How to run this project
## Install project dependencies
The following instructions explain how to install the project dependencies.

1. Install the Java SE Development Kit 8 following this webpage's instructions: https://docs.oracle.com/javase/8/docs/technotes/guides/install/install_overview.html.

2. Install the appropriate Eclipse IDE for Java developers according to your operative system. Instructions for installing Eclipse can be found in the following URL: https://www.eclipse.org/downloads/packages/installer
3. Install OpenCV for Java following this webpage's instructions: https://opencv-java-tutorials.readthedocs.io/en/latest/01-installing-opencv-for-java.html#introduction-to-opencv-for-java.

4. Using git, clone the following repository https://github.com/jacordero/3dorfi.git.

## Configure project (eclipse)
The following instructions explain how to load and compile the source code of the application using the Eclipse IDE.

First, open the Eclipse IDE and import the *3dorfi/ObjectReconstructor* folder as a project. The import projects tab is enabled by selecting the following options: File > Import > General > Existing projects into workspace. Then, select *ObjectReconstructor* as the root directory of the project and click Finish.

![Import project](./docs_images/ImportProject.png)

After the project is loaded, the project's build path is configured to add the previously built OpenCV library. To open the Java Build Path window do: Right click on the project > Build Path > Configure Build Path. In the *Libraries* tab click on *Add External JARs* and select the location of the opencv-3xx.jar file. Then, update the *Native library location* of the opencv-3xx.jar file. Finally, click *OK* to finalize the configuration.

![Buil Path Conf](./docs_images/BuildPathConf.png)

## Run examples
To use the application, run the *nl.tue.vc.application.ObjectReconstructor.java* as a Java application. To see examples of 3D models, first enable the **Select 3D Test Model** button, then select **Charger**, **Cup** or **Hexagon** from the dropdown menu next to the checkbox button, and finally click the "Generate 3D Test Model" button.

![Object Reconstructor](./docs_images/ObjectReconstructor.png)
