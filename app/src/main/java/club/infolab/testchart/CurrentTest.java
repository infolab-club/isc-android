package club.infolab.testchart;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class CurrentTest {
    String currentFile;
    public static ArrayList<MomentTest> testResult = new ArrayList<>();
    public  Context context;
    public CurrentTest(Context context){
        this.context = context;
    }

    public void AppendMomentTest(String moment) {
        switch (moment) {
            case "0":
                Converter(testResult);
                break;
            default:
                String[] data = moment.split(",");
                float time = Float.parseFloat(data[0]);
                float vol = Float.parseFloat(data[1]);
                float amp = Float.parseFloat(data[2]);
                MomentTest momentTest = new MomentTest(time, vol, amp);
                testResult.add(momentTest);
                break;
        }
    }

    public void Converter(ArrayList current) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String currentTestJson = gson.toJson(current);
    }
    public void Reader(int fileNumber) {
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
            BufferedReader reader = new BufferedReader(new InputStreamReader(context.getAssets().open(currentFile)));
            String line = reader.readLine();
            AppendMomentTest(line);
            while (line != null){
                AppendMomentTest(line);
                line = reader.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        AppendMomentTest("0");
    }
}
