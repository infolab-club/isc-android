package club.infolab.isc.database;

import java.io.Serializable;
import java.sql.Date;

public class Record implements Serializable {
    private long id;
    private String name;
    private String date;
    // there isn't BOOL type in SQLite
    private int isLoaded;
    private String json;

    public Record(long id, String name, String date, int isLoaded, String json){
        this.id = id;
        this.name = name;
        this.date = date;
        this.isLoaded = isLoaded;
        this.json = json;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getIsLoaded() {
        return isLoaded;
    }

    public void setIsLoaded(int isLoaded) {
        this.isLoaded = isLoaded;
    }

    public String getJson() {
        return json;
    }

    public void setJson(String json) {
        this.json = json;
    }
}


