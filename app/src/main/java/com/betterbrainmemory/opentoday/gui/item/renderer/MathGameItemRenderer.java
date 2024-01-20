package com.betterbrainmemory.opentoday.gui.item.renderer;

import static com.betterbrainmemory.opentoday.util.InlineUtil.viewClick;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import com.betterbrainmemory.opentoday.R;
import com.betterbrainmemory.opentoday.databinding.ItemMathGameBinding;
import com.betterbrainmemory.opentoday.fun.mathgame.MathGameItem;
import com.betterbrainmemory.opentoday.gui.interfaces.ItemInterface;
import com.betterbrainmemory.opentoday.gui.item.ItemViewGenerator;
import com.betterbrainmemory.opentoday.gui.item.ItemViewGeneratorBehavior;
import com.betterbrainmemory.opentoday.gui.item.registry.ItemRenderer;
import com.betterbrainmemory.opentoday.gui.item.registry.NameResolver;
import com.betterbrainmemory.opentoday.util.Destroyer;
import com.betterbrainmemory.opentoday.util.RandomUtil;

import org.jetbrains.annotations.NotNull;

public class MathGameItemRenderer implements NameResolver, ItemRenderer<MathGameItem> {
    @Override
    public String resolveName(Context context) {
        return context.getString(R.string.item_mathGame);
    }

    @Override
    public String resolveDescription(Context context) {
        return context.getString(R.string.item_mathGame_description);
    }

    @Override
    public View render(MathGameItem item, Activity context, LayoutInflater layoutInflater, ViewGroup parent, @NotNull ItemViewGeneratorBehavior behavior, @NotNull ItemInterface onItemClick, ItemViewGenerator itemViewGenerator, boolean previewMode, @NotNull Destroyer destroyer) {
        final ItemMathGameBinding binding = ItemMathGameBinding.inflate(layoutInflater, parent, false);

        final MathGameInterface gameInterface = new MathGameInterface() {
            private String currentNumberStr = "0";
            private int currentNumber = 0;

            public void numberPress(byte b) {
                if (b < 0 || b > 9) {
                    throw new IllegalArgumentException("OutOfRange of numberPress(0-9): " + b);
                }
                currentNumberStr += b;
                try {
                    currentNumber = Integer.parseInt(currentNumberStr);
                } catch (Exception ignored) {
                    currentNumber = RandomUtil.nextInt(); // easter egg number (if number out of int(32-bits))
                }
                currentNumberStr = String.valueOf(currentNumber);
                updateDisplay();
            }

            public void donePress() {
                final int color;
                final boolean right = item.isResultRight(currentNumber);
                if (right) {
                    color = Color.GREEN;
                    item.postResult(currentNumber);
                    binding.questText.setText(item.getQuestText());
                    binding.questText.setTextSize(item.getQuestTextSize());
                    binding.questText.setGravity(item.getQuestTextGravity());
                } else {
                    color = Color.RED;
                }

                clearCurrentInput();

                final ValueAnimator animator = ValueAnimator.ofObject(new ArgbEvaluator(), color, Color.TRANSPARENT);
                animator.setDuration(right ? 1000 : 512);
                animator.setInterpolator(right ? new DecelerateInterpolator() : new AccelerateInterpolator());
                animator.addUpdateListener(valueAnimator -> {
                    binding.userEnterNumber.setBackgroundTintList(ColorStateList.valueOf(((int) valueAnimator.getAnimatedValue())));
                });
                animator.start();
            }

            public void clearCurrentInput() {
                setCurrentInput(0);
            }

            private void setCurrentInput(int v) {
                currentNumber = v;
                currentNumberStr = String.valueOf(v);
                updateDisplay();
            }

            private void updateDisplay() {
                binding.userEnterNumber.setText(currentNumberStr);
            }

            public void invert() {
                setCurrentInput(-currentNumber);
            }

            @Override
            public void init() {
                binding.questText.setText(item.getQuestText());
                binding.questText.setTextSize(item.getQuestTextSize());
                binding.questText.setGravity(item.getQuestTextGravity());

                binding.userEnterNumber.setText(currentNumberStr);
                viewClick(binding.userEnterNumber, this::invert);

                viewClick(binding.number0, () -> numberPress((byte) 0));
                viewClick(binding.number1, () -> numberPress((byte) 1));
                viewClick(binding.number2, () -> numberPress((byte) 2));
                viewClick(binding.number3, () -> numberPress((byte) 3));
                viewClick(binding.number4, () -> numberPress((byte) 4));
                viewClick(binding.number5, () -> numberPress((byte) 5));
                viewClick(binding.number6, () -> numberPress((byte) 6));
                viewClick(binding.number7, () -> numberPress((byte) 7));
                viewClick(binding.number8, () -> numberPress((byte) 8));
                viewClick(binding.number9, () -> numberPress((byte) 9));
                viewClick(binding.numberClear, this::clearCurrentInput);
                viewClick(binding.numberNext, this::donePress);
            }
        };

        // Text
        TextItemRenderer.applyTextItemToTextView(context, item, binding.title, behavior, destroyer, previewMode);
        ItemViewGenerator.applyItemNotificationIndicator(context, item, binding.indicatorNotification, behavior, destroyer, previewMode);
        gameInterface.init();

        binding.keyboard.setEnabled(!previewMode);
        binding.userEnterNumber.setEnabled(!previewMode);
        binding.questText.setEnabled(!previewMode);
        binding.numberClear.setEnabled(!previewMode);
        binding.numberNext.setEnabled(!previewMode);
        binding.number0.setEnabled(!previewMode);
        binding.number1.setEnabled(!previewMode);
        binding.number2.setEnabled(!previewMode);
        binding.number3.setEnabled(!previewMode);
        binding.number4.setEnabled(!previewMode);
        binding.number5.setEnabled(!previewMode);
        binding.number6.setEnabled(!previewMode);
        binding.number7.setEnabled(!previewMode);
        binding.number8.setEnabled(!previewMode);
        binding.number9.setEnabled(!previewMode);


        if (behavior.isRenderMinimized(item)) {
            binding.keyboard.setVisibility(View.GONE);
            binding.userEnterNumber.setVisibility(View.GONE);
            binding.questText.setGravity(Gravity.NO_GRAVITY);
            binding.questText.setTextSize(18);
        }

        return binding.getRoot();
    }


    private interface MathGameInterface {
        void init();
    }

}
