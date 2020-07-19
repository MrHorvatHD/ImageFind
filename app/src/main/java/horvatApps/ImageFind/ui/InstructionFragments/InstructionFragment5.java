package horvatApps.ImageFind.ui.InstructionFragments;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import horvatApps.ImageFind.R;

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
        finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                storeSharedPref();
                getActivity().finish();
            }
        });

        return view;
    }

    //writes to the shared preferences
    private void storeSharedPref(){
        SharedPreferences sharedPref = getContext().getSharedPreferences("ImageScanPref", 0);

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("isFirst", false);
        editor.apply();
    }
}
