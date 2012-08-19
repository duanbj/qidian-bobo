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
import java.net.URLEncoder;

import javax.swing.JOptionPane;

public class TwitterPublisher extends Publisher {

    private final static String LOGIN_URL = "https://twitter.com/login";

    private final static String SESSIONS_URL = "https://twitter.com/sessions";

    private final static String SESSIONS_PATTERN = "session%%5Busername_or_email%%5D=%s&session%%5Bpassword%%5D=%s&remember_me=1&scribe_log=&redirect_after_login=&authenticity_token=%s";

    private final static String STATUS_URL = "https://twitter.com/";

    private final static String POST_URL = "https://api.twitter.com/1/statuses/update.json";

    private final static String POST_PATTERN = "include_entities=true&status=%s&post_authenticity_token=%s";

    private final static int MAX_MSG_LEN = 140;

    public static void main(final String[] args) throws Exception {
        TwitterPublisher publisher = new TwitterPublisher(new Main());
        publisher.onLogin("email", "pass");
        publisher.beforePublish();
        publisher.onPublish("ni hao!");
    }

    private String postToken;

    public TwitterPublisher(final Main main) {
        super(main, main.chkTwitter);
    }

    @Override
    public boolean onLogin(final String user, final String pass) {
        String authToken = null;
        String postToken = this.postToken;
        try {
            // System.out.println(getName() + ": user=" + user + ", pass=" + pass);
            final String encUser = URLEncoder.encode(user, "UTF-8");
            final String encPass = URLEncoder.encode(pass, "UTF-8");
            BufferedReader reader = new BufferedReader(new InputStreamReader(new BufferedInputStream(
                    this.helper.openUrlInputStream(TwitterPublisher.LOGIN_URL)), "UTF-8"));
            String line;
            while (null != (line = reader.readLine())) {
                if (line.contains(" name=\"authenticity_token\"")) {
                    authToken = Helper.substringBetween(line, " value=\"", "\" ");
                    break;
                }
            }
            reader.close();
            Thread.sleep(600);

            if (authToken != null) {
                final String postData = String.format(TwitterPublisher.SESSIONS_PATTERN, encUser, encPass, authToken);
                reader = new BufferedReader(new InputStreamReader(new BufferedInputStream(
                        this.helper.openUrlInputStream(TwitterPublisher.SESSIONS_URL, true, postData)), "UTF-8"));
                while (null != (line = reader.readLine())) {
                }
                reader.close();

                System.out.println(getName() + ": location=" + this.helper.lastLocation);
                Thread.sleep(1000);

                reader = new BufferedReader(new InputStreamReader(new BufferedInputStream(
                        this.helper.openUrlInputStream(TwitterPublisher.STATUS_URL)), "UTF-8"));
                while (null != (line = reader.readLine())) {
                    if (line.contains("\"postAuthenticityToken\":")) {
                        postToken = Helper.substringBetween(line, "\"postAuthenticityToken\":\"", "\",");
                        break;
                    }
                }
                reader.close();
            } else {
                System.out.println(getName() + ": auth token missing. still logged in?");
            }
        } catch (final Exception e) {
            System.err.println(getName() + ": login failed with error: " + e.toString());
        }
        if (authToken != null) {
            System.out.println(getName() + ": auth-token=" + authToken);
            System.out.println(getName() + ": post-token=" + postToken);
            if ((authToken != null) && (postToken != null)) {
                this.postToken = postToken;
                return true;
            }
            Main.EXECUTOR_SERVICE.execute(new Runnable() {
                @Override
                public void run() {
                    if (TwitterPublisher.this.helper.lastLocation.contains("/captcha")) {
                        JOptionPane.showMessageDialog(TwitterPublisher.this.main, "Twitter账号被锁！请在twitter上人工登陆解锁！",
                                "Twitter", JOptionPane.ERROR_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(TwitterPublisher.this.main, "Twitter登录错误！请确定用户名和密码是否正确！",
                                "Twitter", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
        }
        return false;
    }

    @Override
    public void onPublish(final String update) throws Exception {
        try {
            System.out.println(getName() + ": publish=" + update);
            final String encUpdate = URLEncoder.encode(Helper.cut(update, TwitterPublisher.MAX_MSG_LEN), "UTF-8");
            final String postData = String.format(TwitterPublisher.POST_PATTERN, encUpdate, this.postToken);

            BufferedReader reader = new BufferedReader(new InputStreamReader(new BufferedInputStream(
                    this.helper.openUrlInputStream(TwitterPublisher.POST_URL, true, postData)), "UTF-8"));
            System.out.println(getName() + ": sent=" + update);
            String line;
            while (null != (line = reader.readLine())) {
                // read full
                // System.out.println(line);
            }
            reader.close();
            System.out.println(getName() + ": published=" + update);
        } catch (final IOException e) {
            if (e.toString().contains("400") || e.toString().contains("403")) {
                // rate limit or duplicate message
                throw new RuntimeException(getName() + ": rate limit or duplicate message", e);
            } else {
                throw e;
            }
        }
    }

    @Override
    protected void beforePublish() throws Exception {
        if (isValid() && (this.postToken != null)) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new BufferedInputStream(
                    this.helper.openUrlInputStream(TwitterPublisher.STATUS_URL))));
            String line;
            while (null != (line = reader.readLine())) {
                if (line.contains("\"postAuthenticityToken\":")) {
                    this.postToken = Helper.substringBetween(line, "\"postAuthenticityToken\":\"", "\",");
                    break;
                }
            }
            reader.close();
            if (this.postToken == null) {
                throw new RuntimeException(getName() + ": session timed out or login failure");
            } else {
                System.out.println(getName() + ": post-token=" + this.postToken);
                this.helper.putConnectionHeader("Accept", "application/json, text/javascript, */*; q=0.01");
                this.helper.putConnectionHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
                this.helper.putConnectionHeader("X-Requested-With", "XMLHttpRequest");
                this.helper.putConnectionHeader("X-PHX", "true");
                this.helper.putConnectionHeader("Accept-Language", "en");
                this.helper.putConnectionHeader("DNT", "1");
                this.helper.putConnectionHeader("Referer", "https://api.twitter.com/receiver.html");
            }
        }
    }

    @Override
    public String getName() {
        return "twitter";
    }
}
