package club.infolab.isc.usb;

import android.hardware.usb.UsbDevice;

import com.hoho.android.usbserial.driver.UsbSerialDriver;

public class UsbController {
    public static boolean isConnect = false;
    public static UsbDevice device;
    public static int port;
    public static UsbSerialDriver driver;
}
