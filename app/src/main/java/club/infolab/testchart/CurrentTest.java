package club.infolab.testchart;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;

public class CurrentTest {
    public static ArrayList<MomentTest> testResult = new ArrayList<>();

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
}
