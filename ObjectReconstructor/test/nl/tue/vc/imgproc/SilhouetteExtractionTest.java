package nl.tue.vc.imgproc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import org.opencv.core.Core;
import org.opencv.core.Mat;

import nl.tue.vc.imgproc.ConcurrentSilhouetteExtractor;
import nl.tue.vc.imgproc.SegmentedImageStruct;

public class SilhouetteExtractionTest {

	
	
	public void completableFutureTest(){
		Map<String, Mat> objectImagesMap = ImageLoader.loadObjectImages();
				
		long lStartTime = System.nanoTime();
		
		int nImages = objectImagesMap.size();
		List<CompletableFuture<SegmentedImageStruct>> futures = new ArrayList<CompletableFuture<SegmentedImageStruct>>();

		int binaryThreshold = 105;
		String method = "Binarization";
		for (String imageKey: objectImagesMap.keySet()){
			Mat image = objectImagesMap.get(imageKey);
			ConcurrentSilhouetteExtractor cse = new ConcurrentSilhouetteExtractor(image, imageKey, method, binaryThreshold);
			CompletableFuture<SegmentedImageStruct> future = CompletableFuture.supplyAsync(() -> cse.call());
			futures.add(future);
		}
		
		CompletableFuture<Map<String, Mat>> combinedFuture = CompletableFuture.allOf(futures.toArray(new CompletableFuture[nImages]))
				.thenApply(v -> futures.stream()
				.map(CompletableFuture::join)
				.collect(Collectors.toMap(x -> x.getImageName(), x -> x.getImage())));
		
		try {
			Map<String, Mat> binaryImages = combinedFuture.get();
			for (String imageKey: binaryImages.keySet()){
				System.out.println("Recovering: " + imageKey);				
			}
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
				
		long lEndTime = System.nanoTime();
		long output = (lEndTime - lStartTime) / 1000000000;
		System.out.println("The task was finished in: " + output + " seconds!!!");

	}
	

	public void callableTest(){
		Map<String, Mat> objectImagesMap = ImageLoader.loadObjectImages();
		Map<String, Mat> segmentedImagesMap = new HashMap<String, Mat>();
		List<Future<SegmentedImageStruct>> segmentationImageTasks = new ArrayList<Future<SegmentedImageStruct>>();
		List<Boolean> completedTasks = new ArrayList<Boolean>();
		
		long lStartTime = System.nanoTime();

		ExecutorService executor = Executors.newFixedThreadPool(4);
		int binaryThreshold = 105;
		String method = "Binarization";
		for (String imageKey: objectImagesMap.keySet()){
			Mat image = objectImagesMap.get(imageKey);
			ConcurrentSilhouetteExtractor cse = new ConcurrentSilhouetteExtractor(image, imageKey, method, binaryThreshold);
			Future<SegmentedImageStruct> future = executor.submit(cse);
			segmentationImageTasks.add(future);
			completedTasks.add(Boolean.FALSE);
		}
		
		// TODO: change this way of checking for completion of all tasks
		boolean allDone = false;
		while(!allDone){
			int finishedTasks = 0;
			for (Future<SegmentedImageStruct> task: segmentationImageTasks){
				if (task.isDone()){
					finishedTasks++;
				}
			}	
			if (finishedTasks >= objectImagesMap.size()){
				allDone = true;
			}
		}
		
		// Store results of the segmentation procedure
		for (Future<SegmentedImageStruct> task: segmentationImageTasks){
			SegmentedImageStruct struct;
			try {
				struct = task.get();
				segmentedImagesMap.put(struct.getImageName(), struct.getImage());			
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		}

		long lEndTime = System.nanoTime();
		long output = (lEndTime - lStartTime) / 1000000000;
		System.out.println("The task was finished in: " + output + " seconds!!!");
		
	}
	
	
	public static void main(String[] args){
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

		SilhouetteExtractionTest set = new SilhouetteExtractionTest();
		set.completableFutureTest();
		
	}
}
