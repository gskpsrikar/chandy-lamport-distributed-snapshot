import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Vector;

import javax.swing.SizeRequirements;

public class Helper {
    public static void main(String args[]){
    }

    public static void verifyConsistency(Map<Integer, Vector<Integer>> gatheredLocalSnapshots, int n){
        boolean consistent = true;

        for (Map.Entry<Integer, Vector<Integer>> entry : gatheredLocalSnapshots.entrySet()) {
            int current = entry.getKey();

            for (int i=0; i<n; i++){
                if (gatheredLocalSnapshots.containsKey(i)){
                    int reference = gatheredLocalSnapshots.get(i).get(i);
                    for (int j=0; j<n; j++){
                        if (gatheredLocalSnapshots.containsKey(j)){
                            if (gatheredLocalSnapshots.get(j).get(i) > reference) {
                                consistent = false;
                            }
                        }
                    }
                }
            };
        };

        if (consistent){
            System.out.println("**************** CONSISTENCY VERIFIED ****************");
        } else {
            System.out.println("**************** CONSISTENCY FAILED ****************");
        }
    }

    public static void writeOutput(int nodeId, Vector<Integer> clock) throws Exception{

        String filename = String.format("config-%d.out", nodeId);

        FileOutputStream stream = new FileOutputStream(filename, true);
        PrintWriter writer = new PrintWriter(stream);

        for (Integer i : clock) {
            writer.print(i);
            writer.print(" ");
        }
        writer.println();
        writer.close();
        stream.close();
    }
}
