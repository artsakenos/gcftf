package tk.artsakenos.geocachingftf;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ViewAnimator;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

/**
 * <com.google.android.gms.ads.AdView
 * android:id="@+id/adView"
 * android:layout_width="match_parent"
 * android:layout_height="wrap_content"
 * ads:adSize="BANNER"
 * ads:adUnitId="@string/banner_ad_unit_id"
 * android:layout_below="@+id/txtLog"
 * android:layout_alignParentLeft="true"
 * android:layout_alignParentStart="true"
 * android:layout_alignParentRight="true"
 * android:layout_alignParentEnd="true" />
 *
 *     <view
 class="tk.artsakenos.geocachingftf.U_AdView"
 android:layout_width="match_parent"
 android:layout_height="wrap_content"
 android:id="@+id/adView"
 android:layout_below="@+id/btnRefresh"
 android:layout_alignParentLeft="true"
 android:layout_alignParentStart="true" />
 *
 * Created by Andrea on 2015-01-08.
 */
public class U_AdView extends ViewAnimator {
    private AdView googleAdView;
    private AdRequest mAdRequest;

    public void init(Context context) {

        googleAdView = new AdView(context);
        googleAdView.setAdSize(AdSize.BANNER);
        googleAdView.setAdUnitId("ca-app-pub-1499106218978814/1094829686");

        mAdRequest = new AdRequest.Builder()
                // .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();

        // Start loading the ad in the background.
        googleAdView.loadAd(mAdRequest);
        this.addView(googleAdView);
    }

    public U_AdView(Context context) {
        super(context);
        init(context);
    }




}
