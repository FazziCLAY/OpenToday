package com.fazziclay.opentoday.util.callback;

/**
 * callback run status
 * @see CallbackStorage
 * **/
public class Status {
    public static final Status NONE = new Status.Builder().build();

    private final boolean isDeleteCallback;
    private final boolean isChangeImportance;
    private final CallbackImportance newImportance;

    private Status(boolean isDeleteCallback, boolean isChangeImportance, CallbackImportance newImportance) {
        this.isDeleteCallback = isDeleteCallback;
        this.isChangeImportance = isChangeImportance;
        this.newImportance = newImportance;
    }

    public boolean isDeleteCallback() {
        return isDeleteCallback;
    }

    public boolean isChangeImportance() {
        return isChangeImportance;
    }

    public CallbackImportance getNewImportance() {
        return newImportance;
    }

    public static class Builder {
        private boolean isRemoveCallback;
        private boolean isChangeImportance;
        private CallbackImportance newImportance;

        public Builder setRemoveCallback(boolean b) {
            this.isRemoveCallback = b;
            return this;
        }

        public Builder setNewImportance(CallbackImportance importance) {
            this.isChangeImportance = importance != null;
            this.newImportance = importance;
            return this;
        }

        public Status build() {
            return new Status(isRemoveCallback, isChangeImportance, newImportance);
        }
    }
}
