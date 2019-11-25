package tedsu.um.chatrooms;

import android.net.Uri;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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
import tedsu.um.chatrooms.fragments.MapFragment;
import tedsu.um.chatrooms.messages.actions.ChatNewMessageAction;
import tedsu.um.chatrooms.messages.events.LocationChangeEvent;
import tedsu.um.chatrooms.ui.main.SectionsPagerAdapter;

public class MainActivity extends AppCompatActivity implements ChatFragment.OnFragmentInteractionListener, LocationFragment.OnFragmentInteractionListener, MapFragment.OnFragmentInteractionListener {

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
        EventBus.getDefault().register(this);
        Log.d("mainapp", "Start event");
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void unTechnologiesInitializationResultEvent (TechnologiesInitializationResultEvent event) {
        Log.d("mainapp", "Multilocation inicializado.");

        MultilocationHelper helper = ((MyApplication) this.getApplication()).getMultilocationHelper();
        helper.enableTechnology(Technology.GPS_TAG);
        Log.d("mainapp", "Enable GPS.");
        helper.enableTechnology(Technology.NET_TAG);
        helper.enableTechnology(Technology.NET_TAG);
        Log.d("mainapp", "Enable NET.");
        helper.enableTechnology(Technology.WIFI_TAG);
        Log.d("mainapp", "Enable Wifi.");
        helper.enableLearningMode(false);
        Log.d("mainapp", "Enabling learning mode.");
        helper.enablePositioningMode();
        Log.d("mainapp", "Enabling positioning mode.");

        Toast.makeText(this, "Library initialized!", Toast.LENGTH_SHORT).show();
    }

    @Subscribe (threadMode = ThreadMode.MAIN)
    public void onPositionUpdateEvent(PositionUpdateEvent event) {
        //Toast.makeText(this, "Hi!", Toast.LENGTH_SHORT).show();
        if (!event.positions.isEmpty()) {
            TextView textView = findViewById(R.id.title);
            PositionInfo position = event.positions.get(0);
            textView.setText(position.getTag());
            String[] items = getItems(position.getTag());
            Log.d("mainapp", position.getTag());
            String building = items[0];
            Log.d("mainapp", building);
            String floor = items[1];
            Log.d("mainapp", floor);
            String room = items[2];
            Log.d("mainapp", room);
            printLocation(building,floor, room);
        }
    }

    public String[] getItems(String cadena) {
        return cadena.split("\\.");
    }

    public void writeMsgOnChat(String message, String origin){
        EventBus.getDefault().post(new ChatNewMessageAction(message, origin));
    }

    public void printLocation (String building, String floor, String room) {
        EventBus.getDefault().post(new LocationChangeEvent(building, floor, room));
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}