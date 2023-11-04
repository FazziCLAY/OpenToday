package com.fazziclay.opentoday.util.profiler;

import android.util.ArraySet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

// unused: delete?
public class ProfilerImpl implements Profiler {
    public static ArrayList<Profiler> PROFILERS;

    private final String profilerName;
    private final NodeImpl rootNode;
    private NodeImpl currentNode;

    public ProfilerImpl(String profilerName) {
        this.profilerName = profilerName;
        rootNode = NodeImpl.createRootNode(profilerName);
        currentNode = rootNode;

        rootNode.start();
    }

    public void push(NodeImpl node) {
        node.parent = currentNode;
        List<NodeImpl> list;
        if (currentNode.children.containsKey(node.name)) {
            list = currentNode.children.get(node.name);
        } else {
            list = new ArrayList<>();
            currentNode.children.put(node.name, list);
        }
        if (list == null) {
            list = new ArrayList<>();
            currentNode.children.put(node.name, list);
        }
        list.add(node);
        currentNode = node;
        node.start();
    }

    @Override
    public void push(String s) {
        push(new NodeImpl(null, s));
    }

    @Override
    public void swap(String s) {
        pop();
        push(s);
    }

    @Override
    public void instant(String s) {
        push(s);
        pop();
    }

    @Override
    public void pop() {
        currentNode.end();
        currentNode = currentNode.parent;
    }

    @Override
    public void pop2() {
        pop();
        pop();
    }

    @Override
    public void end() {
        rootNode.end();
    }

    @Override
    public String getResult(int depth) {
        return "==== Profiler Results (" + profilerName + ") ====\n" + _result(0, depth, "", rootNode.name, Collections.singletonList(rootNode)).text;
    }

    @Nullable
    private Result _result(int currDepth, int maxDepth, String spaces, String name, List<NodeImpl> nodes) {
        if (currDepth > maxDepth && maxDepth > 0) {
            return null;
        }
        final Set<String> allKeys = new ArraySet<>();
        long sumDur = 0;
        long avgDur = 0;
        long maxDur = Long.MIN_VALUE;
        long minDur = Long.MAX_VALUE;
        for (final NodeImpl node : nodes) {
            long dur = node.getDuration();
            sumDur += dur;
            if (minDur > dur) minDur = dur;
            if (maxDur < dur) maxDur = dur;
            allKeys.addAll(node.children.keySet());
        }
        if (nodes.size() > 0) avgDur = sumDur / nodes.size();

        String text = (avgDur == -1) ? (spaces + "[" + name + "]") : String.format("%s[%s] %s%sms %s%s", spaces, name, (nodes.size() == 1 ? "=" : "~"), avgDur, (nodes.size() == 1 ? "" : String.format("(max=%sms min=%sms) ", maxDur, minDur)), (nodes.size() == 1 ? "" : nodes.size() + " nodes"));
        StringBuilder s = new StringBuilder(text);

        List<Result> childResults = new ArrayList<>();
        for (String childKey : allKeys) {
            List<NodeImpl> child = new ArrayList<>();
            for (NodeImpl node : nodes) {
                if (node.children.containsKey(childKey)) {
                    child.addAll(node.children.get(childKey));
                }
            }

            Result recursive = _result(currDepth + 1, maxDepth, spaces + " | ", childKey, child);
            if (recursive != null) {
                childResults.add(recursive);
            }
        }

        Result[] results = childResults.toArray(new Result[0]);
        Arrays.sort(results, (result, t1) -> {
            return Math.toIntExact(t1.dur - result.dur);
        });

        for (Result result : results) {
            s.append("\n").append(result.text);
        }

        Result r = new Result();
        r.text = s.toString();
        r.dur = avgDur;
        return r;
    }

    private static class Result {
        String text;
        long dur;
    }

    public static class NodeImpl {
        @Nullable
        NodeImpl parent;
        @NotNull
        String name;
        @NotNull
        final HashMap<String, List<NodeImpl>> children = new HashMap<>();
        long startTime;
        long endTime;


        public NodeImpl(@Nullable NodeImpl parent, @NonNull String name) {
            this.parent = parent;
            this.name = name;
        }

        public void start() {
            if (this.startTime > 0) return;
            this.startTime = System.currentTimeMillis();
        }

        public void end() {
            if (this.endTime > 0) return;
            this.endTime = System.currentTimeMillis();
        }

        public static NodeImpl createRootNode(String root) {
            return new NodeImpl(null, "root:"+root);
        }

        public long getDuration() {
            if (endTime == 0) return -1;
            return endTime - startTime;
        }
    }
}
