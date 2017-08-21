package tk.artsakenos.geocachingftf;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.widget.RemoteViews;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Calendar;


/**
 * Implementation of App Widget functionality.
 */
public class GCWidget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        final int N = appWidgetIds.length;
        for (int i = 0; i < N; i++) {
            updateAppWidget(context, appWidgetManager, appWidgetIds[i]);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            playNotification(context, true);
        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            playNotification(context, true);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
        playNotification(context, true);
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
        playNotification(context, true);
    }


    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        // CharSequence widgetText = context.getString(R.string.appwidget_text) + " " + Calendar.getInstance().getTime();
        String widgetText = "";

        playNotification(context, true);

        try {
            String url = "http://www.geocaching.com/seek/nearest.aspx?country_id=91";
            Document document = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_6_8) AppleWebKit/534.30 (KHTML, like Gecko) Chrome/12.0.742.122 Safari/534.30")
                    .get();

            // html = document.html();
            Elements elements = document.select(".Data");

            widgetText += " SIZE:" + elements.size();

        } catch (IOException ex) {
            // myActivity.log("ERROR IOException doInBackground(): " + ex.getLocalizedMessage());
            widgetText += " IO EXCEPTION:" + ex.getLocalizedMessage();
            ex.printStackTrace();
        } catch (Exception e) {
            widgetText += " EXCEPTION:" + e.getLocalizedMessage();
            e.printStackTrace();
            // myActivity.log("ERROR Exception doInBackground(): " + e.getLocalizedMessage());
        }

        widgetText += " DATE:" + Calendar.getInstance().getTime().getHours() + ":" + Calendar.getInstance().getTime().getMinutes();
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.gcwidget);
        views.setTextViewText(R.id.appwidget_text, widgetText);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    public static void playNotification(Context context, boolean alarm) {

        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            if (alarm) notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            Ringtone r = RingtoneManager.getRingtone(context, notification);
            if (!r.isPlaying())
                r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


