package tedsu.um.chatrooms;

import android.app.Application;
import android.widget.Toast;

import es.umu.multilocation.android.helpers.MultilocationHelper;

public class MyApplication extends Application {
    String mode;
    private MultilocationHelper multilocationHelper;
    public MultilocationHelper getMultilocationHelper() {
        return multilocationHelper;
    }

    public void setMultilocationHelper(MultilocationHelper multilocationHelper) {
        this.multilocationHelper = multilocationHelper;
        Toast.makeText(this, "MultilocationHelperSet",Toast.LENGTH_SHORT);
    }
    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

}
