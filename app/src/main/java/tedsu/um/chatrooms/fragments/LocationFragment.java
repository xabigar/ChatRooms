package tedsu.um.chatrooms.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import tedsu.um.chatrooms.MainActivity;
import tedsu.um.chatrooms.R;
import tedsu.um.chatrooms.messages.actions.ChatNewMessageAction;
import tedsu.um.chatrooms.messages.events.LocationChangeEvent;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link LocationFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link LocationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LocationFragment extends Fragment {

    private TextView building;
    private TextView floor;
    private TextView room;
    private TextView srk;
    private TextView rrk;
    private OnFragmentInteractionListener mListener;

    public LocationFragment() {
        // Required empty public constructor
    }

    public static LocationFragment newInstance(String param1, String param2) {
        LocationFragment fragment = new LocationFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View fragment_view = inflater.inflate(R.layout.fragment_location, container, false);
        building = fragment_view.findViewById(R.id.buildingTag);
        floor = fragment_view.findViewById(R.id.floorTag);
        room = fragment_view.findViewById(R.id.roomTag);
        srk = fragment_view.findViewById(R.id.senderRK);
        rrk = fragment_view.findViewById(R.id.receiverRK);

        return fragment_view;
    }

    @Subscribe(threadMode= ThreadMode.MAIN)
    public void receiveNewLocation (LocationChangeEvent event){
        //TextView textView = new TextView(getActivity());

        building.setText("Building: "+event.getBuilding());
        floor.setText("Floor: "+event.getFloor());
        room.setText("Room: "+event.getRoom());
        srk.setText("SRK: "+event.getS());
        rrk.setText("RRK: "+event.getR());
    }


    @Override
    public void onStart(){
        super.onStart();
        EventBus.getDefault().register(this);
    }


    @Override
    public void onStop(){
        super.onStop();
        EventBus.getDefault().unregister(this);
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
