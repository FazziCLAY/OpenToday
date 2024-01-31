package com.betterbrainmemory.opentoday.fun.mathgame;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.betterbrainmemory.opentoday.app.data.Cherry;
import com.betterbrainmemory.opentoday.app.items.item.Item;
import com.betterbrainmemory.opentoday.app.items.item.ItemFactory;
import com.betterbrainmemory.opentoday.app.items.item.TextItem;
import com.betterbrainmemory.opentoday.app.items.item.Transform;
import com.betterbrainmemory.opentoday.app.items.tick.TickSession;
import com.betterbrainmemory.opentoday.app.items.tick.TickTarget;
import com.betterbrainmemory.opentoday.util.RandomUtil;
import com.betterbrainmemory.opentoday.util.callback.CallbackStorage;

import java.util.ArrayList;
import java.util.List;

public class MathGameItem extends TextItem {
    public static final MathGameItemCodec CODEC = new MathGameItemCodec();
    public static final ItemFactory<MathGameItem> FACTORY = new MathGameItemFactory();


    private List<PrimitiveQuest> quests = new ArrayList<>();
    private PrimitiveQuest current;
    private long currentSetAt;
    private int initalSize;
    private long time;
    private final CallbackStorage<MathGameItemCallback> callbackStorage = new CallbackStorage<>();

    public MathGameItem() {
        super();
        initQuests();
    }

    public MathGameItem(@Nullable MathGameItem copy) {
        super(copy);
        if (copy != null) {
            this.quests = new ArrayList<>(quests);
        }
        initQuests();
    }

    private void initQuests() {
        quests.clear();
        initalSize = 0;


        for (int i = 0; i < 100; i++) {
            for (int j = 0; j < 100; j++) {
                quests.add(new PrimitiveQuest(this, i, j, Operation.ADD));
                quests.add(new PrimitiveQuest(this, i, j, Operation.SUBTRACT));
                initalSize += 2;
            }
        }

        currentSetAt = System.currentTimeMillis();
        randomizeCurrent();
    }

    private void randomizeCurrent() {
        if (quests.isEmpty()) {
            initQuests();
            return;
        }
        current = RandomUtil.randomOfList(quests);

        time += (System.currentTimeMillis() - currentSetAt);

        currentSetAt = System.currentTimeMillis();
    }

    public MathGameItem(@Nullable TextItem append) {
        super(append);
    }

    @Override
    public void tick(TickSession tickSession) {
        if (!tickSession.isAllowed(this)) return;

        super.tick(tickSession);

        if (tickSession.isTickTargetAllowed(TickTarget.ITEM_MATH_GAME_UPDATE)) {
            profilerPush(tickSession, "math_game_update");

            updateStat();

            profilerPop(tickSession);
        }
    }

    @Override
    protected void updateStat() {
        super.updateStat();
        callbackStorage.run((callbackStorage, callback) -> {
            int tm = 0;
            try {
                tm = (int) (time / (initalSize - quests.size()));
            } catch (Exception e) {
                tm = -1;
            }
            return callback.mathGameStatUpdated(MathGameItem.this, String.format("%s/%s ~%sms", quests.size(), initalSize, tm));
        });
    }

    public boolean isOperationEnabled(Operation o) {
        return true;
    }

    public void setOperationEnabled(Operation o, boolean b) {

    }

    public float getQuestTextSize() {
        return 20;
    }

    public void generateQuest() {
        randomizeCurrent();
    }


    public String getQuestText() {
        return current.getText();
    }

    public boolean isResultRight(int currentNumber) {
        return current.getResult() == currentNumber;
    }

    public void postResult(int currentNumber) {
        if (isResultRight(currentNumber)) {
            quests.remove(current);
            randomizeCurrent();
        }
        updateStat();
    }

    public int getPrimitiveNumber1Min() {
        return 0;
    }

    public void setPrimitiveNumber1Min(int primitiveNumber1Min) {
    }

    public int getPrimitiveNumber1Max() {
        return 1;
    }

    public void setPrimitiveNumber1Max(int primitiveNumber1Max) {
    }

    public int getPrimitiveNumber2Min() {
        return 2;
    }

    public void setPrimitiveNumber2Min(int primitiveNumber2Min) {
    }

    public int getPrimitiveNumber2Max() {
        return 3;
    }

    public void setPrimitiveNumber2Max(int primitiveNumber2Max) {
    }

    public CallbackStorage<MathGameItemCallback> mathGameCallbacks() {
        return callbackStorage;
    }


    private static class MathGameItemCodec extends TextItemCodec {
        @NonNull
        @Override
        public Cherry exportItem(@NonNull Item item) {
            MathGameItem mathGameItem = (MathGameItem) item;
            return super.exportItem(item);
        }

        private static final MathGameItem DEFAULT_VALUES = new MathGameItem();
        @NonNull
        @Override
        public Item importItem(@NonNull Cherry cherry, Item item) {
            MathGameItem mathGameItem = fallback(item, MathGameItem::new);
            super.importItem(cherry, mathGameItem);

            return mathGameItem;
        }
    }

    private static class MathGameItemFactory implements ItemFactory<MathGameItem> {
        @Override
        public MathGameItem create() {
            return new MathGameItem();
        }

        @Override
        public MathGameItem copy(Item item) {
            return new MathGameItem((MathGameItem) item);
        }

        @Override
        public Transform.Result transform(Item from) {
            if (from instanceof TextItem textItem) {
                return Transform.Result.allow(() -> new MathGameItem(textItem));
            }
            return Transform.Result.NOT_ALLOW;
        }
    }
}
