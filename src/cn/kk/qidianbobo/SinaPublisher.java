/*  Copyright (c) 2012 Xiaoion is hereby granted, free of charge, to any person obtaining a copy  
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

public class SinaPublisher extends Publisher {
    private final static String LOGIN_URL = "http://3g.sina.com.cn/prog/wapsite/sso/login.php?ns=1&revalid=2&backURL=http%3A%2F%2Fweibo.cn%2F&backTitle=%D0%C2%C0%CB%CE%A2%B2%A9&vt=";

    private final static String SESSIONS_URL = "http://3g.sina.com.cn/prog/wapsite/sso/login_submit.php?rand="
            + (long) (Math.random() * 10000000000L)
            + "&backURL=http%3A%2F%2Fweibo.cn%2F&backTitle=%D0%C2%C0%CB%CE%A2%B2%A9&vt=4&revalid=2&ns=1";

    private final static String SESSIONS_PATTERN = "mobile=%s&password_%s=%s&remember=on&backURL=http%%253A%%252F%%252Fweibo.cn%%252F&backTitle=%%E6%%96%%B0%%E6%%B5%%AA%%E5%%BE%%AE%%E5%%8D%%9A&vk=%s&submit=%%E7%%99%%BB%%E5%%BD%%95";

    private final static String STATUS_URL = "http://weibo.cn/";

    private final static String POST_URL = "http://weibo.cn/mblog/sendmblog?st=";

    private String postKey = "0b8b";

    private final static String POST_PATTERN = "rl=0&content=%s";

    private final static int MAX_MSG_LEN = 140;

    public static void main(final String[] args) throws Exception {
        SinaPublisher publisher = new SinaPublisher(new Main());
        publisher.onLogin("user", "pass");
        publisher.beforePublish();
        publisher.onPublish("ni hao!");
    }

    public SinaPublisher(final Main main) {
        super(main, main.chkSina, false, 7);
    }

    @Override
    protected boolean onLogin(final String user, final String pass) {
        boolean success = false;
        try {
            // System.out.println(getName()+": user=" + user + ", pass=" + pass);
            final String encUser = URLEncoder.encode(user, "UTF-8");
            final String encPass = URLEncoder.encode(pass, "UTF-8");

            String vk = null;
            String passKey = null;
            BufferedReader reader = new BufferedReader(new InputStreamReader(new BufferedInputStream(
                    this.helper.openUrlInputStream(SinaPublisher.LOGIN_URL)), "UTF-8"));
            String line;
            while (null != (line = reader.readLine())) {
                // ignore me
                if (line.contains("name=\"vk\"")) {
                    vk = Helper.substringBetween(line, "name=\"vk\" value=\"", "\"");
                }
                if (line.contains("name=\"password_")) {
                    passKey = Helper.substringBetween(line, "name=\"password_", "\"");
                }
            }
            reader.close();

            if ((vk != null) && (passKey != null)) {
                Thread.sleep(600);
                final String postData = String.format(SinaPublisher.SESSIONS_PATTERN, encUser, passKey, encPass, vk);
                reader = new BufferedReader(new InputStreamReader(new BufferedInputStream(
                        this.helper.openUrlInputStream(SinaPublisher.SESSIONS_URL, true, postData)), "UTF-8"));
                while (null != (line = reader.readLine())) {
                    // ignore me
                }
                reader.close();
                if ((this.helper.lastLocation != null) && this.helper.lastLocation.contains("login_succ")) {
                    reader = new BufferedReader(new InputStreamReader(new BufferedInputStream(
                            this.helper.openUrlInputStream(this.helper.lastLocation)), "UTF-8"));
                    String loc = null;
                    while (null != (line = reader.readLine())) {
                        if (line.contains("http-equiv=\"refresh\"")) {
                            loc = Helper.substringBetween(line, ";url=", "\"");
                        }
                    }
                    reader.close();
                    if (loc != null) {
                        reader = new BufferedReader(new InputStreamReader(new BufferedInputStream(
                                this.helper.openUrlInputStream(loc)), "UTF-8"));
                        while (null != (line = reader.readLine())) {
                            // ignore
                        }
                        reader.close();
                        if (this.helper.lastLocation != null) {
                            reader = new BufferedReader(new InputStreamReader(new BufferedInputStream(
                                    this.helper.openUrlInputStream(this.helper.lastLocation)), "UTF-8"));
                            while (null != (line = reader.readLine())) {
                                // System.out.println(line);
                                if (line.contains("我的首页")) {
                                    success = true;
                                    break;
                                }
                            }
                            reader.close();
                        } else {
                            System.err.println(getName() + ": cross domain login failed!");
                        }
                    } else {
                        System.err.println(getName() + ": meta-redirect failed!");
                    }
                }
            } else {
                System.err.println(getName() + ": incompatible: vk=" + vk + ", pass-key=" + passKey);
            }
        } catch (final Exception e) {
            e.printStackTrace();
            System.err.println(getName() + ": login failed: " + e.toString());
        }
        return success;
    }

    @Override
    protected void beforePublish() throws Exception {
        if (isValid()) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new BufferedInputStream(
                    this.helper.openUrlInputStream(SinaPublisher.STATUS_URL))));
            String line;
            while (null != (line = reader.readLine())) {
                // silent
                if (line.contains("sendmblog?st=")) {
                    this.postKey = Helper.substringBetween(line, "sendmblog?st=", "\"");
                }
            }
            reader.close();
        }
    }

    @Override
    protected void onPublish(String update) throws Exception {
        String postData = String.format(SinaPublisher.POST_PATTERN, URLEncoder.encode(update, "UTF-8"));
        try {
            System.out.println(getName() + ": publish=" + update);
            BufferedReader reader = new BufferedReader(new InputStreamReader(new BufferedInputStream(
                    this.helper.openUrlInputStream(SinaPublisher.POST_URL + this.postKey, true, postData)), "UTF-8"));
            String line;
            while (null != (line = reader.readLine())) {
                // read full
                // System.out.println(line);
            }
            reader.close();
            System.out.println(getName() + ": published=" + update);
        } catch (final IOException e) {
            throw e;
        }

    }

    @Override
    public String getName() {
        return "sina";
    }

}
