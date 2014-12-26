/* ====================================================================
 * Copyright (c) 2014 Alpha Cephei Inc.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY ALPHA CEPHEI INC. ``AS IS'' AND
 * ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL CARNEGIE MELLON UNIVERSITY
 * NOR ITS EMPLOYEES BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * ====================================================================
 */

package edu.cmu.pocketsphinx.demo;

import static android.widget.Toast.makeText;
import static edu.cmu.pocketsphinx.SpeechRecognizerSetup.defaultSetup;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.objdetect.CascadeClassifier;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PictureCallback;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;

public class PocketSphinxActivity extends Activity implements
        RecognitionListener {

    private static final String KWS_SEARCH = "wakeup";
    private static final String FORECAST_SEARCH = "forecast";
    private static final String DIGITS_SEARCH = "digits";
    private static final String COMMAND_SEARCH = "menu";
    private static final String KEYPHRASE = "hi chibi";
    private File mCascadeFile;
	private CascadeClassifier faceCascade;
	private PersonRecognizer classifier;
	private static String mPath;
	private String TAG="mylog";
    private SpeechRecognizer recognizer;
    private HashMap<String, Integer> captions;
    private ChibiFaceRecognizer mFaceRecognizer;
    private final static String DEBUG_TAG = "MakePhotoActivity";
    private Camera camera;
    private int cameraId = 0;
    private ImageView  front;
    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);

        // Prepare the data for UI
        captions = new HashMap<String, Integer>();
        captions.put(KWS_SEARCH, R.string.kws_caption);
        captions.put(COMMAND_SEARCH, R.string.menu_caption);
        captions.put(DIGITS_SEARCH, R.string.digits_caption);
        captions.put(FORECAST_SEARCH, R.string.forecast_caption);
        setContentView(R.layout.main);
        ((TextView) findViewById(R.id.caption_text))
                .setText("Preparing the recognizer");
        
        front=(ImageView) findViewById(R.id.Frontal_Camera);
        
        //Get Camera 
        if (!getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
              Toast.makeText(this, "No camera on this device", Toast.LENGTH_LONG)
                  .show();
            } else {
              cameraId = findFrontFacingCamera();
              if (cameraId < 0) {
                Toast.makeText(this, "No front facing camera found.",
                    Toast.LENGTH_LONG).show();
              } else {
                camera = Camera.open(cameraId);
              }
            }
        PictureCallback pC=new PictureCallback() {
			
			@Override
			public void onPictureTaken(byte[] data, Camera camera) {
				// TODO Auto-generated method stub
				Bitmap bmp=new BitmapFactory().decodeByteArray(data, 0, data.length);
				front.setImageBitmap(bmp);
			}
		};
		camera.takePicture(null, null, pC);
        // Recognizer initialization is a time-consuming and it involves IO,
        // so we execute it in async task
        mPath = Environment.getExternalStorageDirectory().toString() + "/chibifacerecognitionfiles/";
		
		// constructor which is already the same
		mFaceRecognizer = new ChibiFaceRecognizer(mPath, classifier, faceCascade);
		
		new AsyncTask<Void, Void, Exception>() {
            @Override
            protected Exception doInBackground(Void... params) {
                try {
                    Assets assets = new Assets(PocketSphinxActivity.this);
                    File assetDir = assets.syncAssets();
                    setupRecognizer(assetDir);
                } catch (IOException e) {
                    return e;
                }
                return null;
            }

            @Override
            protected void onPostExecute(Exception result) {
                if (result != null) {
                    ((TextView) findViewById(R.id.caption_text))
                            .setText("Failed to init recognizer " + result);
                } else {
                    switchSearch(KWS_SEARCH);
                }
            }
        }.execute();
    }

    @Override
    public void onPartialResult(Hypothesis hypothesis) {
        String text = hypothesis.getHypstr();
        if (text.equals(KEYPHRASE))
            switchSearch(COMMAND_SEARCH);
        else if (text.equals(DIGITS_SEARCH))
            switchSearch(DIGITS_SEARCH);
        else if (text.equals(FORECAST_SEARCH))
            switchSearch(FORECAST_SEARCH);
        else
            ((TextView) findViewById(R.id.result_text)).setText(text);
    }

    @Override
    public void onResult(Hypothesis hypothesis) {
        ((TextView) findViewById(R.id.result_text)).setText("");
        if (hypothesis != null) {
            String text = hypothesis.getHypstr();
            makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBeginningOfSpeech() {
    }

    @Override
    public void onEndOfSpeech() {
        if (DIGITS_SEARCH.equals(recognizer.getSearchName())
                || FORECAST_SEARCH.equals(recognizer.getSearchName()))
            switchSearch(KWS_SEARCH);
    }

    private void switchSearch(String searchName) {
        recognizer.stop();
        recognizer.startListening(searchName);
        String caption = getResources().getString(captions.get(searchName));
        ((TextView) findViewById(R.id.caption_text)).setText(caption);
    }

    private void setupRecognizer(File assetsDir) {
        File modelsDir = new File(assetsDir, "models");
        recognizer = defaultSetup()
                .setAcousticModel(new File(modelsDir, "hmm/en-us-semi"))
                .setDictionary(new File(modelsDir, "dict/cmu07a.dic"))
                .setRawLogDir(assetsDir).setKeywordThreshold(1e-20f)
                .getRecognizer();
        recognizer.addListener(this);

        // Create keyword-activation search.
        recognizer.addKeyphraseSearch(KWS_SEARCH, KEYPHRASE);
        // Create grammar-based searches.
        File menuGrammar = new File(modelsDir, "grammar/menu.gram");
        recognizer.addGrammarSearch(COMMAND_SEARCH, menuGrammar);
        File digitsGrammar = new File(modelsDir, "grammar/digits.gram");
        recognizer.addGrammarSearch(DIGITS_SEARCH, digitsGrammar);
        // Create language model search.
        File languageModel = new File(modelsDir, "lm/weather.dmp");
        recognizer.addNgramSearch(FORECAST_SEARCH, languageModel);
    }
    private int findFrontFacingCamera() {
        int cameraId = -1;
        // Search for the front facing camera
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
          CameraInfo info = new CameraInfo();
          Camera.getCameraInfo(i, info);
          if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
            Log.d(DEBUG_TAG, "Camera found");
            cameraId = i;
            break;
          }
        }
        return cameraId;
      }
 // must be included (just copy-paste)
 	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
 	    @Override
 	    public void onManagerConnected(int status) {
 	        switch (status) {
 	            case LoaderCallbackInterface.SUCCESS:
 	            {
                     try {
                     	classifier = new PersonRecognizer(mPath); 
                     	// load cascade file from application resources
                         InputStream is = getResources().openRawResource(R.raw.lbpcascade_frontalface);
                         File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                         mCascadeFile = new File(cascadeDir, "lbpcascade.xml");
                         FileOutputStream os = new FileOutputStream(mCascadeFile);

                         byte[] buffer = new byte[4096];
                         int bytesRead;
                         while ((bytesRead = is.read(buffer)) != -1) {
                             os.write(buffer, 0, bytesRead);
                         }
                         is.close();
                         os.close();
                         faceCascade = new CascadeClassifier(mCascadeFile.getAbsolutePath());
                         if (faceCascade.empty()) {
                             Log.e(TAG, "Failed to load cascade classifier");
                             faceCascade = null;
                         } else
                             Log.i(TAG, "Loaded cascade classifier from " + mCascadeFile.getAbsolutePath());
                         cascadeDir.delete();
                     } catch (IOException e) {
                         e.printStackTrace();
                         Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
                     }
 	                Log.i(TAG, "OpenCV loaded successfully");
 	                Log.d("mylog", "OPENCV LOADED SUCCESSFULLY");
 	            } break;
 	            default:
 	            {
 	                super.onManagerConnected(status);
 	            } break;
 	        }
 	    }
 	};
 	
@Override
	
	public void onResume(){
		super.onResume();
		
		// must be included in onResume
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_9, this, mLoaderCallback);
	}

	
	
}
