package tedsu.um.chatrooms;

import android.net.Uri;
import android.os.Bundle;

import com.google.android.material.tabs.TabLayout;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import es.umu.multilocation.android.bus.events.PositionUpdateEvent;
import es.umu.multilocation.android.bus.events.TechnologiesInitializationResultEvent;
import es.umu.multilocation.android.helpers.MultilocationHelper;
import es.umu.multilocation.core.exceptions.PositioningException;
import es.umu.multilocation.core.managers.data.PositionInfo;
import es.umu.multilocation.core.technologies.Technology;
import tedsu.um.chatrooms.fragments.ChatFragment;
import tedsu.um.chatrooms.fragments.LocationFragment;
import tedsu.um.chatrooms.fragments.ModeFragment;
import tedsu.um.chatrooms.messages.events.LocationChangeEvent;
import tedsu.um.chatrooms.ui.main.SectionsPagerAdapter;

public class MainActivity extends AppCompatActivity implements ChatFragment.OnFragmentInteractionListener, LocationFragment.OnFragmentInteractionListener, ModeFragment.OnFragmentInteractionListener {
    private String senderRoutingKey;
    private String receiverRoutingKey;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
    }

    public void onStart() {
        super.onStart();
        // Register to the event bus and initialize the multilocation object
        EventBus.getDefault().register(this);
        Log.d("mainapp", "Start event");
        if (((MyApplication) this.getApplication()).getMode() == null) {
            ((MyApplication) this.getApplication()).setMode("building");
        }

        if (((MyApplication) this.getApplication()).getMultilocationHelper() == null) {
            MultilocationHelper multilocationHelper = null;
            try {
                multilocationHelper = new MultilocationHelper(this, "Multilocation", 1000, 0, 1);
            } catch (PositioningException e) {
                e.printStackTrace();
            }

            ((MyApplication) this.getApplication()).setMultilocationHelper(multilocationHelper);
        }
    }

    public void onStop() {
        EventBus.getDefault().unregister(this);
        Log.d("mainapp", "Stop event");
        super.onStop();
    }

    //Enable the requires technologies
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void unTechnologiesInitializationResultEvent (TechnologiesInitializationResultEvent event) {
        Log.d("mainapp", "Multilocation inicializado.");

        MultilocationHelper helper = ((MyApplication) this.getApplication()).getMultilocationHelper();
        helper.enableTechnology(Technology.WIFI_TAG);
        Log.d("mainapp", "Enable Wifi.");
        helper.enableLearningMode(false);
        Log.d("mainapp", "Enabling learning mode.");
        helper.enablePositioningMode();
        Log.d("mainapp", "Enabling positioning mode.");

        Toast.makeText(this, "Library initialized!", Toast.LENGTH_SHORT).show();
    }

    //Event which receives the positions, sends an event with the new location and the new routing keys
    @Subscribe (threadMode = ThreadMode.MAIN)
    public void onPositionUpdateEvent(PositionUpdateEvent event) {
        //Toast.makeText(this, "Hi!", Toast.LENGTH_SHORT).show();
        if (!event.positions.isEmpty()) {
            PositionInfo position = event.positions.get(0);
            if  (position.getTag().length() > 0) {
                String[] items = getItems(position.getTag());
                if (items.length > 2) {
                    TextView textView = findViewById(R.id.title);
                    Log.d("mainapp", position.getTag());
                    String building = items[0];
                    Log.d("mainapp", building);
                    String floor = items[1];
                    Log.d("mainapp", floor);
                    String room = items[2];
                    Log.d("mainapp", room);
                    ///To make floor tests:
                    //building = "informatica";
                    //floor = "0";
                    //room = "home";
                    senderRoutingKey = building + "." + floor + "." + room;
                    String mode = getMode();
                    receiverRoutingKey = generateReceiverRoutingKey(building, floor, room, mode);
                    textView.setText(senderRoutingKey +" "+mode.substring(0,1).toUpperCase());
                    printLocation(building, floor, room);
                }
            }
        }
    }

    // Retrives the position elements and returns them like an array
    public String[] getItems(String cadena) {
        return cadena.split("\\.");
    }

    //Giving the position elements and the mode, returns the retrieve routing key
    public String generateReceiverRoutingKey (String building, String floor, String room, String mode) {
        switch(mode)
        {
            case "building":
                return building + '.' + '*' + '.' + '*';
            case "floor":
                return building + '.' + floor + '.' + '*';
            case "room":
                return building + '.' + floor + '.' + room;
            default:
                return building + '.' + floor + '.' + room;
        }
    }

    // Sends the event with the new location
    public void printLocation (String building, String floor, String room) {
        EventBus.getDefault().post(new LocationChangeEvent(building, floor, room, senderRoutingKey, receiverRoutingKey));
    }

    public String getSenderRoutingKey () {
        return senderRoutingKey;
    }

    public String getReceiverRoutingKey () {
        return receiverRoutingKey;
    }

    public String getMode () {
        return ((MyApplication) this.getApplication()).getMode();
    }

    public void setMode (String mode) {
        ((MyApplication) this.getApplication()).setMode(mode);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}