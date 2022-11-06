package com.fazziclay.opentoday.callback;

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
        private boolean isDeleteCallback;
        private boolean isChangeImportance;
        private CallbackImportance newImportance;

        public Builder setDeleteCallback(boolean deleteCallback) {
            this.isDeleteCallback = deleteCallback;
            return this;
        }

        /**
         * На что после выполнения изменить Importance этого Callback'а
         * Если null то не изменять
         * Если не вызывать то не вызывать
         * **/
        public Builder setNewImportance(CallbackImportance importance) {
            this.isChangeImportance = importance != null;
            this.newImportance = importance;
            return this;
        }

        public Status build() {
            return new Status(isDeleteCallback, isChangeImportance, newImportance);
        }
    }
}
