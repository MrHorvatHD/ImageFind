package horvatApps.ImageFind.ui.InstructionFragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import horvatApps.ImageFind.R;
import horvatApps.ImageFind.ui.InstructionsActivity;
import horvatApps.ImageFind.ui.MainActivity;

/**
 * A simple {@link Fragment} subclass.
 */
public class InstructionFragment5 extends Fragment {

    public InstructionFragment5() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_instruction5, container, false);

        Button finishButton = view.findViewById(R.id.instructionButton);
        finishButton.setOnClickListener(v -> finishClicked());

        ImageView imageView = view.findViewById(R.id.imageView);
        imageView.setOnClickListener(v -> finishClicked());

        return view;
    }

    public void finishClicked(){
        storeSharedPref();

        //starts main activity on a new stack
        Intent intent = new Intent(getContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    //writes to the shared preferences
    private void storeSharedPref(){
        SharedPreferences sharedPref = getContext().getSharedPreferences("ImageScanPref", 0);

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("isFirst", false);
        editor.apply();
    }
}
