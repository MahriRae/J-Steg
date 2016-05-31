package com.stegatective.abcd.j_steg;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by Mariah on 4/21/2016.
 */
public class EncryptionActivityFragment extends Fragment {

    public EncryptionActivityFragment(){
        //-----Do Nothing-----
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        //Get the selected image path that the user selected
        String selectedImagePath = this.getArguments().getString("imagePath");

        //Inflate the EncryptionActivityFragment xml to the container
        View view = inflater.inflate(R.layout.fragment_encryption, container, false);

        //Place the selected image from the user into the selectedImage image field.
        ImageView selectedImage = (ImageView)view.findViewById(R.id.selectedImage);
        selectedImage.setImageBitmap(BitmapFactory.decodeFile(selectedImagePath));

        //Find the users message field in the UI of the encryption activity
        final EditText userMessageField = (EditText)view.findViewById(R.id.userMessage);
        final EditText pinNumberField = (EditText)view.findViewById(R.id.setPinNumber);

        Button encrypt = (Button)view.findViewById(R.id.encryptBTN);
        encrypt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Tell the MainActivity to encrypt the Image that is within its selectedImagePath
                String userMessage = userMessageField.getText().toString();
                String pin = pinNumberField.getText().toString();

                Toast.makeText(((MainActivity)getActivity()).getBaseContext(), "Encrypting...",
                        Toast.LENGTH_SHORT).show();

                //Create a file directory (folder) to store the text of the user message into
                File filePath = new File(Environment.getExternalStorageDirectory(), "J-Steg");

                if(!filePath.exists()){
                    filePath.mkdirs();//This will create the folder "J-Steg"
                }
                //This will create a file named with the timeStamp of the time the picture was encrypted
                File messagePath = new File(filePath, System.currentTimeMillis() + ".txt");
                //Try to write to the file then close it when finished
                try {
                    FileWriter writer = new FileWriter(messagePath);
                    writer.append(userMessage);
                    writer.flush();
                    writer.close();
                } catch (IOException e){
                    e.printStackTrace();
                }

                if(!pin.isEmpty()) {
                    ((MainActivity)getActivity()).encryptImage(messagePath, pin);
                }
                else
                    ((MainActivity)getActivity()).encryptImage(messagePath, "0");
            }
        });

        Button cancel = (Button)view.findViewById(R.id.cancelBTN);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //User has canceled return to the Main Menu
                MainMenuFragment mainMenu = new MainMenuFragment();
                getFragmentManager().beginTransaction().
                        replace(R.id.container, mainMenu).commit();
            }
        });

        return view;
    }
}
