import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Vector;

public class Helper {
    public static void main(String args[]){
    }

    public static void verifyConsistency(Map<Integer, Vector<Integer>> gatheredLocalSnapshots, int n){
        boolean consistent = true;

        for (Map.Entry<Integer, Vector<Integer>> entry : gatheredLocalSnapshots.entrySet()) {
            int current = entry.getKey();

            for (int i=0; i<n; i++){
                if (i == current) {
                    continue;
                }
            }
        }
    }
}
