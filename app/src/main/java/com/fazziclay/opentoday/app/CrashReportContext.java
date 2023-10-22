package com.fazziclay.opentoday.app;

import com.fazziclay.opentoday.util.time.TimeUtil;

import java.util.Stack;

public class CrashReportContext {
    public static final Front FRONT = new Front();
    public static final Back BACK = new Back();

    static {
        FRONT.push("front push in static CrashReportContext");
        BACK.push("back push in static CrashReportContext");
    }
    private static String mainActivityStatus = "NON-CREATED";
    private static String mainRootFragment = "NON-CREATED";

    public static void mainActivityCreate() {
        mainActivityStatus = "Created";
    }
    
    public static void mainActivityDestroy() {
        mainActivityStatus = "Destroyed";
    }

    public static void mainActivityPause() {
        mainActivityStatus = "Paused";
    }

    public static void setMainRootFragment(String mainRootFragment) {
        CrashReportContext.mainRootFragment = mainRootFragment;
    }

    public static String getAsString() {
        StringBuilder builder = new StringBuilder(String.format("""
                == CrashReportContext ==
                mainActivityStatus=%s
                mainRootFragment=%s
                """, mainActivityStatus,
                mainRootFragment));

        builder.append("\nFRONT: ");
        for (String s : FRONT.stack) {
            builder.append("(").append(s).append(")").append(", ");
        }
        builder.delete(builder.lastIndexOf(","), builder.lastIndexOf(" "));

        builder.append("\nBACK: ");
        for (String s : BACK.stack) {
            builder.append("(").append(s).append(")").append(", ");
        }
        builder.delete(builder.lastIndexOf(","), builder.lastIndexOf(" "));

        return builder.toString();
    }

    public static class Front {
        private final Stack<String> stack = new Stack<>();

        public void push(String s) {
            stack.push(TimeUtil.getDebugDate() + " " + Thread.currentThread().getName() + " " + s);
        }

        public void pop() {
            stack.pop();
        }
    }

    public static class Back {
        private final Stack<String> stack = new Stack<>();

        public void push(String s) {
            stack.push(TimeUtil.getDebugDate() + " " + Thread.currentThread().getName() + " " + s);
        }

        public void pop() {
            stack.pop();
        }

        public void swap(String s) {
            pop();
            push(s);
        }
    }
}
