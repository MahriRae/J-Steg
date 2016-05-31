package com.stegatective.abcd.j_steg;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

/**
 * Created by Mariah on 4/21/2016.
 */
public class DecryptionActivityFragment extends Fragment {

    public DecryptionActivityFragment(){
        //-----Do Nothing-----
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState){
        //Inflate the DecryptionActivityFragment xml to the container
        View view = inflater.inflate(R.layout.fragment_decryption, container, false);

        //Get the image path from the MainMenu
        String selectedImagePath = this.getArguments().getString("imagePath");

        //Put the selected image into the image view
        ImageView selectedImage = (ImageView)view.findViewById(R.id.selectedEncryptedImage);
        selectedImage.setImageBitmap(BitmapFactory.decodeFile(selectedImagePath));

        final EditText pinFeild = (EditText)view.findViewById(R.id.imagePinNumber);


        Button decrypt = (Button)view.findViewById(R.id.decryptBTN);
        decrypt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //When the decrypt button is pressed call the decrypt image function from the
                //MainActivity which will tell the Stego Manager to decrypt the image.
                String pin = pinFeild.getText().toString();

                Toast.makeText(((MainActivity)getActivity()).getBaseContext(), "Decrypting...",
                        Toast.LENGTH_SHORT).show();

                if(!pin.isEmpty())
                    ((MainActivity)getActivity()).decryptImage(pin);
                else
                    ((MainActivity)getActivity()).decryptImage("0");
            }
        });

        Button cancel = (Button)view.findViewById(R.id.cancelBTN);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //When the user cancels Return to the Main Menu
                MainMenuFragment mainMenu = new MainMenuFragment();
                getFragmentManager().beginTransaction().replace(R.id.container, mainMenu).commit();
            }
        });

        return view;
    }
}
