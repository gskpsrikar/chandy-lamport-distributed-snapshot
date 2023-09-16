import java.net.InetAddress;
import java.net.UnknownHostException;


public class HelloWorld {
    public static void main(String[] args) {

        int waitTime = 3;

        try {
            InetAddress localhost = InetAddress.getLocalHost();
            System.out.println("Hello from " + localhost.getHostAddress());
        } catch (UnknownHostException e) {
            System.out.println(e);
        }

        for (int i=0; i < waitTime; i++) {
            
            String message = String.format("The time is %d seconds", i+1);
            System.out.println(message);

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                System.out.println(e);
            }
        }
        
    }
}
