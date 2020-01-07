package tedsu.um.chatrooms.fragments;

import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import tedsu.um.chatrooms.MainActivity;
import tedsu.um.chatrooms.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ModeFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ModeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ModeFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    private RadioGroup radioGroup;
    private RadioButton radioButton;
    TextView textView;
    View fragment_view;
    //One for each mode
    private RadioButton r1;
    private RadioButton r2;
    private RadioButton r3;
    private String mode;
    public ModeFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static ModeFragment newInstance(String param1, String param2) {
        ModeFragment fragment = new ModeFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        fragment_view = inflater.inflate(R.layout.fragment_mode, container, false);
        mode = ((MainActivity)getActivity()).getMode();
        radioGroup = fragment_view.findViewById(R.id.radioGroup);
        textView = fragment_view.findViewById(R.id.mode);
        r1 = fragment_view.findViewById(R.id.builidingButton);
        r2 = fragment_view.findViewById(R.id.floorButton);
        r3 = fragment_view.findViewById(R.id.roomButton);
        //When it is initialize, it allows to set checked radio button according to the current mode.
        if (mode == "building") {
            r1.setChecked(true);
        } else {
            r1.setChecked(false);
        }
        if (mode == "floor") {
            r2.setChecked(true);
        } else {
            r2.setChecked(false);
        }
        if (mode == "room") {
            r3.setChecked(true);
        } else {
            r3.setChecked(false);
        }

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // checkedId is the RadioButton selected
                radioButton = fragment_view.findViewById(checkedId);
                String text = (String)radioButton.getText();
                String mode = mappingButtonText2ModeString(text);
                Toast.makeText(getActivity(), mode + " mode", Toast.LENGTH_SHORT).show();
                ((MainActivity)getActivity()).setMode(mode);
            }
        });
        return fragment_view;
    }
    // returns de mode string depending on the radio button that has been pressed
    public String mappingButtonText2ModeString (String text){
        switch(text)
        {
            case "BUILDING MODE":
                return "building";
            case "FLOOR MODE":
                return "floor";
            case "ROOM MODE":
                return "room";
            default:
                return "building";
        }
    }
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
