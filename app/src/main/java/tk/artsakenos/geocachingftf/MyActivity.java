package tk.artsakenos.geocachingftf;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;


public class MyActivity extends Activity {
    public static final String STORAGE = "geocachingftf_storage_";
    Notification notificationBar = null;
    private SharedPreferences prefs_private;
    private SharedPreferences prefs_settings;
    private Ringtone ringtone = null;
    private String countryCode = "191"; // 2230:sardegna, 2196:lazio;
    // public static String countryName = "Hong Kong";
    private boolean theme_dark = true;
    private boolean new_caches_notification = true;
    private String cache_type = "all";
    public final static String check_frequency_MIN = "15";
    private String check_frequency = check_frequency_MIN;
    private SharedPreferences.OnSharedPreferenceChangeListener onSharedPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equals("lst_countries")) {
                countryCode = prefs_settings.getString("lst_countries", "191");
                loadPage(true);
            }
            if (key.equals("lst_cache_types")) {
                cache_type = prefs_settings.getString("lst_cache_types", "all");
            }
            if (key.equals("edt_check_frequency")) {
                check_frequency = prefs_settings.getString("edt_check_frequency", check_frequency_MIN);
                mCheckInterval = Integer.parseInt(check_frequency) * 1000 * 60;
            }
            if (key.equals("cbx_new_caches_notification")) {
                new_caches_notification = prefs_settings.getBoolean("cbx_new_caches_notification", true);
            }
        }
    };
    private boolean newCache = false;
    // *********************************************************************************************
    // *********************************************************************************************
    public long mCheckInterval = 10 * 1000; // 10 seconds by default, can be changed if download is ok!
    private Handler mHandler;
    // *********************************************************************************************
    // *********************************************************************************************
    private TextView txtLog = null;
    private TextView txtStatus = null;
    private ListView lstView = null;
    private ArrayList<CacheItem> lstItems = new ArrayList<>();
    private ArrayList<String> lstItems_archived = new ArrayList<>();
    private CacheItemAdapter lstAdapter;

    // *********************************************************************************************
    // *********************************************************************************************
    public void log(String message) {
        txtLog.setText(message);
        // System.out.println("DEBUG------------------: " + message);
    }

    public void addAllItems(ArrayList<CacheItem> items) {

        ///-{ Leggo le nuove cache e le metto dentro il lstItems
        ///-{ 1) Segno tutte le cache come trovate

        for (CacheItem ci : lstItems) {
            ci.status = CacheItem.STATUS_FOUND;
        }

        for (CacheItem ci : items) {
            CacheItem oldCache = CacheItem.getCache(lstItems, ci.code);
            if (oldCache == null) {
                // Nuova Cache, la aggiungiamo come new
                ci.status = CacheItem.STATUS_NEW;
                if (!lstItems_archived.contains(ci.code)) {
                    lstItems.add(ci);
                    newCache = true;
                }
            } else {
                oldCache.status = CacheItem.STATUS_NORMAL;
            }
            lstView.invalidateViews();
            lstAdapter.notifyDataSetChanged();
        }
        ///-{ Chiamo un ultima volta nel caso non ci siano cache per niente per piazzargli l'empty.
        lstView.invalidateViews();
        lstAdapter.notifyDataSetChanged();
    }

    /**
     * @param clear Cancella listItems prima di ricaricarla, utile se si sta cambiando country.
     *              Se clear, notifica le caches come nuove.
     */
    public void loadPage(boolean clear) {
        if (!isNetworkAvailable()) {
            log(getString(R.string.lbl_no_network));
            scheduleNextCheck(15 * 60 * 1000);
            return;
        }

        log(getString(R.string.lbl_wait));
        String model = countryCode.substring(0, 1);
        String country = countryCode.substring(1);

        String url = RetrieveWebPages.URL_MODEL_Country;
        if (model.equals("2")) url = RetrieveWebPages.URL_MODEL_State;
        url += country;

        if (cache_type.equals("traditional")) {
            url += "&cFilter=32bc9333-5e52-4957-b0f6-5a2c8fc7b257&as=1"; // Traditional
        }
        if (cache_type.equals("multicache")) {
            url += "&cFilter=a5f6d0ad-d2f2-4011-8c14-940a9ebf3c74&as=1"; // MultiCache
        }

        if (clear) {
            lstItems.clear();
        }
        AsyncTask<String, CacheItem, ArrayList<CacheItem>> taskExecutor = new RetrieveWebPages(this);
        taskExecutor.execute(url);

        scheduleNextCheck(0);
    }

    private Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            loadPage(false); // This function can change value of mInterval.
            mHandler.postDelayed(mStatusChecker, mCheckInterval);
        }
    };

    public void scheduleNextCheck(long delay_milliseconds) {
        if (delay_milliseconds == 0) {
            String now = getString(R.string.lbl_now);
            String status = String.format(Locale.getDefault(), getString(R.string.txt_status), now);
            txtStatus.setText(status);
            return;
        }

        long time = Calendar.getInstance().getTimeInMillis();
        mCheckInterval = delay_milliseconds;
        time += mCheckInterval;
        Date date = new Date(time);

        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        String dateString = format.format(date);

        // "[" + countryName + "] " +
        String status = String.format(Locale.getDefault(), getString(R.string.txt_status), dateString);
        txtStatus.setText(status);

        mHandler.removeCallbacks(mStatusChecker);
        mHandler.postDelayed(mStatusChecker, mCheckInterval);
    }

    public void playNotification() {
        if (!newCache) return;
        if (!new_caches_notification) return;
        if (ringtone == null) return;
        newCache = false;

        try {
            if (!ringtone.isPlaying()) {
                ringtone.play();

                NotificationManager notificationManager =
                        (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                notificationManager.notify(0, notificationBar);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    // *********************************************************************************************
    // Life Cycle
    // *********************************************************************************************
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

        ///-{ 1) Load Settings
        prefs_private = getPreferences(Context.MODE_PRIVATE);
        prefs_settings = PreferenceManager.getDefaultSharedPreferences(this);
        lstItems_archived = new ArrayList<>(prefs_private.getStringSet(STORAGE + "lstItems_Archived", new HashSet<String>()));
        ArrayList<String> lstItemsStrings = new ArrayList<>(prefs_private.getStringSet(STORAGE + "lstItems", new HashSet<String>()));
        lstItems = CacheItem.fromStrings(lstItemsStrings);
        theme_dark = prefs_private.getBoolean(STORAGE + "theme_dark", true);

        prefs_settings.registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
        new_caches_notification = prefs_settings.getBoolean("cbx_new_caches_notification", true);
        countryCode = prefs_settings.getString("lst_countries", "191");
        cache_type = prefs_settings.getString("lst_cache_types", "all");
        check_frequency = prefs_settings.getString("edt_check_frequency", check_frequency_MIN);
        mCheckInterval = Integer.parseInt(check_frequency) * 60 * 1000;

        ///-{ 2) Prepare interface
        txtLog = (TextView) findViewById(R.id.txtLog);
        txtStatus = (TextView) findViewById(R.id.txtStatus);
        Button btnRefresh = (Button) findViewById(R.id.btnRefresh);
        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadPage(false);
            }
        });

        lstView = (ListView) findViewById(R.id.lstView);
        lstAdapter = new CacheItemAdapter(this, R.layout.cache_item_layout, lstItems);
        lstView.setAdapter(lstAdapter);
        registerForContextMenu(lstView);
        lstView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long positionL) {
                CacheItem ci = (CacheItem) lstView.getItemAtPosition(position);
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(ci.link));
                startActivity(browserIntent);
            }
        });

        View empty = findViewById(R.id.empty);
        lstView.setEmptyView(empty);

        ///-{ Creazione dei notificatori
        Uri notificationSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        // if (alarm) notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        ringtone = RingtoneManager.getRingtone(getApplicationContext(), notificationSound);

        Intent intent = new Intent(this, MyActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT);
        // PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.gc_logo)
                        .setContentTitle("New Cache Available!")
                        .setContentText("Check the new available cache.")
                                // .addAction(R.drawable.gc_logo, "Open!", pIntent)
                        .setContentIntent(pIntent)
                        .setAutoCancel(true);

        notificationBar = notificationBuilder.build();

        ///-{ 4) Handler per ripetere il check
        mHandler = new Handler();
        mStatusChecker.run();
    }

    @Override
    protected void onPause() {
        super.onPause();

        SharedPreferences.Editor editor = prefs_private.edit();
        editor.putStringSet(STORAGE + "lstItems", CacheItem.toStrings(lstItems));
        editor.putStringSet(STORAGE + "lstItems_Archived", new HashSet<>(lstItems_archived));
        editor.putBoolean(STORAGE + "theme_dark", theme_dark);
        editor.apply();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (isFinishing()) {
            mHandler.removeCallbacks(mStatusChecker);
            finish();
            System.exit(888);
        }
    }

    @Override
    public void onContentChanged() {
        super.onContentChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my, menu);
        return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if (v.getId() == R.id.lstView) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
            menu.setHeaderTitle(lstItems.get(info.position).name);
            String[] menuItems = getResources().getStringArray(R.array.lstview_menu);
            for (int i = 0; i < menuItems.length; i++) {
                menu.add(Menu.NONE, i, i, menuItems[i]);
            }
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int menuItemIndex = item.getItemId();
        CacheItem ci = (CacheItem) lstView.getItemAtPosition(info.position);

        switch (menuItemIndex) {
            case 0: // Archive
                CacheItem.removeItem(lstItems, ci.code);
                lstItems_archived.add(ci.code);
                lstAdapter.notifyDataSetChanged();
                break;
            case 1: // Open Url
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(ci.link));
                startActivity(browserIntent);
                break;
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        if (id == R.id.action_restore_archived) {
            lstItems_archived.clear();
            loadPage(false);
        }

        if (id == R.id.action_help) {
            Intent intent = new Intent(this, HelpActivity.class);
            startActivity(intent);
            return true;
        }

        if (id == R.id.action_rate) {
            Uri uri = Uri.parse("market://details?id=" + getPackageName());
            Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
            try {
                startActivity(goToMarket);
            } catch (ActivityNotFoundException e) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + getPackageName())));
            }
            return true;
        }

        if (id == R.id.action_pro) {
            Uri uri = Uri.parse("market://details?id=" + getPackageName() + "_pro");
            Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
            try {
                startActivity(goToMarket);
            } catch (ActivityNotFoundException e) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + getPackageName() + "_pro")));
            }
            return true;
        }

        if (id == R.id.action_dark_theme) {
            if (theme_dark) {
                setTheme(R.style.AppTheme);
            } else {
                setTheme(R.style.AppThemeDark);
            }
            setContentView(R.layout.activity_my);
            theme_dark = !theme_dark;
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    // *********************************************************************************************
    // Listeners
    // *********************************************************************************************


}