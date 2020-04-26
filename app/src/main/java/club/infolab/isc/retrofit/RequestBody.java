package club.infolab.isc.retrofit;

public class RequestBody {
    private String type;
    private String date;
    private String results;

    RequestBody(String type, String date, String results) {
        this.type = type;
        this.date = date;
        this.results = results;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getResults() {
        return results;
    }

    public void setResults(String results) {
        this.results = results;
    }
}
