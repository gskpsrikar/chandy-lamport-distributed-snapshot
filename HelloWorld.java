public class HelloWorld {
    public static void main(String[] args) {

        int waitTime = 3;

        for (int i=0; i < waitTime; i++) {

            String message = String.format("Hello World! The time is %d seconds", i+1);
            System.out.println(message);
            
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                System.out.println(e);
            }
        }
        
    }
}
