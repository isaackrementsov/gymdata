package com.webapps.gymdata;

import com.webapps.gymdata.models.Member;
import com.webapps.gymdata.models.Scan;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import java.io.InputStream;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

/*Any imports that contain fazecast are from the jserailcomm jar file. 
This is the file i needed to use for the serial communication between my RFID scanner and my Web App*/

/*This code comes mostly from a youtbe tutorial 
https://arduino.stackexchange.com/questions/16750/reading-arduino-serial-stream-in-java-using-*/ 

/*I need the help of Andrea (tutor provided by BIS) in order to turn the reading of bytes into a count
There was a problem with my code because 1 scan was being counted as multiple. Andrea helped me write code to count 
each scan only if there is half a second between them.
*/

public class PortRead {
    
  public static SerialPort userPort;
  public static int counter;
  static InputStream in;
  public static long last_scan;
  public static String curr_scan;

  public PortRead() {
    Scanner input = new Scanner(System.in);
    /*
     This returns an array of commport addresses, not useful for the client
     but useful for iterating through to get an actual list of com parts available
    */
    SerialPort ports[] = SerialPort.getCommPorts();
    int i = 1;
    counter = 0;
    last_scan = 0;
    curr_scan = "";
    //User port selection
    System.out.println("COM Ports available on machine");
    for (SerialPort port : ports) {
      //iterator to pass through port array
      System.out.println(i++ + ": " + port.getSystemPortName()); //print windows com ports
    }
    System.out.println("Please select COM PORT: 'COM#'");
    SerialPort userPort = SerialPort.getCommPort("COM5");

    //Initializing port
    userPort.openPort();
    if (userPort.isOpen()) {
    System.out.println("Port initialized!");
    //timeout not needed for event based reading
    //userPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 100, 0);
    } else {
    System.out.println("Port not available");
    return;
    }

    userPort.addDataListener(new SerialPortDataListener() {
      
      @Override
      public int getListeningEvents() {
        return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
      }
      
      public void serialEvent(SerialPortEvent event) {
        if (event.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE)
        return;
        byte[] newData = new byte[userPort.bytesAvailable()];
        int numRead = userPort.readBytes(newData, newData.length);
        //System.out.println("Read " + numRead + " bytes.");
        long timeElapsed = System.currentTimeMillis() - last_scan;
        if (timeElapsed > 500)
        {
            saveScan();
            curr_scan = "";
            System.out.println("Scan nr " + counter );
        }
        last_scan = System.currentTimeMillis();
        curr_scan = curr_scan + newData;
        //System.out.println("Scanning: " + curr_scan );
        
      }
    
    });
  }
  
    public Scan saveScan(){
        Random random = new Random();
        // Randomly select a member to add this scan to
        List<Member> members = Member.getAll();
        Member member = members.get(random.nextInt(members.size()));

        // See if the member's most recent scan was in or out
        List<Scan> previousScans = member.getScans();
        Collections.sort(previousScans, new Scan.DateComparator());
        boolean lastScanIn = previousScans.size() > 0 ? previousScans.get(0).getScanIn() : false;
                
        Scan scan = new Scan();
        scan.setMember(member);
        scan.setDate(new Date());
        // If the user's last scan was in, this is a scan out. If the last scan was out, this is a scan in
        scan.setScanIn(!lastScanIn);
        scan.save();
        
        // Increment/decrement counter depending on whether the scan was in or out
        if(scan.getScanIn()) counter++;
        else counter--;
        
        return scan;
    }
}
