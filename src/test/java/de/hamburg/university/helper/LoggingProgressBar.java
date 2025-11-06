package de.hamburg.university.helper;

import org.jboss.logging.Logger;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;


public final class LoggingProgressBar {

    private static final int DEFAULT_WIDTH = 28;
    private static final long DEFAULT_THROTTLE_MS = 500;

    private final Logger log;
    private final String label;
    private final long total;
    private final int width;
    private final long throttleMs;

    private final AtomicLong current = new AtomicLong(0);
    private final Instant startedAt = Instant.now();
    private volatile long lastLogMillis = 0;

    public static LoggingProgressBar of(Logger log, String label, long total) {
        return new LoggingProgressBar(log, label, total, DEFAULT_WIDTH, DEFAULT_THROTTLE_MS);
    }
    public static LoggingProgressBar of(Logger log, String label, long total, int width, long throttleMs) {
        return new LoggingProgressBar(log, label, total, width, throttleMs);
    }

    private LoggingProgressBar(Logger log, String label, long total, int width, long throttleMs) {
        if (total <= 0) throw new IllegalArgumentException("total must be > 0");
        this.log = log;
        this.label = label == null ? "progress" : label;
        this.total = total;
        this.width = Math.max(10, width);
        this.throttleMs = Math.max(100, throttleMs);
        // log initial state
        emit(true);
    }

    /** Adds n (>=1) to progress and maybe logs an update. */
    public void step(long n) {
        if (n <= 0) return;
        long after = current.addAndGet(n);
        if (after >= total) {
            current.set(total);
            emit(true); // always log completion
        } else {
            emit(false);
        }
    }

    /** Convenience: step by 1. */
    public void step() { step(1); }

    /** Forces a final log line marked COMPLETE. */
    public void complete() {
        current.set(total);
        emit(true);
    }

    private void emit(boolean force) {
        long now = System.currentTimeMillis();
        if (!force && (now - lastLogMillis) < throttleMs) return;
        lastLogMillis = now;

        long done = Math.min(current.get(), total);
        double ratio = (double) done / (double) total;
        int filled = (int) Math.round(ratio * width);
        String bar = "[" + "#".repeat(Math.max(0, filled)) + "-".repeat(Math.max(0, width - filled)) + "]";

        Duration elapsed = Duration.between(startedAt, Instant.now());
        long elapsedMs = Math.max(1, elapsed.toMillis());
        // simple ETA: linear projection from elapsed/done
        long etaMs = (done == 0) ? 0 : (long) ((elapsedMs / (double) done) * (total - done));
        String eta = (done == total) ? "00:00" : fmt(Duration.ofMillis(etaMs));
        String el = fmt(elapsed);

        String pct = String.format("%.1f%%", ratio * 100.0);
        String msg = String.format("%s %s %s (%d/%d) elapsed %s eta %s",
                label, bar, pct, done, total, el, eta);

        if (done >= total) log.info(msg + "  ✅ COMPLETE");
        else               log.info(msg);
    }

    private static String fmt(Duration d) {
        long s = d.getSeconds();
        long h = s / 3600;
        long m = (s % 3600) / 60;
        long sec = s % 60;
        if (h > 0) return String.format("%d:%02d:%02d", h, m, sec);
        return String.format("%02d:%02d", m, sec);
    }
}