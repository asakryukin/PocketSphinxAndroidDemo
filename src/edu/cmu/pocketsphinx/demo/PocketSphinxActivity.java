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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.objdetect.CascadeClassifier;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;

public class PocketSphinxActivity extends Activity implements
        RecognitionListener, CvCameraViewListener2, OnClickListener, OnInitListener {

    private static final String KWS_SEARCH = "wakeup";
    private static final String FORECAST_SEARCH = "forecast";
    private static final String DIGITS_SEARCH = "digits";
    private static final String COMMAND_SEARCH = "menu";
    private static final String KEYPHRASE = "hi chibi";
   private CascadeClassifier faceCascade;
	private static String mPath;
	private String TAG="mylog";
    private SpeechRecognizer recognizer;
    private HashMap<String, Integer> captions;
    private final static String DEBUG_TAG = "MakePhotoActivity";
    private Camera camera;
    private int cameraId = 0;
   
   private static final Scalar    FACE_RECT_COLOR     = new Scalar(0, 255, 0, 255);
    public static final int        JAVA_DETECTOR       = 0;
    public static final int        NATIVE_DETECTOR     = 1;
    
    public static final int TRAINING= 0;
    public static final int SEARCHING= 1;
    public static final int IDLE= 2;
    
    private static final int frontCam =1;
    private static final int backCam =2;
    	    		
    
    private int faceState=IDLE;
//    private int countTrain=0;
    
//    private MenuItem               mItemFace50;
//    private MenuItem               mItemFace40;
//    private MenuItem               mItemFace30;
//    private MenuItem               mItemFace20;
//    private MenuItem               mItemType;
//    
    private MenuItem               nBackCam;
    private MenuItem               mFrontCam;
    private MenuItem               mEigen;
    

    private Mat                    mRgba;
    private Mat                    mGray;
    private File                   mCascadeFile;
    private CascadeClassifier      mJavaDetector;
 //   private DetectionBasedTracker  mNativeDetector;

    private int                    mDetectorType       = JAVA_DETECTOR;
    private String[]               mDetectorName;

    private float                  mRelativeFaceSize   = 0.2f;
    private int                    mAbsoluteFaceSize   = 0;
    private int mLikely=999;
    
    String mPathFace="";

    private Tutorial3View   mOpenCvCameraView;
    private int mChooseCamera = backCam;
    
    EditText text;
    TextView textresult,status, rect_size_txt;
    private  ImageView Iv;
    Bitmap mBitmap;
    Handler mHandler;
  
    private TextToSpeech mTts;
    private Access_Manager access;
    
    PersonRecognizer fr;
    ToggleButton toggleButtonGrabar,toggleButtonTrain,buttonSearch,buttonChange;
    Button buttonCatalog,train,search;
    ImageView ivGreen,ivYellow,ivRed; 
    ImageButton imCamera;
    
    TextView textState;
    com.googlecode.javacv.cpp.opencv_contrib.FaceRecognizer faceRecognizer;
    static final long MAXIMG = 10;
    
    ArrayList<Mat> alimgs = new ArrayList<Mat>();

    int[] labels = new int[(int)MAXIMG];
    int countImages=0;
    
    labels labelsFile;
    
    private int[] pred_results=new int[10];
    private int pred_ind=0;
    private String active_person="";
    private int not_defined_number=0;
    private String speech_result="";
    private Socket socket;
    
    private BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");

                    // Load native library after(!) OpenCV initialization
                 //   System.loadLibrary("detection_based_tracker");
            
                    
 
                    fr=new PersonRecognizer(mPathFace);
                    String s = getResources().getString(R.string.Straininig);
                    Toast.makeText(getApplicationContext(),s, Toast.LENGTH_LONG).show();
                    fr.load();
                    access=new Access_Manager(getApplicationContext(), mPathFace+"faces.txt");
                    try {
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

                        mJavaDetector = new CascadeClassifier(mCascadeFile.getAbsolutePath());
                        if (mJavaDetector.empty()) {
                            Log.e(TAG, "Failed to load cascade classifier");
                            mJavaDetector = null;
                        } else
                            Log.i(TAG, "Loaded cascade classifier from " + mCascadeFile.getAbsolutePath());

       //                 mNativeDetector = new DetectionBasedTracker(mCascadeFile.getAbsolutePath(), 0);

                        cascadeDir.delete();

                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
                    }

                    mOpenCvCameraView.enableView();
                    
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
                
                
            }
        }
    };
    public PocketSphinxActivity()  {
        mDetectorName = new String[2];
        mDetectorName[JAVA_DETECTOR] = "Java";
        mDetectorName[NATIVE_DETECTOR] = "Native (tracking)";

        Log.i(TAG, "Instantiated new " + this.getClass());
    }
    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
       /* mDetectorName = new String[2];
        mDetectorName[JAVA_DETECTOR] = "Java";
        mDetectorName[NATIVE_DETECTOR] = "Native (tracking)";*/
        // Prepare the data for UI
        captions = new HashMap<String, Integer>();
        captions.put(KWS_SEARCH, R.string.kws_caption);
        captions.put(COMMAND_SEARCH, R.string.menu_caption);
        captions.put(DIGITS_SEARCH, R.string.digits_caption);
        captions.put(FORECAST_SEARCH, R.string.forecast_caption);
        
        mTts = new TextToSpeech(this,this);
		mTts.setLanguage(Locale.US);
		mTts.setSpeechRate((float)0.9);
        
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        setContentView(R.layout.main);
        mPathFace = Environment.getExternalStorageDirectory() + "/CHIBI/";
        labelsFile= new labels(mPathFace);
        text=(EditText) findViewById(R.id.main_name);
        toggleButtonTrain=(ToggleButton)findViewById(R.id.main_train);
        toggleButtonGrabar=(ToggleButton)findViewById(R.id.main_grab);
        Iv=(ImageView) findViewById(R.id.imageView_IV);
        buttonSearch=(ToggleButton) findViewById(R.id.main_search);
        status=(TextView) findViewById(R.id.main_status);
        rect_size_txt=(TextView) findViewById(R.id.main_rect_size);
        buttonChange=(ToggleButton) findViewById(R.id.main_change);
        
        ((TextView) findViewById(R.id.caption_text))
                .setText("Preparing the recognizer");
        mOpenCvCameraView = (Tutorial3View) findViewById(R.id.tutorial3_activity_java_surface_view);
        //mOpenCvCameraView.setCamFront();
        mOpenCvCameraView.setCvCameraViewListener(this);
    
      // constructor which is already the same
	
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
        
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
            	if (msg.obj=="IMG")
            	{
            	 Canvas canvas = new Canvas();
                 canvas.setBitmap(mBitmap);
                 rect_size_txt.setText("Width:"+mBitmap.getWidth()+"  Height:"+mBitmap.getHeight()+"  Area:"+(mBitmap.getWidth()*mBitmap.getHeight()));
                 
                 //if (mBitmap.getWidth()>270)
                //	 mTts.speak("You are too close, go away", TextToSpeech.QUEUE_FLUSH, null);
                 
                 Iv.setImageBitmap(mBitmap);
                 if (countImages>=MAXIMG-1)
                 {
                	 toggleButtonGrabar.setChecked(false);
                 	 grabarOnclick();
                 }
            	}
            	else
            	{
            		//if (mLikely<40)
            		//Toast.makeText(getApplicationContext(), msg.obj.toString()+"   "+mLikely, Toast.LENGTH_SHORT).show();
            		status.setText(msg.obj.toString()+"   "+mLikely);
            		
            		if (pred_ind<10){
            			pred_results[pred_ind]=access.getId(msg.obj.toString());
            			pred_ind++;
            		}else{
            			int r=0;
            			for(int i=0;i<10;i++){
            				r=r+pred_results[i];
            			}
            			r=(int) Math.floor((double) r/10); 
            			if (r>0){
            			active_person=access.getName(r);
            			}
            			else {
            				active_person="";
            			}
            			Toast.makeText(getApplicationContext(), active_person, Toast.LENGTH_SHORT).show();
            			Log.d("mylog", "PER:"+active_person+"R:"+r);
            			pred_ind=0;
            		}
            		
            		/*textresult.setText(msg.obj.toString());
            		 ivGreen.setVisibility(View.INVISIBLE);
            	     ivYellow.setVisibility(View.INVISIBLE);
            	     ivRed.setVisibility(View.INVISIBLE);
            	     
            	     if (mLikely<0);
            	     else if (mLikely<50)
            			ivGreen.setVisibility(View.VISIBLE);
            		else if (mLikely<80)
            			ivYellow.setVisibility(View.VISIBLE);            			
            		else 
            			ivRed.setVisibility(View.VISIBLE);*/
            	}
            }
        };
        
        toggleButtonGrabar.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				grabarOnclick();
			}
		});
        
        buttonChange.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (mChooseCamera==frontCam)
				{
					mChooseCamera=backCam;
					mOpenCvCameraView.setCamBack();
				}
				else
				{
					mChooseCamera=frontCam;
					mOpenCvCameraView.setCamFront();
				}
			}
		});
        
        toggleButtonTrain.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (toggleButtonTrain.isChecked()) {
					/*
					textState.setText(getResources().getString(R.string.SEnter));
					buttonSearch.setVisibility(View.INVISIBLE);
					textresult.setVisibility(View.VISIBLE);
					text.setVisibility(View.VISIBLE);
					textresult.setText(getResources().getString(R.string.SFaceName));
					if (text.getText().toString().length() > 0)
						toggleButtonGrabar.setVisibility(View.VISIBLE);
					ivGreen.setVisibility(View.INVISIBLE);
					ivYellow.setVisibility(View.INVISIBLE);
					ivRed.setVisibility(View.INVISIBLE);
					*/

				} else {
					/*
					textState.setText(R.string.Straininig); 
					textresult.setText("");
					text.setVisibility(View.INVISIBLE);
					
					buttonSearch.setVisibility(View.VISIBLE);
					;
					textresult.setText("");
					{
						toggleButtonGrabar.setVisibility(View.INVISIBLE);
						text.setVisibility(View.INVISIBLE);
					}
			        Toast.makeText(getApplicationContext(),getResources().getString(R.string.Straininig), Toast.LENGTH_LONG).show();
					*/
					fr.train();
					access=new Access_Manager(getApplicationContext(), mPathFace+"faces.txt");
					//textState.setText(getResources().getString(R.string.SIdle));

				}
			}

		});
        
        toggleButtonGrabar.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				grabarOnclick();
			}
		});
        
        buttonSearch.setOnClickListener(new View.OnClickListener() {

 			public void onClick(View v) {
 				if (buttonSearch.isChecked())
 				{
 					if (!fr.canPredict())
 						{
 						buttonSearch.setChecked(false);
 			            Toast.makeText(getApplicationContext(), getResources().getString(R.string.SCanntoPredic), Toast.LENGTH_LONG).show();
 			            return;
 						}
 					/*textState.setText(getResources().getString(R.string.SSearching));
 					toggleButtonGrabar.setVisibility(View.INVISIBLE);
 					toggleButtonTrain.setVisibility(View.INVISIBLE);
 					text.setVisibility(View.INVISIBLE);*/
 					faceState=SEARCHING;
 					//textresult.setVisibility(View.VISIBLE);
 				}
 				else
 				{
 					faceState=IDLE;
 					/*textState.setText(getResources().getString(R.string.SIdle));
 					toggleButtonGrabar.setVisibility(View.INVISIBLE);
 					toggleButtonTrain.setVisibility(View.VISIBLE);
 					text.setVisibility(View.INVISIBLE);
 					textresult.setVisibility(View.INVISIBLE);*/
 					
 				}
 				
 			}
 		});
        
        boolean success=(new File(mPathFace)).mkdirs();
        if (!success)
        {
        	Log.e("Error","Error creating directory");
        	Toast.makeText(this, "ERROR CREATING", Toast.LENGTH_SHORT).show();
        }
        
        //mOpenCvCameraView.setCamFront();
    	//mChooseCamera=frontCam;
        
        
        
    }
    
    void grabarOnclick()
    {
    	if (toggleButtonGrabar.isChecked())
			faceState=TRAINING;
			else
			{ if (faceState==TRAINING)	;				
			 // train();
			  //fr.train();
			  countImages=0;
			  faceState=IDLE;
			}
		

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
            ((TextView) findViewById(R.id.main_status)).setText(text);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_9, this, mLoaderCallback);
       
      	
    }
    
    @Override
    public void onResult(Hypothesis hypothesis) {
        ((TextView) findViewById(R.id.result_text)).setText("");
        if (hypothesis != null) {
            String text = hypothesis.getHypstr();
            speech_result=text;
            makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
            if (speech_result.length()>10){
            if ((!active_person.equals(""))&&(speech_result.indexOf("task")>0))
        	{
        		if (speech_result.indexOf("one")>-1){
        			if (access.isAllowed(active_person, 1)){
        				mTts.speak("Executing task one", TextToSpeech.QUEUE_FLUSH, null);
        				/*try {
							PrintWriter out= new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())),true);
							out.println("Start");
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}*/
        				Thread thread=new Thread(new Runnable() {
        					
        					@Override
        					public void run() {
        						// TODO Auto-generated method stub
        						Log.d("mySocket", "Before");
        				        try {
        				        	InetAddress servadr=InetAddress.getByName("192.168.137.1");
        							Socket socket = new Socket(servadr, 8888);
        							Log.d("mySocket", "After in try");
        							try {
        								PrintWriter out= new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())),true);
        								out.println("task 1");
        							} catch (IOException e) {
        								// TODO Auto-generated catch block
        							}
        							
        						} catch (UnknownHostException e) {
        							// TODO Auto-generated catch block
        							e.printStackTrace();
        						} catch (IOException e) {
        							// TODO Auto-generated catch block
        							e.printStackTrace();
        						}
        					}
        				});
        		        
        		        thread.start();
        			}else {
        				mTts.speak("You have no acces for the task,"+active_person, TextToSpeech.QUEUE_FLUSH, null);
                        
        			}
        		} else 
        			if (speech_result.indexOf("two")>-1){
        				if (access.isAllowed(active_person, 2)){
            				mTts.speak("Executing task two", TextToSpeech.QUEUE_FLUSH, null);
            				/*try {
    							PrintWriter out= new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())),true);
    							out.println("Start");
    						} catch (IOException e) {
    							// TODO Auto-generated catch block
    							e.printStackTrace();
    						}*/
            				Thread thread=new Thread(new Runnable() {
            					
            					@Override
            					public void run() {
            						// TODO Auto-generated method stub
            						Log.d("mySocket", "Before");
            				        try {
            				        	InetAddress servadr=InetAddress.getByName("192.168.137.1");
            							Socket socket = new Socket(servadr, 8888);
            							
            							Log.d("mySocket", "After in try");
            							try {
            								PrintWriter out= new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())),true);
            								out.println("task 2");
            							} catch (IOException e) {
            								// TODO Auto-generated catch block
            							}
            							
            						} catch (UnknownHostException e) {
            							// TODO Auto-generated catch block
            							e.printStackTrace();
            						} catch (IOException e) {
            							// TODO Auto-generated catch block
            							e.printStackTrace();
            						}
            					}
            				});
            		        
            		        thread.start();
            			}else {
            				mTts.speak("You have no acces for the task,"+active_person, TextToSpeech.QUEUE_FLUSH, null);
                            
            			}
        			} else
        				if (speech_result.indexOf("three")>-1){
        					if (access.isAllowed(active_person, 3)){
                				mTts.speak("Executing task three", TextToSpeech.QUEUE_FLUSH, null);
                                
                			}else {
                				mTts.speak("You have no acces for the task,"+active_person, TextToSpeech.QUEUE_FLUSH, null);
                                
                			}
        				} else {
        					mTts.speak("I don't know such a task", TextToSpeech.QUEUE_FLUSH, null);
                            
        				}
        	}
        	else {
        		mTts.speak("You are not registered", TextToSpeech.QUEUE_FLUSH, null);
                
        	}
            }
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
        if (COMMAND_SEARCH.equals(recognizer.getSearchName())){
        	// Executing task after hearing a command
        	makeText(getApplicationContext(), "DOING!", Toast.LENGTH_SHORT).show();
        	
        	switchSearch(KWS_SEARCH);
        }
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
    
    public void onCameraViewStarted(int width, int height) {
        mGray = new Mat();
        mRgba = new Mat();
    }

    public void onCameraViewStopped() {
        mGray.release();
        mRgba.release();
    }

	@Override
	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
		// TODO Auto-generated method stub
		
		mRgba = inputFrame.rgba();
        mGray = inputFrame.gray();
        
        
        
        //faceState=SEARCHING;
        
        if (mAbsoluteFaceSize == 0) {
            int height = mGray.rows();
            if (Math.round(height * mRelativeFaceSize) > 0) {
                mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
            }
          //  mNativeDetector.setMinFaceSize(mAbsoluteFaceSize);
        }

        MatOfRect faces = new MatOfRect();
        if (mDetectorType == JAVA_DETECTOR) {
            if (mJavaDetector != null)
                mJavaDetector.detectMultiScale(mGray, faces, 1.1, 2, 2, // TODO: objdetect.CV_HAAR_SCALE_IMAGE
                        new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());
        }
        else if (mDetectorType == NATIVE_DETECTOR) {
//            if (mNativeDetector != null)
//                mNativeDetector.detect(mGray, faces);
        }
        else {
            Log.e(TAG, "Detection method is not selected!");
        }
        
        Rect[] facesArray = faces.toArray();
        
        if ((facesArray.length==1)&&(faceState==TRAINING)&&(countImages<MAXIMG)&&(!text.getText().toString().isEmpty()))
        {
        
      Log.d("mylog", "!!!!!!INSIDE!!!!");
        Mat m=new Mat();
        Rect r=facesArray[0];
       
        
        m=mRgba.submat(r);
        mBitmap = Bitmap.createBitmap(m.width(),m.height(), Bitmap.Config.ARGB_8888);
        
        
        Utils.matToBitmap(m, mBitmap);
       // SaveBmp(mBitmap,"/sdcard/db/I("+countTrain+")"+countImages+".jpg");
        
        Message msg = new Message();
        String textTochange = "IMG";
        msg.obj = textTochange;
        mHandler.sendMessage(msg);
        if (countImages<MAXIMG)
        {
        	fr.add(m, text.getText().toString());
        	countImages++;
        }
        }
        else
        	 if ((facesArray.length>0)&& (faceState==SEARCHING))
          {
        		 
        		 Log.d("mylog", "!!!!SEARCHING!!!!");
        	  Mat m=new Mat();
        	  m=mGray.submat(facesArray[0]);
        	  mBitmap = Bitmap.createBitmap(m.width(),m.height(), Bitmap.Config.ARGB_8888);
        
             
              Utils.matToBitmap(m, mBitmap);
              Message msg = new Message();
              String textTochange = "IMG";
              msg.obj = textTochange;
              mHandler.sendMessage(msg);
        	  
              textTochange=fr.predict(m);
              mLikely=fr.getProb();
        	  msg = new Message();
        	  msg.obj = textTochange;
        	  
        	  mHandler.sendMessage(msg);
        	  
          }
        	 else 
        		 if ((facesArray.length==0)&& (faceState==SEARCHING)) {
        			 if(not_defined_number<10){
        				 not_defined_number++;
        			 }else {
        				 not_defined_number=0;
        				 pred_ind=0;
        				 active_person="";
        			 }
        		 }
        for (int i = 0; i < facesArray.length; i++)
            Core.rectangle(mRgba, facesArray[i].tl(), facesArray[i].br(), FACE_RECT_COLOR, 3);
        
        
        
		return mRgba;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()){
		case R.id.main_search:
			
				faceState=SEARCHING;
			
			break;
		}
	}
	@Override
	public void onInit(int status) {
		// TODO Auto-generated method stub
		
	}
	@Override
    public void onDestroy() {
        // Don't forget to shutdown tts!
        if (mTts != null) {
        	mTts.stop();
        	mTts.shutdown();
        }
        try {
			socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        super.onDestroy();
    }
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		menu.add("Access Settings");
		return super.onCreateOptionsMenu(menu);
		
	}
	
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		// TODO Auto-generated method stub
		
		if (item.getTitle().toString().equals("Access Settings")==true){
			Intent intent=new Intent(getApplicationContext(), Access_Settings.class);
			startActivity(intent);
		}
		
		return super.onMenuItemSelected(featureId, item);
	}
	
}
