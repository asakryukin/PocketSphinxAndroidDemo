package edu.cmu.pocketsphinx.demo;


import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import android.graphics.Bitmap;

public class ChibiFaceRecognizer {

	private static String TAG = "ChibiFaceRecognizer";
	
	private CascadeClassifier faceCascade;
    private float mRelativeFaceSize = 0.2f;
    private int mAbsoluteFaceSize = 0;

	PersonRecognizer classifier;
	
	private String mPath;
	
	ChibiFaceRecognizer(String Path, PersonRecognizer mClassifier, CascadeClassifier mFaceCascade) {
		classifier = mClassifier;
		faceCascade = mFaceCascade;
		mPath = Path;
	}
	
	// input: RGB Bitmap image of the scene with person face
	// returns the Bitmap (RGB) image which will crop detected face only
	public Bitmap detectFace(Bitmap RGBBitmap) {
		Mat RGBImage = new Mat();
		Utils.bitmapToMat(RGBBitmap, RGBImage);
		MatOfRect faces = new MatOfRect();

		Mat mGray = new Mat();
		Imgproc.cvtColor(RGBImage, mGray, Imgproc.COLOR_RGB2GRAY);
		
		if (mAbsoluteFaceSize == 0) {
            int height = mGray.rows();
            if (Math.round(height * mRelativeFaceSize) > 0) {
                mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
            }
        }

        if (faceCascade != null)
            faceCascade.detectMultiScale(mGray, faces, 1.1, 2, 2, 
                    new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());

        Rect[] facesArray = faces.toArray();
       
        Mat m = new Mat();
        Rect r = facesArray[0];
       
        m=RGBImage.submat(r);
        Bitmap mBitmap = Bitmap.createBitmap(m.width(),m.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(m, mBitmap);
       return mBitmap;
	}
	
	
	// input: array of Bitmap images who already face detected
	//		  together with the array of String names for each image
	//		  which is needed to label each person
	public void trainClassifier(Bitmap [] RGBImages, String [] names) {
		for (int i = 0; i < RGBImages.length; i++) {
			Mat mat = new Mat();
			Utils.bitmapToMat(RGBImages[i], mat);
			Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGB2GRAY);
			Bitmap grayImage = Bitmap.createBitmap(mat.width(), mat.height(), Bitmap.Config.ARGB_8888);
			classifier.add(grayImage, names[i]);
		}
	}
	
	
	// returns the name (String) of person who is classified
	// input is RGB Bitmap image
	public String classify (Bitmap RGBImage) {
		Mat mat = new Mat();
		Utils.bitmapToMat(RGBImage, mat);
		Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGB2GRAY);
		Bitmap grayImage = Bitmap.createBitmap(mat.width(), mat.height(), Bitmap.Config.ARGB_8888);
		if (classifier.canPredict()) {
			return classifier.predict(grayImage) + " with probability of " + String.valueOf(classifier.getProb());
		}
		return "Cant predict, add more samples";
	}



	
}
