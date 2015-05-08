package com.ems.rotate;

import java.io.File;
import java.io.IOException;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import com.ems.rotate.R;

public class MainActivity extends Activity {
	

	ImageView imageView;
    private static final int PICK_FROM_FILE = 1;
    int screenWidth, screenHeight ;
    String pathImage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        imageView = (ImageView) findViewById(R.id.imageView);
        
        DisplayMetrics displaymetrics;
        displaymetrics = new DisplayMetrics();
	    getWindowManager().getDefaultDisplay().getMetrics(displaymetrics); 
	    screenWidth = displaymetrics.widthPixels;
	    screenHeight = displaymetrics.heightPixels;
    }
    
    protected void onActivityResult (int requestCode,int resultCode,Intent data){
        if (resultCode != RESULT_OK) return;
        String path = "";
        Log.e("requestCode", String.valueOf(requestCode));
        if (requestCode == PICK_FROM_FILE) {
                Uri mImageCaptureUri = data.getData();
                path = getRealPathFromURI (mImageCaptureUri);
                if (path == null) {
                    path = mImageCaptureUri.getPath(); //from File Manager
                }
                Log.e("path", path);
                pathImage = path;
                imageView.setImageBitmap(getBitmap(path))  ;  
        }
    }
    
    @SuppressWarnings({ "deprecation" })
	public String getRealPathFromURI(Uri contentUri) {
        String [] proj 		= {MediaStore.Images.Media.DATA};
        Cursor cursor 		= managedQuery( contentUri, proj, null, null,null);        
        if (cursor == null) return null;        
        int column_index 	= cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);        
        cursor.moveToFirst();
        return cursor.getString(column_index);
	}
    
    @SuppressLint("NewApi") private Bitmap getBitmap(String path) {
		Log.e("inside of", "getBitmap = "+path);
		try {
		    Bitmap b = null;
		    BitmapFactory.Options o = new BitmapFactory.Options();
		    o.inJustDecodeBounds = true;		   
		    Matrix matrix = new Matrix();
	    	ExifInterface exifReader = new ExifInterface(path);
	    	int orientation = exifReader.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1);
	    	int rotate = 0;
	    	if (orientation ==ExifInterface.ORIENTATION_NORMAL) {
	    	// Do nothing. The original image is fine.
	    	} else if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
	    			rotate = 90;
	    	} else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
	    			rotate = 180;	
	    	} else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
	    			rotate = 270;
	    	}
	    	matrix.postRotate(rotate);	
	    	Button btn_RotateImg = (Button) findViewById(R.id.btn_RotateImg);
		    try {
		    	b = loadBitmap(path, rotate, screenWidth, screenHeight);
		    	btn_RotateImg.setEnabled(true);
		    } catch (OutOfMemoryError e) {
		    	btn_RotateImg.setEnabled(false);
	    	}
		    	System.gc();
		    	return b;  
		} catch (Exception e) {
		    Log.e("my tag", e.getMessage(), e);
		    return null;
		}
	}
          
    public static Bitmap loadBitmap(String path, int orientation, final int targetWidth, final int targetHeight) {
	    Bitmap bitmap = null;
	    try {
	        final BitmapFactory.Options options = new BitmapFactory.Options();
	        options.inJustDecodeBounds = true;
	        BitmapFactory.decodeFile(path, options);
	        int sourceWidth, sourceHeight;
	        if (orientation == 90 || orientation == 270) {
	            sourceWidth = options.outHeight;
	            sourceHeight = options.outWidth;
	        } else {
	            sourceWidth = options.outWidth;
	            sourceHeight = options.outHeight;
	        }
	        if (sourceWidth > targetWidth || sourceHeight > targetHeight) {
	            float widthRatio = (float)sourceWidth / (float)targetWidth;
	            float heightRatio = (float)sourceHeight / (float)targetHeight;
	            float maxRatio = Math.max(widthRatio, heightRatio);
	            options.inJustDecodeBounds = false;
	            options.inSampleSize = (int)maxRatio;
	            bitmap = BitmapFactory.decodeFile(path, options);
	        } else {
	            bitmap = BitmapFactory.decodeFile(path);
	        }
	        if (orientation > 0) {
	            Matrix matrix = new Matrix();
	            matrix.postRotate(orientation);
	            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
	        }
	        sourceWidth = bitmap.getWidth();
	        sourceHeight = bitmap.getHeight();
	        if (sourceWidth != targetWidth || sourceHeight != targetHeight) {
	            float widthRatio = (float)sourceWidth / (float)targetWidth;
	            float heightRatio = (float)sourceHeight / (float)targetHeight;
	            float maxRatio = Math.max(widthRatio, heightRatio);
	            sourceWidth = (int)((float)sourceWidth / maxRatio);
	            sourceHeight = (int)((float)sourceHeight / maxRatio);
	            bitmap = Bitmap.createScaledBitmap(bitmap, sourceWidth, sourceHeight, true);
	        }
	    } catch (Exception e) {
	    }
	    return bitmap;
	}
           
    public void rotateImage(String path){
		File file = new File(path);
		ExifInterface exifInterface = null;
		try {
			exifInterface = new ExifInterface(file.getPath());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
		if ( (orientation == ExifInterface.ORIENTATION_NORMAL) | (orientation == 0) ) {
			exifInterface.setAttribute(ExifInterface.TAG_ORIENTATION, ""+ExifInterface.ORIENTATION_ROTATE_90);
		} else if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
			exifInterface.setAttribute(ExifInterface.TAG_ORIENTATION, ""+ExifInterface.ORIENTATION_ROTATE_180);
		} else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
			exifInterface.setAttribute(ExifInterface.TAG_ORIENTATION, ""+ExifInterface.ORIENTATION_ROTATE_270);
		} else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
			exifInterface.setAttribute(ExifInterface.TAG_ORIENTATION, ""+ExifInterface.ORIENTATION_NORMAL);
		} 
		try {
			exifInterface.saveAttributes();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		imageView.setImageBitmap(getBitmap(path))  ;
	}
        
    public void loadBtnClick(View viev){
       Intent intent = new Intent();
       intent.setType("image/*");
       intent.setAction(Intent.ACTION_GET_CONTENT);
       startActivityForResult(Intent.createChooser(intent, "Complete action using"), PICK_FROM_FILE);
    }
        
    public void rotateBtnclick(View viev){
    	rotateImage (pathImage);     	
    }
    
}
