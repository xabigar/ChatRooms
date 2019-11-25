package tedsu.um.chatrooms.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import tedsu.um.chatrooms.R;
import tedsu.um.chatrooms.messages.actions.ChatNewMessageAction;


public class ChatFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

    private ScrollView scrollView;
    private LinearLayout lineas;

    public ChatFragment() {
        // Required empty public constructor
    }


    public static ChatFragment newInstance(String param1, String param2) {
        ChatFragment fragment = new ChatFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragment_view = inflater.inflate(R.layout.fragment_chat, container, false);

        scrollView = fragment_view.findViewById(R.id.scroll);
        lineas = fragment_view.findViewById(R.id.lineas);

        return fragment_view;
    }

    @Subscribe (threadMode=ThreadMode.MAIN)
    public void receiveChatMessagesFormActivy (ChatNewMessageAction chatNewMessageAction){
        TextView textView = new TextView(getActivity());

        textView.setText(chatNewMessageAction.getMessage());

        lineas.addView(textView);

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

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
