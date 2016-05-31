package com.stegatective.abcd.j_steg;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;


/**
 * Created by Mariah on 4/21/2016.
 * The MainMenuFragment controls the layout for the main menu and activates the
 * Encryption or Decryption Activity based on a users button click. This will
 * allow the gallery activity to be opened and tell the MainActivity which
 * UI to give the imagePath to after it has been retrieved.
 */
public class MainMenuFragment extends Fragment implements OnClickListener {

    private int encryptionInterfaceControl = 1;
    private int decryptionInterfaceControl = 2;



    public MainMenuFragment() {
        //--------Do nothing------
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        //Inflate the main menu fragment xml, fragment_main into the container
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        //Create onClickListeners for the encryption and decryption buttons in the layout
        Button encryption = (Button)view.findViewById(R.id.encryptionBTN);
        encryption.setOnClickListener(this);

        Button decryption = (Button)view.findViewById(R.id.decryptionBTN);
        decryption.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View view){

        switch(view.getId()){
            //If the encryption button is pressed
            case R.id.encryptionBTN:
                //Set the interfaceControl in main to the the number of the encryption fragment
                ((MainActivity)getActivity()).setInterfaceControl(encryptionInterfaceControl);

                //Open the gallery and allow the user to select an image to be encrypted
                ((MainActivity)getActivity()).openGallery();
                break;

            //If the decryption button is pressed
            case R.id.decryptionBTN:
                //Set the interface control in main to the number of the decryption fragment
                ((MainActivity)getActivity()).setInterfaceControl(decryptionInterfaceControl);

                //Open the gallery ad allow the user to select an image to be decrypted
                ((MainActivity)getActivity()).openGallery();
                break;
    }
  }
}//END OF CLASS
