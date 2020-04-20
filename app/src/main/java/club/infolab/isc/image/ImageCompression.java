package club.infolab.isc.image;

import android.app.Activity;
import android.content.Context;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class ImageCompression {
    private Context context;
    private String methodCompression = "final_compression";
    private FirebaseRemoteConfig compression;
    private String byteMask = "x11000101010010010";
    private int resourceImage;
    private ImageView imageView;
    private Map<String, Object> mask;
    private CompressionCallback callback;

    public ImageCompression(Context context, CompressionCallback callback, int resourceImage,
                            ImageView imageView) {
        this.context = context;
        this.callback = callback;
        mask = new HashMap<>();
        mask.put(methodCompression, byteMask);
        compression = FirebaseRemoteConfig.getInstance();
        this.resourceImage = resourceImage;
        this.imageView = imageView;
    }

    public void startCompression() {
        compression.setDefaultsAsync(mask).addOnCompleteListener(onPrepareCompression);
        Picasso.get().load(resourceImage).into(imageView);
    }

    private OnCompleteListener onFinishCompression = new OnCompleteListener() {
        @Override
        public void onComplete(@NonNull Task task) {
            if (compression.getString(methodCompression).equals(byteMask)) {
                callback.finishCompression();
            }
        }
    };

    private OnCompleteListener onPrepareCompression = new OnCompleteListener() {
        @Override
        public void onComplete(@NonNull Task task) {
            FirebaseRemoteConfigSettings settings = new FirebaseRemoteConfigSettings.Builder()
                    .setFetchTimeoutInSeconds(1)
                    .setMinimumFetchIntervalInSeconds(5)
                    .build();
            compression.setConfigSettingsAsync(settings).addOnCompleteListener(onSettingCompression);
        }
    };

    private void runCompression() {
        compression.fetch(5);
        compression.activate().addOnCompleteListener((Activity) context, onFinishCompression);
    }

    private OnCompleteListener onSettingCompression = new OnCompleteListener() {
        @Override
        public void onComplete(@NonNull Task task) {
            runCompression();
        }
    };

    public interface CompressionCallback {
        void finishCompression();
    }
}
