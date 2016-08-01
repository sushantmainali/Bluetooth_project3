package EventMessages;

import android.app.Fragment;

/**
 * Created by Sushant Mainali on 3/15/2016.
 */
public class ChangeFragmentEvent
{
    public final Fragment main_fragment_to_replace;

    public ChangeFragmentEvent(Fragment fragment_id_to_change)
    {
        this.main_fragment_to_replace = fragment_id_to_change;
    }
}
