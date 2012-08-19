/*  Copyright (c) 2012 Xiaoyun Zhu
 * 
 *  Permission is hereby granted, free of charge, to any person obtaining a copy  
 *  of this software and associated documentation files (the "Software"), to deal  
 *  in the Software without restriction, including without limitation the rights  
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell  
 *  copies of the Software, and to permit persons to whom the Software is  
 *  furnished to do so, subject to the following conditions:
 *  
 *  The above copyright notice and this permission notice shall be included in  
 *  all copies or substantial portions of the Software.
 *  
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR  
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,  
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE  
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER  
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,  
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN  
 *  THE SOFTWARE.  
 */
package cn.kk.qidianbobo;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.JComponent;
import javax.swing.JOptionPane;

public class PublisherManager {
    private static final int CHECK_TIME_SECONDS = 60;
    private final Publisher pubTwitter;
    private final Publisher pubQQ;
    private final Publisher pubSina;
    private final Publisher pubSohu;
    private boolean running;
    private final Main main;
    private final JComponent cmp;
    private final ReentrantLock lock;
    private boolean checkUpdateFinished;

    private static final List<String> NO_UPDATES = Collections.emptyList();

    private long lastUpdateTime = -1;

    private String lastChapterName = null;

    private static final String URL_QIDIAN_LIST = "http://all.qidian.com/Book/BookStore.aspx?ChannelId=-1&SubCategoryId=-1&Tag=all&Size=-1&Action=-1&OrderId=6&P=all&PageIndex=1&update=4&PageIndex=";

    private final Helper helper = new Helper();
    private static final String UPDATE_SEP = " class=\"swa\"";
    private static final String UPDATE_BOOKID_PREFIX = "href=\"/Book/";

    public static void main(final String[] args) throws IOException, InterruptedException {
        PublisherManager manager = new PublisherManager(new Main());
        while (true) {
            final BlockingDeque<String> updates = manager.check();
            for (final String u : updates) {
                System.out.println(u);
            }
            Thread.sleep(10000);
        }
    }

    public PublisherManager(final Main main) {
        this.main = main;
        this.cmp = main.btnStart;
        this.pubTwitter = new TwitterPublisher(main);
        this.pubQQ = new QQPublisher(main);
        this.pubSina = new SinaPublisher(main);
        this.pubSohu = new SohuPublisher(main);
        this.lock = new ReentrantLock();
    }

    public void cancel() {
        this.running = false;
        this.lock.lock();
        this.lock.unlock();
    }

    private long tmpUpdateTime = -1;
    private String tmpChapterName = null;

    private boolean check(final BlockingDeque<String> queue, final BlockingDeque<String> names, final int pageIdx) {
        System.out.println("manager: check for updates (" + new Date() + ")");
        try {
            boolean finished = false;
            BlockingDeque<String> updates = new LinkedBlockingDeque<String>();
            final BufferedReader reader = new BufferedReader(new InputStreamReader(new BufferedInputStream(
                    this.helper.openUrlInputStream(PublisherManager.URL_QIDIAN_LIST + pageIdx + "&TS="
                            + System.currentTimeMillis())), "UTF-8"));
            String line;
            String bookId = "";
            String bookName = "";
            String categoryName = "";
            String chapterName = "";
            long updateTime = -1;
            while (null != (line = reader.readLine())) {
                if (line.contains(PublisherManager.UPDATE_SEP)) {
                    final String[] tokens = line.split(PublisherManager.UPDATE_SEP);

                    for (int i = tokens.length - 1; i >= 0; i--) {
                        final String t = tokens[i];
                        if (t.contains(PublisherManager.UPDATE_BOOKID_PREFIX)) {
                            categoryName = Helper.substringBetween(t, "class=\"hui2\">", "</a>");
                            bookId = Helper.substringBetween(t, PublisherManager.UPDATE_BOOKID_PREFIX, ".aspx\"");
                            bookName = Helper.substringBetween(t, "target=\"_blank\">", "</a>");
                            chapterName = Helper.substringBetween(t, "\"_blank\" class=\"hui2\">", "</a>");
                            final String updateTimeString = Helper.substringBetween(t, "<div class=\"swe\">", "</div>");
                            try {
                                updateTime = Long.parseLong(updateTimeString.replaceAll("[ \\-:]", ""));
                            } catch (final Exception e) {
                                // silent
                            }
                            if (Helper.isNotEmptyOrNull(bookId) && Helper.isNotEmptyOrNull(bookName)
                                    && Helper.isNotEmptyOrNull(chapterName) && (-1 != updateTime)) {
                                // System.out.println(pageIdx + ": " + bookId + ": " + bookName + ", " + chapterName
                                // + ", " + updateTime + ", " + categoryName + " // " + this.lastUpdateTime);
                                if (this.lastUpdateTime == -1) {
                                    this.lastUpdateTime = updateTime;
                                    this.lastChapterName = chapterName;
                                    this.tmpUpdateTime = updateTime;
                                    this.tmpChapterName = chapterName;
                                    finished = true;
                                    break;
                                } else if ((this.tmpUpdateTime == -1) || (updateTime > this.tmpUpdateTime)) {
                                    this.tmpUpdateTime = updateTime;
                                    this.tmpChapterName = chapterName;
                                }
                                if ((this.lastUpdateTime < updateTime)
                                        || ((this.lastUpdateTime == updateTime) && !chapterName
                                                .equals(this.lastChapterName))) {
                                    if ((names.isEmpty() && this.main.isCategorySelected(categoryName))
                                            || (names.contains(bookId) || names.contains(bookName))) {
                                        // TODO get full chapter name
                                        String updateText = "『" + bookName + "』 " + chapterName + " （"
                                                + updateTimeString + "）";

                                        checkAndAdd(updates, updateText);
                                    }
                                } else {
                                    if (this.tmpUpdateTime != -1) {
                                        this.lastUpdateTime = this.tmpUpdateTime;
                                        this.lastChapterName = this.tmpChapterName;
                                    }
                                    finished = true;
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            reader.close();
            if (!updates.isEmpty()) {
                PublisherManager.addAll(queue, updates, false);
                return finished;
            }
        } catch (final Exception e) {
            System.err.println("manager: failed to get update list, " + e);
            // e.printStackTrace();
        }

        return true;
    }

    private final LinkedList<String> updated = new LinkedList<String>();

    private void checkAndAdd(BlockingDeque<String> updates, String updateText) {
        if (!this.updated.contains(updateText)) {
            System.out.println("> " + updateText);
            updates.addLast(updateText);
            this.updated.addLast(updateText);
            if (this.updated.size() > 100) {
                this.updated.removeFirst();
            }
        }
    }

    private boolean checkPublishers() {
        boolean valid = false;

        valid |= this.pubTwitter.isValid();
        valid |= this.pubQQ.isValid();
        valid |= this.pubSina.isValid();
        valid |= this.pubSohu.isValid();

        return valid;
    }

    public String getLastChapterName() {
        return Helper.chopNull(this.lastChapterName);
    }

    public long getLastUpdateTime() {
        return this.lastUpdateTime;
    }

    public void setLastChapterName(final String val) {
        this.lastChapterName = val;
    }

    public void setLastUpdateTime(final long time) {
        this.lastUpdateTime = time;
    }

    public void start() {
        if (this.lock.tryLock()) {
            int sleepCounter = 0;
            try {
                this.running = true;
                final String user = this.main.tfUser.getText();
                final String pass = new String(this.main.tfPass.getPassword());

                this.main.setStatus(this.cmp, Status.AUTHENTICATING);
                authenticate(user, pass);

                while (this.running) {
                    if (checkPublishers()) {
                        if (sleepCounter == 0) {
                            sleepCounter++;
                            this.main.setStatus(this.cmp, Status.PREPARING);
                            checkAndPublish();
                        } else {
                            this.main.setStatus(this.cmp, Status.WAITING);
                            sleepCounter++;
                            if (sleepCounter > PublisherManager.CHECK_TIME_SECONDS) {
                                sleepCounter = 0;
                            }
                        }
                        try {
                            TimeUnit.SECONDS.sleep(1);
                        } catch (final InterruptedException e) {
                            // silent
                        }
                    } else {
                        this.main.setStatus(this.cmp, Status.FAILED);
                        System.err.println("用户信息错误！请检查您的用户名及密码！");
                        JOptionPane.showMessageDialog(this.main, "用户信息错误！请检查您的用户名及密码！", "设置错误",
                                JOptionPane.ERROR_MESSAGE);
                        this.running = false;
                    }
                }
            } finally {
                stopPublishers();
                this.lock.unlock();
                if (!checkPublishers()) {
                    this.main.cancel();
                }
            }
        } else {
            this.main.cancel();
        }
    }

    private boolean stopPublishers() {
        boolean valid = true;

        valid &= this.pubTwitter.stop();
        valid &= this.pubQQ.stop();
        valid &= this.pubSina.stop();
        valid &= this.pubSohu.stop();

        return valid;
    }

    private void authenticate(final String user, final String pass) {
        if (this.running) {
            this.pubTwitter.start(this.main.chkTwitter.isSelected(), user, pass);
        }
        if (this.running) {
            this.pubQQ.start(this.main.chkQQ.isSelected(), user, pass);
        }
        if (this.running) {
            this.pubSina.start(this.main.chkSina.isSelected(), user, pass);
        }
        if (this.running) {
            this.pubSohu.start(this.main.chkSohu.isSelected(), user, pass);
        }
    }

    private void checkAndPublish() {
        this.checkUpdateFinished = false;
        try {
            final BlockingDeque<String> updates = check();
            this.main.setStatus(this.cmp, Status.PUBLISHING);
            System.out.println("manager: updates=" + updates.size());
            this.pubTwitter.addUpdates(updates);
            this.pubQQ.addUpdates(updates);
            this.pubSina.addUpdates(updates);
            this.pubSohu.addUpdates(updates);
            this.main.save();
        } finally {
            this.checkUpdateFinished = true;
        }

    }

    private synchronized BlockingDeque<String> check() {
        final BlockingDeque<String> updates = new LinkedBlockingDeque<String>();
        int pageIdx = 1;
        this.tmpUpdateTime = -1;
        while (!check(updates, this.main.getNames(), pageIdx)) {
            pageIdx++;
        }
        return updates;
    }

    static final void addAll(BlockingDeque<String> queue, BlockingDeque<String> updates, boolean first) {
        for (String u : updates) {
            if (!queue.contains(u)) {
                if (first) {
                    queue.addFirst(u);
                } else {
                    queue.addLast(u);
                }
            }
        }
    }

}
