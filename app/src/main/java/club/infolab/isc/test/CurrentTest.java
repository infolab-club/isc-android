package club.infolab.isc.test;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class CurrentTest {
    public static ArrayList<MomentTest> results = new ArrayList<>();
    public static void appendMomentTest(String moment) {
        MomentTest momentTest = getMomentFromString(moment);
        results.add(momentTest);
    }

    public static MomentTest getMomentFromString(String moment) {
        String[] megaString = moment.split("\n");
        String[] data = megaString[0].split(",");
        float time = Float.parseFloat(data[0]);
        float vol = Float.parseFloat(data[1]);
        float amp = Float.parseFloat(data[2]);
        return new MomentTest(time, vol, amp);
    }

    public static String convertTestsToJson(ArrayList current) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String currentTestJson = gson.toJson(current);
        return currentTestJson;
    }

    public static ArrayList<MomentTest> getTestsFromFiles(Context context, int fileNumber) {
        ArrayList<MomentTest> simulation = new ArrayList<>();
        String currentFile;

        switch (fileNumber) {
            case 0:
                currentFile = "sample_input/cyclic.txt";
                break;
            case 1:
                currentFile = "sample_input/linear_sweep.txt";
                break;
            case 2:
                currentFile = "sample_input/sinusoid.txt";
                break;
            default:
                currentFile = "sample_input/constant_voltage.txt";
                break;
        }

        try {
            InputStreamReader input = new InputStreamReader(context.getAssets().open(currentFile));
            BufferedReader reader = new BufferedReader(input);
            String line = reader.readLine();
            simulation.add(getMomentFromString(line));
            while (line != null){
                simulation.add(getMomentFromString(line));
                line = reader.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return simulation;
    }
}

