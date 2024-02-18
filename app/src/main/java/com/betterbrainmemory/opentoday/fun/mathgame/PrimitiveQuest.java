package com.betterbrainmemory.opentoday.fun.mathgame;

class PrimitiveQuest {
    private final Operation operation;
    private final int val1;
    private final int val2;
    private final int result;

    public PrimitiveQuest(MathGameItem parent, int i, int j, Operation operation) {
        this.operation = operation;
        val1 = i;
        val2 = j;
        result = operation.apply(val1, val2);
    }

    public String getText() {
        return operation.forNumbers(val1, val2);
    }

    public int getResult() {
        return result;
    }
}
