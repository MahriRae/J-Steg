package com.stegatective.abcd.j_steg;


import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.view.Window;
import android.view.WindowManager;

import java.io.File;
import java.util.jar.Manifest;



/*
* The MainActivity is the "Meat" of the app. It will control the encryption and decryption using a
* StegonagraphyManager *--TO BE ADDED IN A LATER VERSION--* and controls the opening and retrieving
* of an image from the gallery to be given to the different UIs of the application.
* */

public class MainActivity extends FragmentActivity {
    //Variable used to retrieve an image from the gallery
    private static int RESULT_LOAD_IMAGE = 1;
    private static int PERMISSION_REQUEST_READ_EXTERNAL_SERVICE = 1;
    private String imagePath;//The path of the selected image
    private Bundle bundle;//A bundle that will give the imagePath to the appropriate fragment
    private int interfaceControl = 0;//Will tell the main the fragment to give the image to
    private SteganographyManager stegoMan = new SteganographyManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);


        //Check if the activity is using a layout in the container
        if(findViewById(R.id.container) != null){
            //Determine if the app is being restored from a previous state
            if(savedInstanceState != null){
                //Do nothing so there is no overlap of fragments
                return;
            }
            //Set the container to have a MainMenuFragment on its create
            MainMenuFragment mainMenu = new MainMenuFragment();
            mainMenu.setArguments(getIntent().getExtras());
            getSupportFragmentManager().beginTransaction().add(R.id.container, mainMenu).commit();
        }
    }

    //Change the interfaceControl variable used to determine which UI to switch onActivityResult
    public void setInterfaceControl(int control){
        interfaceControl = control;
        return;
    }

    public void openGallery(){
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED){

            // Should we show and explanation
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)){

                //Show an explanation
            }else {
                //No explanation needed, we can request permission
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                       PERMISSION_REQUEST_READ_EXTERNAL_SERVICE);
            }
        }
        //Begin a new intent to open the gallery and allow for the selection of an image
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, RESULT_LOAD_IMAGE);
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        //If the request for intent is to load an image, has been successful and the intent exists
        //requestCode is used to identify the request, resultCode is returned by the child,
        //data is an intent which can return result data to the caller
        if(requestCode == RESULT_LOAD_IMAGE && resultCode == Activity.RESULT_OK && null != data) {
            Uri selectedImage = data.getData();//The URI of the content provider to query
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            //getContentResolver().query(URI at the content, Columns returned for each row,
            //                           Selection Criteria, Selection Criteria, Order to sort the rows);
            //This will query the given URI, and return a cursor over the result set
            Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            cursor.moveToFirst();//Move cursor to the first row

            //Returns the zero-based index for the given column name, or throws IllegalSArgumentException
            //If the column does not exist
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);

            //Set the imagePath to the string of the columnIndex from the cursor
            imagePath = cursor.getString(columnIndex);
            cursor.close();//Close the cursor, releasing all of its resources and making it completely invalid

            //If an image is successfully retrieved ie. imagePath != null, create  new bundle and
            //put imagePath into it with the key "imagePath".
            if(imagePath != null) {
                bundle = new Bundle();
                bundle.putString("imagePath", imagePath);
            }

            //Determine which UI to give the image to by the current setting of the interfaceControl
            switch(interfaceControl){
                //If interfaceControl is 1, give the resulting image to the EncryptionActivityFragment
                case 1:
                    EncryptionActivityFragment encryptionActivity = new EncryptionActivityFragment();
                    encryptionActivity.setArguments(bundle);
                    getSupportFragmentManager().beginTransaction().
                            replace(R.id.container, encryptionActivity).commit();
                    break;

                //If the interfaceControl is 2, give the resulting image to the DecryptionActivityFragment
                case 2:
                    DecryptionActivityFragment decryptionActivity = new DecryptionActivityFragment();
                    decryptionActivity.setArguments(bundle);
                    getSupportFragmentManager().beginTransaction().
                            replace(R.id.container, decryptionActivity).commit();
                    break;
            }
        }
    }

    public void encryptImage(File userMessage, String pinNumber){

        //Create a bitmap of the selected image and give the image to the stegoMan
        //The stegoMan will return the pathname of the encrypted image(A copy of the original)
        Bitmap selectedImage = BitmapFactory.decodeFile(imagePath).copy(Bitmap.Config.ARGB_8888, true);
        String encryptedImagePath = stegoMan.encryptIMG(this, selectedImage, userMessage, pinNumber);

        //Bundle the imagePath and returned encryptedImagePath into the bundle
        if(imagePath != null) {
            bundle = new Bundle();
            bundle.putString("imagePath", imagePath);
            bundle.putString("encryptedImagePath", encryptedImagePath);

        }

        //Give the bundle to the EncryptionCompletionFragment so it can be displayed to the user
        EncryptionCompletionFragment encryptionCompletion = new EncryptionCompletionFragment();
        encryptionCompletion.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().
                replace(R.id.container, encryptionCompletion).commit();
    }

    public void decryptImage(String pinNumber){

        Bitmap selectedImage = BitmapFactory.decodeFile(imagePath);
        String retrievedTextPath = stegoMan.decryptIMG(selectedImage,pinNumber);

        if(imagePath != null) {
            bundle = new Bundle();
            bundle.putString("imagePath", imagePath);
            bundle.putString("retrievedTextPath", retrievedTextPath);
        }

        DecryptionCompletionFragment decryptionCompletion = new DecryptionCompletionFragment();
        decryptionCompletion.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().
                replace(R.id.container, decryptionCompletion).commit();
    }
}//END OF CLASS

