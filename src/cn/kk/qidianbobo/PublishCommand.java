package cn.kk.qidianbobo;

import java.util.concurrent.TimeUnit;

public class PublishCommand implements Runnable {
    private final Publisher publisher;
    private int failureCounter;
    private int fatalCounter;
    private final int defaultWaitSeconds;
    private boolean retry;

    public PublishCommand(Publisher publisher, int defaultWaitTime) {
        this.publisher = publisher;
        this.failureCounter = 0;
        this.defaultWaitSeconds = defaultWaitTime;
    }

    @Override
    public void run() {
        int waitSeconds = this.defaultWaitSeconds;
        try {
            this.publisher.setStatus(Status.PREPARING);
            if (this.retry) {
                retry();
            }
            if (this.publisher.isValid() && !this.publisher.queue.isEmpty()) {
                try {
                    final String update = this.publisher.queue.pollFirst();
                    if (update != null) {
                        this.publisher.setStatus(Status.PUBLISHING);
                        this.publisher.beforePublish();
                        this.publisher.onPublish(update);
                    }
                } catch (final RuntimeException e) {
                    e.printStackTrace();
                    System.out.println(this.publisher.getName() + ": failed to publish: " + e.toString());
                    this.failureCounter++;
                    if (this.failureCounter > 20) {
                        this.failureCounter = 0;
                        this.retry = true;
                        waitSeconds = this.defaultWaitSeconds * 10;
                    } else {
                        waitSeconds = this.defaultWaitSeconds * 3;
                    }
                } catch (Throwable t) {
                    t.printStackTrace();
                    System.err.println(this.publisher.getName() + ": error on publish: " + t.toString());
                    this.retry = true;
                    waitSeconds = this.defaultWaitSeconds * 20;
                }
            }
        } finally {
            this.publisher.setStatus(Status.WAITING);
            Main.EXECUTOR_SERVICE.schedule(this, waitSeconds, TimeUnit.SECONDS);
        }
    }

    private void retry() {
        this.retry = false;
        if (this.fatalCounter++ > 3) {
            this.publisher.invalidate();
        } else {
            this.publisher.helper.resetConnectionHeaders();
            this.publisher.onLogin(this.publisher.user, this.publisher.pass);
        }
    }

    public void reset() {
        this.failureCounter = 0;
        this.fatalCounter = 0;
    }

    public void start() {
        Main.EXECUTOR_SERVICE.schedule(this, 1, TimeUnit.MINUTES);
    }

}
