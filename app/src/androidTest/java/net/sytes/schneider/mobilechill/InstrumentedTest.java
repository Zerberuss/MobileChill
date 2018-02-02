package net.sytes.schneider.mobilechill;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.ImageButton;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

/**
 * Created by Timo Hasenbichler on 30.01.2018.
 */
@RunWith(AndroidJUnit4.class)
public class InstrumentedTest extends ActivityInstrumentationTestCase2<LocationActivity> {


    private LocationActivity mTestActivity;
    private ImageButton mTestButton;


    public InstrumentedTest() {
        super(LocationActivity.class);
    }

    @Rule
    public ActivityTestRule<LocationActivity> rule = new ActivityTestRule<LocationActivity>(LocationActivity.class);

    @Before
    public void setUp() throws Exception {
        super.setUp();
        mTestActivity = rule.getActivity();
        mTestButton = (ImageButton) mTestActivity
                .findViewById(R.id.remove_btn_id);
    }


    @Test
    public void checkButtons() {
        assertNotNull("mTestActivity is null", mTestActivity);
        assertNotNull("mTestButton is null", mTestButton);
    }
}
