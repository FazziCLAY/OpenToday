package com.fazziclay.opentoday.util.profiler;

public interface Profiler {
    Profiler EMPTY = new Profiler() {
        @Override
        public void push(String s) {}

        @Override
        public void pop() {}

        @Override
        public void pop2() {}

        @Override
        public void end() {}

        @Override
        public void swap(String s) {}

        @Override
        public void instant(String s) {}

        @Override
        public String getResult(int depth) {
            return "EMPTY_PROFILER";
        }
    };

    void push(String s);

    void pop();

    void pop2();

    void end();

    void swap(String s);

    void instant(String s);

    String getResult(int depth);
}
