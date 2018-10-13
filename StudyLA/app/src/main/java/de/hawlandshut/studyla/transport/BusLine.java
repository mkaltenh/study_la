package de.hawlandshut.studyla.transport;

/*
* Objektklasse: BusLine
* Wird zur Übergabe von einzelnen BusLinien an den BusAdapter verwendet
* @Fragment: TransportFragment
 */

public class BusLine {
    private String destination;
    private String time;
    private String line;
    private String type;


    public BusLine() {

    }

    public BusLine(String destination, String time, String line, String type){
        this.destination = destination;
        this.time = time;
        this.line = line;
        this.type = type;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getLine() {
        return line;
    }

    public void setLine(String line) {
        this.line = line;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}