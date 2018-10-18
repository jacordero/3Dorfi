package nl.tue.vc.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.opencv.core.Mat;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import nl.tue.vc.application.utils.Utils;
import nl.tue.vc.model.Octree;

public class SidePanelImageSelector {

	private List<Image> imagesToDisplay;
	private ObservableList<String> imagesNames;
	private ListView<String> imageSelectionView;
	private ImageView imageDisplayView;
	private Map<String, Integer> imagesDescription;
	private String selectedImageId;
	private String panelId;
	private static final Logger logger = Logger.getLogger(SidePanelImageSelector.class.getName());
	
	public SidePanelImageSelector(ListView<String> imagesView, ImageView displayView, String panelId){
		imageSelectionView = imagesView;
		imageDisplayView = displayView;
		imagesToDisplay = new ArrayList<Image>();
		imagesNames = FXCollections.observableArrayList();
		imagesDescription = new HashMap<>();
		selectedImageId = null;
		this.panelId = panelId;
	}
	
	public void showImagesForSelection(VBox sidePanelArea) {
			
		imageSelectionView.setItems(imagesNames);
		imageSelectionView.setCellFactory(param -> {
			ListCell<String> cell = new ListCell<String>() {
				private ImageView imageView = new ImageView();

				@Override
				public void updateItem(String name, boolean empty) {
					super.updateItem(name, empty);
					if (empty) {
						setText(null);
						setGraphic(null);
					} else {
						int imagePosition = imagesDescription.get(name);
						imageView.setImage(imagesToDisplay.get(imagePosition));
						imageView.setFitWidth(100);
						imageView.setPreserveRatio(true);
						setText(name);
						setGraphic(imageView);
						
						if (imageDisplayView.getImage() == null) {
							showSelectedImage(imagesToDisplay.get(imagePosition));
							selectedImageId = name;
						}
					}
				}
			};

			cell.setOnMouseClicked(e -> {
				if (cell.getItem() != null) {
					selectedImageId = cell.getText();
					int imagePosition = imagesDescription.get(selectedImageId);
					showSelectedImage(imagesToDisplay.get(imagePosition));
					logger.log(Level.INFO, "Click on image " + selectedImageId);
				}
			});

			return cell;
		});
		
		sidePanelArea.getChildren().clear();
		sidePanelArea.getChildren().add(imageSelectionView);
	}

	public void updateImageInfo(Image imageToDisplay, String imageName){
		if (imagesDescription.containsKey(imageName)){
			imagesToDisplay.add(imageToDisplay);
		} else {
			imagesToDisplay.add(imageToDisplay);
			imagesNames.add(imageName);
			imagesDescription.put(imageName, imagesNames.size() - 1);
		}
		if (selectedImageId == null){
			selectedImageId = imageName;
		}
	}
	
	public void addImageInfo(Image imageToDisplay, String imageName, int imagePosition){
		imagesToDisplay.add(imageToDisplay);
		imagesNames.add(imageName);
		imagesDescription.put(imageName, imagePosition);
		if (selectedImageId == null){
			selectedImageId = imageName;
		}
	}

	public void showSelectedImage(Image image) {
		imageDisplayView.setImage(image);
		imageDisplayView.setFitWidth(500);
		imageDisplayView.setPreserveRatio(true);
	}
	
	public void clearImages(ListView<String> imagesView){
		imageSelectionView = imagesView;
		imagesToDisplay = new ArrayList<Image>();
		imagesNames = FXCollections.observableArrayList();
		imagesDescription = new HashMap<>();
		selectedImageId = null;
	}
	
}
