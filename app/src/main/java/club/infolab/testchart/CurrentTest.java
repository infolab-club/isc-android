package club.infolab.testchart;

import java.util.ArrayList;

public class CurrentTest {
    public static ArrayList<MomentTest> testResult = new ArrayList<>();
    private static String sampleInput = "0.01000, -0.9950, -71.69\n" +
            "0.02000, -0.9900, -63.73\n" +
            "0.03000, -0.9850, -60.10\n" +
            "0.04000, -0.9800, -57.21\n";

    public void AppendMomentTest(String moment) {
        String[] data = moment.split(",");
        float time = Float.parseFloat(data[0]);
        float vol = Float.parseFloat(data[1]);
        float amp = Float.parseFloat(data[2]);
        MomentTest momentTest = new MomentTest(time, vol, amp);
        testResult.add(momentTest);
    }

    public void ParseSampleInput() {
        String[] interim = sampleInput.split("\n");

        for (String s : interim) {
            String[] data = s.split(",");
            float time = Float.parseFloat(data[0]);
            float vol = Float.parseFloat(data[1]);
            float amp = Float.parseFloat(data[2]);
            MomentTest momentTest = new MomentTest(time, vol, amp);
            testResult.add(momentTest);
        }
    }
}
