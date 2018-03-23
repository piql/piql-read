package no.ntnu.bachelor2018.filmreader;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.widget.ImageView;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import filmreader.bacheloroppg.ntnu.no.filmreader.R;

/**
 * The capture class controls the camera used for preview, it configures the camera on the
 * device and repeatedly captures frames to show using an imageView.
 */
public class Capture {

	// Tag for this class
	private final String TAG = getClass().getSimpleName();

	private Activity activity;       // The context for the activity of creation
	private CameraManager cameraManager;  // Camera manager to get information about all cameras
	private CameraDevice cam;            // Object for one camera
	private CaptureRequest request;        // A request object for a camera device
	private CameraCaptureSession cSession;       // Globally storing the session to properly close it
	private SharedPreferences prefs;          // Get the preferences stored
	private String backCamID;      // The ID for the back camera
	private Size cSize;          // The image resolution of the picture to be taken
	private List<Surface> surfaces;       // The output surface to put the image
	private ImageReader img;            // Object for reading images
	private ImageView preview;        // View for the preview images
	private Bitmap bitmap;         // bitmap for the image to process
	private Reader reader;         // Reader object for processing an image
	private Thread t1;             // Thread for updating the preview image
	private Mat procImage;      // Image to be processed and viewed
	private Mat processedImage; // Image ready for viewing
	private byte[] byteArray;      // Byte buffer for image reading

	// The imageformat to use on captures, changing this will most likely break something else.
	private int format = ImageFormat.YUV_420_888;

	// A callback object for receiving updates about the state of a camera device.
	CameraDevice.StateCallback cameraDeviceStateCallback = new CameraDevice.StateCallback() {

		@Override
		public void onOpened(@NonNull CameraDevice camera) {
			Log.d(TAG, "Opened camera");

			// Save the camera object for further use
			cam = camera;
			preview = activity.findViewById(R.id.imageView);

			try {
				// Create an ImageReader object where we can properly read images
				img = ImageReader.newInstance(cSize.getWidth(), cSize.getHeight(), format, 2);

				// Whenever a new image is available
				img.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {

					@Override
					public void onImageAvailable(ImageReader reader) {
						onNewImageCapture(reader);
					}

				}, null);

				// We get a surface from the image which is the output
				Surface surface = img.getSurface();
				//Surface textureSurface = new Surface(texture);

				if (cam == null) {
					Log.e(TAG, "Cam is null");
				}
				// Build a capture request for the camera
				CaptureRequest.Builder requestBuilder = cam.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
				requestBuilder.addTarget(surface);
				request = requestBuilder.build();

				// Create a list of the surfaces
				surfaces = new ArrayList<>();
				surfaces.add(surface);
				//surfaces.add(textureSurface);

				// Send the surface to another callback
				cam.createCaptureSession(surfaces, cameraCaptureSessionStateCallback, null);
			} catch (CameraAccessException e) {
				// Automatically goes to onDisconnected()
				Log.e(TAG, "onOpened error");
			}

		}

		@Override
		public void onDisconnected(@NonNull CameraDevice camera) {
			Log.d(TAG, "Camera disconnected");
		}

		@Override
		public void onError(@NonNull CameraDevice camera, int error) {
			Log.e(TAG, "Could not open camera, error: " + String.valueOf(error));
			camera.close();

			// Print out the error, only for debugging
			switch (error) {
				case ERROR_CAMERA_DEVICE: // This is also when onDisconnect was run
					Log.e(TAG, "ERROR_CAMERA_DEVICE");
					break;
				case ERROR_CAMERA_DISABLED:
					Log.e(TAG, "ERROR_CAMERA_DISABLED");
					break;
				case ERROR_CAMERA_IN_USE:
					Log.e(TAG, "ERROR_CAMERA_IN_USE");
					break;
				case ERROR_CAMERA_SERVICE:
					Log.e(TAG, "ERROR_CAMERA_SERVICE");
					break;
				case ERROR_MAX_CAMERAS_IN_USE:
					Log.e(TAG, "ERROR_MAX_CAMERAS_IN_USE");
					break;
				default:
					Log.e(TAG, "UNKNOWN ERROR");
					break;
			}

			//errorDialog();
		}

	};

	// A callback object for receiving updates about the state of a camera capture session.
	CameraCaptureSession.StateCallback cameraCaptureSessionStateCallback = new CameraCaptureSession.StateCallback() {

		@Override
		public void onConfigured(@NonNull CameraCaptureSession session) {
			try {
				// If the camera configured successfully we do a capture
				cSession = session;
				session.setRepeatingRequest(request, cameraCaptureSessionCaptureCallback, null);
			} catch (CameraAccessException e) {
				Log.e(TAG, e.getLocalizedMessage());
				stopCamera();
			}
		}

		@Override
		public void onConfigureFailed(@NonNull CameraCaptureSession session) {
			Log.d(TAG, "Configuration failed");
			errorDialog();
		}

		@Override
		public void onActive(@NonNull CameraCaptureSession session) {
			Log.d(TAG, "Starting to process capture requests");
		}

	};

	// A callback object for tracking the progress of a CaptureRequest submitted to
	// the camera device.
	CameraCaptureSession.CaptureCallback cameraCaptureSessionCaptureCallback = new CameraCaptureSession.CaptureCallback() {

		@Override
		public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
			super.onCaptureCompleted(session, request, result);
		}

	};

	/**
	 * Class for updating the view on another thread while the main thread can do image processing
	 */
	public class processingWorker implements Runnable {
		private ImageReader imReader;

		public processingWorker(ImageReader imReader) {
			this.imReader = imReader;
		}

		public void run() {
			final Image image = imReader.acquireLatestImage();
			Log.d(TAG, "Picture size: " + image.getWidth() + "x" + image.getHeight());
			if (image != null) {
				// Create a mat out of the image
				bitmap = processFrame(image);

				activity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if(preview == null){
							Log.e(TAG, "preview is null");
						}
						if(bitmap == null){
							Log.e(TAG, "bitmap is null");
						}
						preview.setImageBitmap(bitmap);
					}
				});

				image.close();
			}
		}
	}


	// Constructor for the object, gets the camera ID for the backcam.
	public Capture(Activity activity) {
		// Store away the context
		this.activity = activity;

		// Get the preferences
		prefs = PreferenceManager.getDefaultSharedPreferences(activity);

		// Create a reader object for image processing
		reader = new Reader();

		// Initial bitmap
		bitmap = null;

		// Get the imageView to get a preview of the captures
		preview = activity.findViewById(R.id.imageView);

		// Get the cameramanager object, this handles all the cameras on the device
		cameraManager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);

		try {
			// Get list of all camera IDs
			backCamID = null;
			String[] strings = cameraManager.getCameraIdList();

			// Go through all the IDs and store away the ID of the back camera
			for (String id : strings) {
				CameraCharacteristics chars = cameraManager.getCameraCharacteristics(id);
				Log.d(TAG, String.valueOf(chars.get(CameraCharacteristics.SENSOR_ORIENTATION)));

				// Check if it is a camera facing back
				if (chars.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK) {
					Log.d(TAG, "Found back camera, ID: " + chars.toString());
					backCamID = id;
					break;
				}
			}

			// If we could not find the back camera
			if (backCamID == null) {
				Log.e(TAG, "Could not find back camera");
				errorDialog("No back camera found", "The app could not find a back camera on this device");
			}

			// Set the camera size and settings
			CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(backCamID);
			StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

			Size[] sizes = map.getOutputSizes(format);
			cSize = sizes[0];
		} catch (CameraAccessException e) {
			Log.e(TAG, "Camera access denied");
			errorDialog(activity.getResources().getString(R.string.camera_access_denied),
					activity.getResources().getString(R.string.camera_access_denied_desc));
		}
	}

	/**
	 * Take a picture with the back camera that has been found.
	 */
	public void startCamera() {
		try {
			// This operation is asynchronous and continues in the callback
			cameraManager.openCamera(backCamID, cameraDeviceStateCallback, null);
		} catch (CameraAccessException e) {
			Log.e(TAG, "Camera access denied");
			errorDialog(activity.getResources().getString(R.string.camera_access_denied),
					activity.getResources().getString(R.string.camera_access_denied_desc));
		} catch (SecurityException e) {
			Log.e(TAG, "Camera permission denied");
			// Dont do anything as this should not happen
		}
	}

	/**
	 * Stops the camera captures
	 */
	public void stopCamera() {
		Log.d(TAG, "closing camera");

		if (cam != null) {
			cam.close();
		}

		if (img != null) {
			img.close();
		}

		cam = null;
		img = null;
	}

	/**
	 * This function is called when an image is captured, transforms an image
	 * to a {@link Mat}
	 *
	 * @param reader - Imagereader object containing the result(s)
	 */
	public void onNewImageCapture(ImageReader reader) {
		//Run image processing thread if it is not busy.
		if (t1 == null || !t1.isAlive()) {
			t1 = new Thread(new processingWorker(reader));
			t1.start();
		}
	}

	/**
	 * Creates an error dialog with custom title and message. Closes
	 * the application on button press
	 *
	 * @param title   The title of the dialog
	 * @param message The message of the dialog
	 */
	private void errorDialog(String title, String message) {
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setTitle(title);
		builder.setMessage(message);
		builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface d, int i) {
				// When the user dismisses the dialog we close the application
				activity.finishAndRemoveTask();
			}
		});
		builder.create().show();
	}

	/**
	 * Creates an error dialog with generic "error has occured" message
	 */
	private void errorDialog() {
		errorDialog(activity.getString(R.string.error_title), activity.getString(R.string.error_message));
	}

	/**
	 * Takes an image object and converts it to a bitmap. Only takes the first plane
	 *
	 * @param image - The image object as input
	 * @return A bitmap object of the image
	 */
	private Bitmap processFrame(Image image) {
		//Get buffer of captured image
		Image.Plane[] planes = image.getPlanes();
		ByteBuffer buffer = planes[0].getBuffer();

		//Initialize image variables once with correct sizes.
		if (bitmap == null || procImage == null) {
			bitmap = Bitmap.createBitmap(image.getWidth(), image.getHeight(), Bitmap.Config.ARGB_8888);
			procImage = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC1);
			byteArray = new byte[buffer.remaining()];
		}
		buffer.get(byteArray);
		procImage.put(0, 0, byteArray);

		// This will process the image
		processedImage = reader.processFrame(procImage);
		//processedImage = rotateMat(processedImage);
		Log.d(TAG, String.valueOf(processedImage.cols()) + " " + String.valueOf(processedImage.rows()));


		//Resize if necessary(if processed frame is cropped)
		if (processedImage.width() != bitmap.getWidth() || processedImage.height() != bitmap.getHeight()) {
			bitmap.setWidth(processedImage.width());
			bitmap.setHeight(processedImage.height());
		}

		//Convert processed image to bitmap that can be shown on screen
		Utils.matToBitmap(processedImage, bitmap);

		return bitmap;
	}

	/**
	 * Converts an {@link Image} to a {@link Mat}, takes only the first plane of the image
	 *
	 * @param image The image to convert
	 * @return A {@link Mat} with the same size and data
	 */
	private Mat imageToMat(Image image) {
		Image.Plane[] planes = image.getPlanes();
		ByteBuffer buffer = planes[0].getBuffer();

		if (procImage == null) {
			Mat mat = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC1);
			byteArray = new byte[buffer.remaining()];
		}
		buffer.get(byteArray);
		procImage.put(0, 0, byteArray);

		return procImage;
	}

	private Mat rotateMat(Mat input) {
		Mat output = new Mat();
		Mat rotationMat = Imgproc.getRotationMatrix2D(new Point(input.width() / 2, input.height() / 2), -90, 1);
		Imgproc.warpAffine(input, output, rotationMat, input.size());
		return output;
	}

	/**
	 * Gets the size
	 *
	 * @return A {@link Size} object
	 */
	public Size getSize() {
		return cSize;
	}

}
