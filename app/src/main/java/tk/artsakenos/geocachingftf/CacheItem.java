package tk.artsakenos.geocachingftf;

import java.util.ArrayList;
import java.util.HashSet;

public class CacheItem {

    public final static int STATUS_NORMAL = 0;
    public final static int STATUS_FOUND = -1;
    public final static int STATUS_NEW = 1;

    public String name = "";    // 0
    public String link = "";    // 1
    public String author_code_place = "";   // 2
    public String difficulty = "";  // 3
    public String datePlaced = "";  // 4
    public String author = "";   // 2.0
    public String code = "";   // 2.1
    public String place = "";   // 2.2
    public int status = 0;

    public CacheItem(String name, String link, String author_code_place, String difficulty, String datePlaced) {
        this.name = name;
        this.link = link;
        this.author_code_place= author_code_place;
        this.difficulty = difficulty;
        this.datePlaced = datePlaced;

        String moreData[] = author_code_place.split("\\|");
        this.author = moreData[0].trim();
        this.code = moreData[1].trim();
        this.place = moreData[2].trim();
    }

    public static CacheItem getCache(ArrayList<CacheItem> lstItems, String code) {
        for (CacheItem ci : lstItems) {
            if (ci.code.equals(code)) return ci;
        }
        return null;
    }

    public String toString() {
        return name + "\t" +
                link + "\t" +
                author_code_place + "\t" +
                difficulty + "\t" +
                datePlaced;
    }

    public static ArrayList<CacheItem> fromStrings(ArrayList<String> lstItemsString) {
        ArrayList<CacheItem> output = new ArrayList<CacheItem>();
        for (String s : lstItemsString) {
            String field[] = s.split("\t");
            CacheItem ci = new CacheItem(field[0], field[1], field[2], field[3], field[4]);
            output.add(ci);
        }
        return output;
    }

    public static HashSet<String> toStrings(ArrayList<CacheItem> lstItems) {
        HashSet<String> lstItemsString = new HashSet<String>();
        for (CacheItem ci : lstItems) {
            if (ci.status!=STATUS_FOUND){
                lstItemsString.add(ci.toString());
            }
        }
        return lstItemsString;
    }

    public static CacheItem removeItem(ArrayList<CacheItem> lstItems, String code) {
        CacheItem toRemove = null;
        for (CacheItem ci : lstItems) {
            if (ci.code.equals(code)) {
                toRemove = ci;
            }
        }
        if (toRemove != null) {
            lstItems.remove(toRemove);
        }
        return toRemove;
    }
}
