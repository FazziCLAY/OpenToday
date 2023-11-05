package com.fazziclay.opentoday.gui.activity;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.fazziclay.opentoday.app.App;
import com.fazziclay.opentoday.app.settings.enums.ItemAction;
import com.fazziclay.opentoday.app.items.ItemsRoot;
import com.fazziclay.opentoday.app.items.item.CycleListItem;
import com.fazziclay.opentoday.app.items.item.FilterGroupItem;
import com.fazziclay.opentoday.app.items.item.GroupItem;
import com.fazziclay.opentoday.app.items.item.Item;
import com.fazziclay.opentoday.databinding.ActivityAlarmBinding;
import com.fazziclay.opentoday.gui.interfaces.ItemInterface;
import com.fazziclay.opentoday.gui.item.Destroyer;
import com.fazziclay.opentoday.gui.item.ItemViewGenerator;
import com.fazziclay.opentoday.gui.item.ItemViewGeneratorBehavior;
import com.fazziclay.opentoday.gui.item.ItemsStorageDrawerBehavior;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

public class AlarmActivity extends AppCompatActivity {

    private App app;
    private ActivityAlarmBinding binding;
    private int cancelNotifyId;
    private MediaPlayer mediaPlayer;
    private final Destroyer holderDestroy = new Destroyer();

    @NotNull
    public static Intent createIntent(@NotNull Context context, @Nullable UUID previewItem, boolean isPreviewMode, @NotNull String title, boolean sound, int cancelNotifyId) {
        Intent intent = new Intent(context, AlarmActivity.class)
                .putExtra("previewItemId", Objects.toString(previewItem, null))
                .putExtra("previewItemIsPreviewMode", isPreviewMode)
                .putExtra("title", title)
                .putExtra("sound", sound)
                .putExtra("cancelNotifyId", cancelNotifyId);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setTurnScreenOn(true);
            setShowWhenLocked(true);
        }

        String previewItemStr = getIntent().getStringExtra("previewItemId");
        boolean previewMode = getIntent().getBooleanExtra("previewItemIsPreviewMode", true);
        String title = getIntent().getStringExtra("title");
        boolean sound = getIntent().getBooleanExtra("sound", true);

        cancelNotifyId = getIntent().getIntExtra("cancelNotifyId", 0);

        app = App.get(this);
        binding = ActivityAlarmBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.title.setText(title);
        binding.okButton.setOnClickListener(view -> {
            getSystemService(NotificationManager.class).cancel(cancelNotifyId);
            finish();
        });

        if (previewItemStr != null) {
            UUID id = UUID.fromString(previewItemStr);
            setupPreviewItem(id, previewMode);
        }

        if (sound) {
            setVolumeControlStream(AudioManager.STREAM_ALARM);
            try {
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setDataSource(this, Settings.System.DEFAULT_ALARM_ALERT_URI);
                mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build());
                mediaPlayer.setVolume(1.0f, 1.0f);
                mediaPlayer.setLooping(true);
                mediaPlayer.prepare();
                mediaPlayer.start();
            } catch (IOException e) {
                App.exception(this, e); // no crash
            }
        }

        new Handler(Looper.getMainLooper()).postDelayed(this::finish, 1000*60*2);
    }

    private void setupPreviewItem(UUID id, boolean previewMode) {
        ItemsRoot itemsRoot = app.getItemsRoot();
        Item item = itemsRoot.getItemById(id);
        ItemViewGenerator itemViewGenerator = new ItemViewGenerator(this, previewMode);
        ItemViewGeneratorBehavior behavior = new ItemViewGeneratorBehavior() {
            @Override
            public boolean isConfirmFastChanges() {
                return false;
            }

            @Override
            public void setConfirmFastChanges(boolean b) {

            }

            @Override
            public Drawable getForeground(Item item) {
                return null;
            }

            @Override
            public void onGroupEdit(GroupItem groupItem) {

            }

            @Override
            public void onCycleListEdit(CycleListItem cycleListItem) {

            }

            @Override
            public void onFilterGroupEdit(FilterGroupItem filterGroupItem) {

            }

            @Override
            public ItemsStorageDrawerBehavior getItemsStorageDrawerBehavior(Item item) {
                return new ItemsStorageDrawerBehavior() {
                    @Override
                    public ItemAction getItemOnClickAction() {
                        return ItemAction.OPEN_EDITOR;
                    }

                    @Override
                    public boolean isScrollToAddedItem() {
                        return false;
                    }

                    @Override
                    public ItemAction getItemOnLeftAction() {
                        return ItemAction.OPEN_EDITOR;
                    }

                    @Override
                    public void onItemOpenEditor(Item item) {

                    }

                    @Override
                    public void onItemOpenTextEditor(Item item) {

                    }

                    @Override
                    public boolean ignoreFilterGroup() {
                        return false;
                    }

                    @Override
                    public void onItemDeleteRequest(Item item) {

                    }
                };
            }

            @Override
            public boolean isRenderMinimized(Item item) {
                return item.isMinimize();
            }

            @Override
            public boolean isRenderNotificationIndicator(Item item) {
                return item.isNotifications();
            }
        };
        ItemInterface onItemClick = new ItemInterface() {
            @Override
            public void run(@Nullable Item item) {
                // do nothing
            }
        };
        binding.preview.addView(itemViewGenerator.generate(item, binding.preview, behavior, holderDestroy, onItemClick));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
        holderDestroy.destroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mediaPlayer != null) {
            mediaPlayer.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mediaPlayer != null) {
            mediaPlayer.start();
        }
    }
}
