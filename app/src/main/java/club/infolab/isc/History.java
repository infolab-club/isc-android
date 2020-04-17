package club.infolab.isc;

class History {

    private String name;
    private String date;
    private String isLoaded;

    public History(String name, String date, String isLoaded){
        this.name = name;
        this.date = date;
        this.isLoaded = isLoaded;
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

    public String getIsLoaded() {
        return isLoaded;
    }

    public void setIsLoaded(String isLoaded) {
        this.isLoaded = isLoaded;
    }
}
