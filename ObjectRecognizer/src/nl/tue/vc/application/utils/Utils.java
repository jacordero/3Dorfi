package nl.tue.vc.application.utils;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.scene.image.Image;

/**
 * Provide general purpose methods for handling OpenCV-JavaFX data conversion.
 * Moreover, expose some "low level" methods for matching few JavaFX behavior.
 *
 * @author <a href="mailto:luigi.derussis@polito.it">Luigi De Russis</a>
 * @author <a href="http://max-z.de">Maximilian Zuleger</a>
 * @version 1.1 (2017-03-10)
 * @since 1.0 (2016-09-17)
 *
 */
public final class Utils
{
	/**
	 * Convert a Mat object (OpenCV) in the corresponding Image for JavaFX
	 *
	 * @param frame
	 *            the {@link Mat} representing the current frame
	 * @return the {@link Image} to show
	 */
	public static Image mat2Image(Mat frame)
	{
		try
		{
			// create a temporary buffer
			MatOfByte buffer = new MatOfByte();
			// encode the frame in the buffer, according to the PNG format
			Imgcodecs.imencode(".png", frame, buffer);
			// build and return an Image created from the image encoded in the
			// buffer
			return new Image(new ByteArrayInputStream(buffer.toArray()));
			//return SwingFXUtils.toFXImage(matToBufferedImage(frame), null);
		}
		catch (Exception e)
		{
			// show the exception details
			System.err.println("Cannot convert the Mat object:");
			e.printStackTrace();

			return null;
		}
	}

	/**
	 * Generic method for putting element running on a non-JavaFX thread on the
	 * JavaFX thread, to properly update the UI
	 *
	 * @param property
	 *            a {@link ObjectProperty}
	 * @param value
	 *            the value to set for the given {@link ObjectProperty}
	 */
	public static <T> void onFXThread(final ObjectProperty<T> property, final T value)
	{
		Platform.runLater(() -> {
			property.set(value);
		});
	}

	/**
	 * Support for the {@link mat2image()} method
	 *
	 * @param original
	 *            the {@link Mat} object in BGR or grayscale
	 * @return the corresponding {@link BufferedImage}
	 */
	public static BufferedImage matToBufferedImage(Mat original)
	{
		// init
		BufferedImage image = null;
		int width = original.width(), height = original.height(), channels = original.channels();
		byte[] sourcePixels = new byte[width * height * channels];
		original.get(0, 0, sourcePixels);

		if (original.channels() > 1)
		{
			image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
		}
		else
		{
			image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
		}
		final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
		System.arraycopy(sourcePixels, 0, targetPixels, 0, sourcePixels.length);

		return image;
	}
	
	public static Mat bufferedImageToMat(BufferedImage bufferedImage){
	        Mat out;
	        byte[] data;
	        int r, g, b;

	        if (bufferedImage.getType() == BufferedImage.TYPE_INT_RGB) {
	            out = new Mat(bufferedImage.getHeight(), bufferedImage.getWidth(), CvType.CV_8UC3);
	            data = new byte[bufferedImage.getWidth() * bufferedImage.getHeight() * (int) out.elemSize()];
	            int[] dataBuff = bufferedImage.getRGB(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight(), 
	            		null, 0, bufferedImage.getWidth());
	            for (int i = 0; i < dataBuff.length; i++) {
	                data[i * 3] = (byte) ((dataBuff[i] >> 0) & 0xFF);
	                data[i * 3 + 1] = (byte) ((dataBuff[i] >> 8) & 0xFF);
	                data[i * 3 + 2] = (byte) ((dataBuff[i] >> 16) & 0xFF);
	            }
	        } else {
	            out = new Mat(bufferedImage.getHeight(), bufferedImage.getWidth(), CvType.CV_8UC1);
	            data = new byte[bufferedImage.getWidth() * bufferedImage.getHeight() * (int) out.elemSize()];
	            int[] dataBuff = bufferedImage.getRGB(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight(),
	            		null, 0, bufferedImage.getWidth());
	            for (int i = 0; i < dataBuff.length; i++) {
	                r = (byte) ((dataBuff[i] >> 0) & 0xFF);
	                g = (byte) ((dataBuff[i] >> 8) & 0xFF);
	                b = (byte) ((dataBuff[i] >> 16) & 0xFF);
	                data[i] = (byte) ((0.21 * r) + (0.71 * g) + (0.07 * b));
	            }
	        }
	        out.put(0, 0, data);
	        return out;
	}
	
	
	public static void debug(String str, boolean printInfo) {
		if (printInfo) {
			System.out.print(str);
		}
	}
	
	public static void debugNewLine(String str, boolean printInfo) {
		if (printInfo) {
			System.out.println(str);
		}
	}
}
