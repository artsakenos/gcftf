package tk.artsakenos.geocachingftf;

import android.os.AsyncTask;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Params, Progress, Result
 * Result: ArrayList of String[]:= Name, Link, ...
 */
public class RetrieveWebPages extends AsyncTask<String, CacheItem, ArrayList<CacheItem>> {

    public static final String UserAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_6_8) AppleWebKit/534.30 (KHTML, like Gecko) Chrome/12.0.742.122 Safari/534.30";
    // public static final String URL_HK = "http://www.geocaching.com/seek/nearest.aspx?country_id=91&as=1&ex=0&cFilter=9a79e6ce-3344-409c-bbe9-496530baf758&children=n";
    public static final String URL_MODEL_Country = "http://www.geocaching.com/seek/nearest.aspx?country_id="; // 1 (HK)
    public static final String URL_MODEL_State = "http://www.geocaching.com/seek/nearest.aspx?state_id="; // 2 (Sardegna)

    private MyActivity myActivity;

    public RetrieveWebPages(MyActivity myActivity) {
        this.myActivity = myActivity;
    }

    protected ArrayList<CacheItem> doInBackground(String... urls) {

        String url = urls[0];

        ArrayList<CacheItem> result = new ArrayList<CacheItem>();
        Document document;

        try {
            // URL url= new URL(urls[0]);
            document = Jsoup.connect(url)
                    .userAgent(UserAgent)
                    .get();
        } catch (IOException ex) {
            // myActivity.log("ERROR IOException doInBackground(): " + ex.getLocalizedMessage());
            return null;
        } catch (Exception e) {
            // myActivity.log("ERROR Exception doInBackground(): " + e.getLocalizedMessage());
            return null;
        }

        // html = document.html();
        Elements elements = document.select(".Data");
        // html = elements.html();

        for (Element e : elements) {
            String[] partial = {"", "", "", "", "", "", ""};
            String fieldDataTrovato = e.select(".small").get(4).text();
            if (fieldDataTrovato.trim().isEmpty()) {

                String imgUrl = e.select("img").first().absUrl("src");
                String imgTitle = e.select("img").first().attr("title");
                // System.out.println("ImgUrl>>>" + imgUrl);
                // System.out.println("ImgTitle>>>" + imgTitle);

                partial[0] = e.select("a").get(2).text(); // name
                partial[1] = e.select("a").attr("abs:href"); // link
                partial[2] = e.select(".small").get(1).text(); // author | code | place
                partial[3] = e.select(".small").get(2).text(); // Difficulty
                partial[4] = e.select(".small").get(3).text(); // Date Placed

                CacheItem ci = new CacheItem(partial[0], partial[1], partial[2], partial[3], partial[4]);
                result.add(ci);
                publishProgress(ci);
            }
        }
        return result;
    }

    @Override
    protected void onProgressUpdate(CacheItem... values) {
        super.onProgressUpdate(values);
        CacheItem ci = values[0];
        // myActivity.addItems(ci);
    }

    protected void onPostExecute(ArrayList<CacheItem> result) {
        myActivity.log(myActivity.getString(R.string.lbl_retrieved));

        if (result == null) {
            myActivity.log(myActivity.getString(R.string.lbl_error_retrieving));
            myActivity.scheduleNextCheck(60 * 1000);
            return;
        } else {
            myActivity.addAllItems(result);
            // myActivity.scheduleNextCheck(myActivity.getResources().getInteger(R.integer.next_check_time));
            myActivity.scheduleNextCheck(myActivity.mCheckInterval);
            myActivity.playNotification();
        }

        if (result.isEmpty()) {
            myActivity.log(myActivity.getString(R.string.lbl_no_new_caches));
        }

        // for (String[] item : result) {
        // myActivity.addItems(item);

    }
}