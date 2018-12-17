# Description

This repository contains the source code of a software application developed for computer science master course. This application is able to generate 3D models of physical objects from a series of images taken from different angles. The image below shows the general procedure followed by the software. First, calibration and object images are taken from a camera and stored in the application. Then, calibration parameters and object silhouettes are computed. Next, using the previously computed parameters and silhouettes, an octree model is generated. Finally, the octree model is rendered at different resolution levels and using different display options. This application uses Java 8, JavaFX, and OpenCV.


![Process diagram](./docs_images/process-diagram.png)

## Calibration and object images

In order to generate a 3D model of an object, several images taken at different angles are used by application. To generate good 3D models, the octree model generation algorithm needs to extract measures (angles and distances) of the real world from the images. That is for each object image a corresponding calibration image is taken. These calibration images must be taken at the same angle as the object image. The figure below shows pairs of objects and calibration images taken at different angles (0, 60, and 120 degrees). For this project, a turntable was used to facilitate the image acquisition procedure. 

![Process diagram](./docs_images/ObjectAndChessboardPairs.png)

## Camera Calibration

The camera calibration procedure uses calibration images to compute camera parameters of the images. The are two types of camera parameters: intrinsic parameters and extrinsic parameters. From these parameters, a projection matrix can be computed which can be used to map real world coordinates to pixel coordinates and the other way around. The intrinsic parameters contain focal length, optical center, and skew coefficients. These parameters are dependent on the camera device. The only need to be computed once. The extrinsic parameters consist of rotation and translation vectors. These vectors need to be computed each time the camera moves with respect to the object being photographed. For this project, the intrinsic parameters were computed using the [https://www.mathworks.com/help/vision/ref/cameracalibrator-app.html](MATLAB camera calibration app). For each calibration image, using the intrinsic parameters, rotation and translation vectors are computed using OpenCV. After computing the required intrinsic and extrinsic parameters, projection matrices are computed and mapped to the corresponding object silhouettes to later generate an octree model.

For a more in depth explanation about camera parameters, look at the following resources:
* [https://docs.opencv.org/3.3.1/d4/d94/tutorial_camera_calibration.html](Camera calibration with OpenCV)
* [http://www.cs.cmu.edu/~16385/s17/] (Lecture slides from section: 8. Multi-View Geometry)
* [https://www.mathworks.com/help/vision/ug/camera-calibration.html] (MatWorks camera calibration tutorial)


## Silhouette extraction
The octree model generation algorithm uses silhouettes to create the 3D model of the object. Several silhouette extraction algorithms were tried during the development of this project. In the end, the following procedure was chosen:

1. Convert the object image to grayscale and apply a simple binarization method using an appropriate threshold.
2. Remove noise from the binary image using a combination of morphological image processing techniques.
3. Compute connected components on the binary image and delete elements that are not connected to elements in the center of the image. This procedure removes elements that were considered important by the thresholding binarization procedure but are not part of the object of interest.

In case the default threshold value does not produce good silhouettes, this value can be adjusted in the **Silhouettes Config Tab** of the application.


![Process diagram](./docs_images/ObjectsAndSilhouettes.png)

## Octree Model Generation
This part of the process is responsible for generating the octree data structure that represents a 3D model of a physical object. The camera parameters and object silhouettes previously computed are used as inputs to the octree generation algorithm. Overall, the octree model generation algorithm works as follows:

1. Create a octree with three levels of depth containing all of its internal nodes and leafs colored as black.
2. Using the computed camera parameters, for a given silhouette, project each black leaf into a 2D image and compute a bounding box.
3. Change the color of the leaf using the following rules: assign a white color if the bounding box is outside of the silhouette; assign a gray color if the bounding box is partially inside the silhouette and the previous color is black or gray; and keep the current color if the bounding box is completely inside the silhouette.
4. Repeat steps 2 and 3 for the remaining silhouettes.
5. Recursively divide the gray leafs to internal nodes with black leafs and go back to step 2 until the maximum depth level is reached.

For a more detailed description of this procedure, read the following article: [https://pdfs.semanticscholar.org/6b04/58ebe30555ebebc31e85f85845fef2be17f4.pdf](Rapid Octree Construction from Image Sequences).


## Model Rendering

Once the octree model is computed, a simple 3D engine written using the JavaFX 3D libraries is used to allow users to view and interact with the generated model. The generated 3D models are rendered using Voxels. Thus, the models look a bit like minecraft objects. However, the models are displayed with enough details by selecting an appropriate number of resolution levels. The maximum number of resolution levels corresponds to the maximum depth of the computed octree. The figures below show a cup displayed at different resolution levels. Notice how the shape looks more realistic as the number of levels is increased.  

![Render model with different levels](./docs_images/OctreeModelWithDifferentLevels.png)

In addition to selecting the number of resolution levels, it is also possible to select the way voxels are rendered. Below you can see how a model can be displayed only using black voxels, using black and gray voxels, using transparent voxels with colored edges for black and grey voxels, and using random colors for black voxels.

![Voxel coloring](./docs_images/VoxelColoring.png)

# How to run this project
## Install project dependencies
First, you have to install the following dependencies:

1. Install the Java SE Development Kit 8 from this webpage: https://docs.oracle.com/javase/8/docs/technotes/guides/install/install_overview.html.
2. Install the appropriate Eclipse IDE for Java developers for your operative system: https://www.eclipse.org/downloads/packages/installer
3. Install OpenCV for Java: https://opencv-java-tutorials.readthedocs.io/en/latest/01-installing-opencv-for-java.html#introduction-to-opencv-for-java.
4. Clone the following repository: https://github.com/jacordero/3dorfi.git.

## Configure project (eclipse)
After you have the required dependencies installed, load and compile the application's source code using the Eclipse IDE.

1. Open the Eclipse IDE and import the *3dorfi/ObjectReconstructor* folder as a project. The import projects tab is enabled by selecting the following options: *File* > *Import* > *General* > *Existing projects into workspace*.
2. Select *ObjectReconstructor* as the root directory of the project and click Finish.

![Import project](./docs_images/ImportProject.png)
After the project is loaded, the OpenCV library must be added to the project build path. This can be done as follows:
1. Open the Java Build Path window by doing: *Right click on the project* > *Build Path* > *Configure Build Path*.
2. Click on *Add External JARs* (*Libraries* tab) and select the location of the opencv-3xx.jar file.
3. Update the *Native library location* of the opencv-3xx.jar file.
4. Click *OK* to finalize the configuration.


![Buil Path Conf](./docs_images/BuildPathConf.png)

## Run examples
To use the application, run the *nl.tue.vc.application.ObjectReconstructor.java* as a Java application. <br />

To see examples of 3D models:
* First, enable the **Select 3D Test Model** button.
* Then, from the dropdown menu next to the checkbox button, select one of the following options: **Charger**, **Cup** or **Hexagon**.
* Finally, click the **Generate 3D Test Model** button.

![Object Reconstructor](./docs_images/ObjectReconstructor.png)
