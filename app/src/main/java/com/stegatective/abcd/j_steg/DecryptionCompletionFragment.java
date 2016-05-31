package com.stegatective.abcd.j_steg;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

/**
 * Created by Mariah on 4/21/2016.
 * The DecryptionCompletionFragment defines the layout of the UI displayed
 * upon the completion of a successful decryption activity
 */
public class DecryptionCompletionFragment extends Fragment {

    private int decryptCompleteInterfaceControl = 4;

    public DecryptionCompletionFragment() {
        //-----Do Nothing-----
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Get the image path from the MainMenu
        String selectedImagePath = this.getArguments().getString("imagePath");
        String retrievedTextPath = this.getArguments().getString("retrievedTextPath");

        //Inflate the DecryptionCompletionFragment xml to the container
        View view = inflater.inflate(R.layout.fragment_decryption_completion, container, false);

        //Put the selected image into the image view
        ImageView selectedImage = (ImageView)view.findViewById(R.id.decryptedImage);
        selectedImage.setImageBitmap(BitmapFactory.decodeFile(selectedImagePath));

        //Get the text from the file and put them into the textview
        TextView message = (TextView)view.findViewById(R.id.decryptedMessage);
        File file = new File(retrievedTextPath,"temp.txt");
        try {
            message.setText(new Scanner(file).useDelimiter("\\Z").next());
        }catch (IOException e){
            e.printStackTrace();
        }
        file.delete();

        Button done = (Button) view.findViewById(R.id.dDoneBTN);
        done.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                MainMenuFragment mainMenu = new MainMenuFragment();
                getFragmentManager().beginTransaction().
                        replace(R.id.container, mainMenu).commit();
            }
        });
        return view;
    }

}//End of Class
