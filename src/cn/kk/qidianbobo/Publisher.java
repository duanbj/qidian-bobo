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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

import javax.swing.JCheckBox;

public abstract class Publisher {
    private final static String SAVE_FOLDER = System.getProperty("user.home") + File.separator;
    protected final Main main;
    protected final JCheckBox cmp;
    protected String user;
    protected String pass;
    protected boolean valid;
    final BlockingDeque<String> queue = new LinkedBlockingDeque<String>();

    protected Helper helper = new Helper();

    private final PublishCommand cmd;

    public Publisher(final Main main, final JCheckBox cmp) {
        this(main, cmp, false, 12);
    }

    public Publisher(Main main, JCheckBox cmp, boolean mobile, int defaultWaitSeconds) {
        this.main = main;
        this.cmp = cmp;
        if (mobile) {
            this.helper.setMobileUserAgent();
        }
        // try {
        // Publisher.load(getName(), this.queue);
        // } catch (Exception e) {
        // e.printStackTrace();
        // System.err.println(getName() + ": error on load persisted updates: " + e.toString());
        // }
        this.cmd = new PublishCommand(this, defaultWaitSeconds);
        this.cmd.start();
    }

    public void invalidate() {
        this.valid = false;
        setStatus(Status.FAILED);
    }

    public void setStatus(Status status) {
        this.main.setStatus(this.cmp, status);
    }

    public boolean isValid() {
        return this.valid;
    }

    public boolean start(final boolean selected, final String user, final String pass) {
        this.user = user;
        this.pass = pass;
        if (selected) {
            try {
                Publisher.load(getName(), this.queue);
                Publisher.delete(getName());
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println(getName() + ": error on load persisted updates: " + e.toString());
            }
            setStatus(Status.AUTHENTICATING);
            this.valid = onLogin(user, pass);
            if (!this.valid) {
                System.out.println(getName() + ": login failed");
                invalidate();
            } else {
                System.out.println(getName() + ": login successful");
                setStatus(Status.WAITING);
            }
        } else {
            this.valid = false;
        }
        return this.valid;
    }

    protected abstract boolean onLogin(String user, String pass);

    protected abstract void beforePublish() throws Exception;

    protected abstract void onPublish(final String update) throws Exception;

    public void addUpdates(final BlockingDeque<String> updates) {
        PublisherManager.addAll(this.queue, updates, true);
    }

    public abstract String getName();

    private static boolean delete(String name) throws IOException {
        File saveFile = new File(Publisher.SAVE_FOLDER + "qidian-bobo-" + name + ".sav");
        if (saveFile.isFile()) {
            return saveFile.delete();
        } else {
            return true;
        }
    }

    private static BlockingDeque<String> load(String name, BlockingDeque<String> queue) throws IOException {
        queue.clear();
        File saveFile = new File(Publisher.SAVE_FOLDER + "qidian-bobo-" + name + ".sav");
        if (saveFile.isFile()) {
            BufferedReader reader = new BufferedReader(new FileReader(saveFile));
            String line;
            while (null != (line = reader.readLine())) {
                if (!line.isEmpty()) {
                    queue.add(line);
                }
            }
            reader.close();
        }
        return queue;
    }

    private static void save(String name, BlockingDeque<String> queue) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(Publisher.SAVE_FOLDER + "qidian-bobo-" + name
                + ".sav"));
        for (String q : queue) {
            writer.write(q);
            writer.write("\r\n");
        }
        writer.close();
    }

    public boolean stop() {
        try {
            Publisher.save(getName(), this.queue);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(getName() + ": error on save undelivered updates: " + e.toString());
        }
        return false;
    }
}
