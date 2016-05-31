package com.stegatective.abcd.j_steg;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

/**
 * Created by Mariah on 4/21/2016.
 * The EncryptionCompletionFragment defines the layout of the UI displayed upon
 * the completion of a successful encryption activity.
 */
public class EncryptionCompletionFragment extends Fragment {

    public EncryptionCompletionFragment(){
        //-----Do Nothing-----
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){

        //Get the path of the originalImage and the encryptedImage from the MainActivities bundle
        String originalImagePath = this.getArguments().getString("imagePath");
        String encryptedImagePath = this.getArguments().getString("encryptedImagePath");

        //Inflate the fragment into the container of the MainMenu
        View view = inflater.inflate(R.layout.fragment_encryption_completion, container, false);

        //Set the imageViews to display the original and the encrypted images
        ImageView originalImage = (ImageView)view.findViewById(R.id.originalImageView);
        ImageView encryptedImage = (ImageView)view.findViewById(R.id.encrtyptedImageView);
        originalImage.setImageBitmap(BitmapFactory.decodeFile(originalImagePath));
        encryptedImage.setImageBitmap(BitmapFactory.decodeFile(encryptedImagePath));

        //Create a button listener for the done button, when pressed show main menu
        Button encryptDone = (Button)view.findViewById(R.id.eDoneBTN);
        encryptDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainMenuFragment mainMenu = new MainMenuFragment();
        //Inflate the EncryptionCompletionFragment xml to the container
                getFragmentManager().beginTransaction().
                        replace(R.id.container, mainMenu).commit();
            }
        });

        return view;
    }
}
