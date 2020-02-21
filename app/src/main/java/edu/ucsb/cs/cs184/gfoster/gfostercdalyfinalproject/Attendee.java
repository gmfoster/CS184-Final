package edu.ucsb.cs.cs184.gfoster.gfostercdalyfinalproject;

public class Attendee {
    public String email;
    public String username;
    public String mac_address;

    public Attendee() {

    }

    public Attendee(String email, String username, String mac_address) {
        this.email = email;
        this.username = username;
        this.mac_address = mac_address;
    }
}
