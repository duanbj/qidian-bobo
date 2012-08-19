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

public class SohuPublisher extends Publisher {
    private final static String LOGIN_URL = "http://w.sohu.com/t2/tologin.do?uname=&f_r=&fr=&passtype=1";

    private final static String SESSIONS_URL = "http://w.sohu.com/t2/login.do";

    private final static String SESSIONS_PATTERN = "uname=%s&p=%s&saveLoginState=1&m=doLogin&f_r=&fr=";

    private final static String POST_URL = "http://w.sohu.com/t2/send.do?s_m_u=%s&suv=%s";

    private final static String POST_PATTERN = "content=%s&hiddenContent=&ru=.%%2Ffridoc.do&eru=.%%2Ffridoc.do&eruaction=fridoc&filter=&kw=&cc_key=wap_send_weibo&send=%%E5%%8F%%91%%E8%%A1%%A8";

    private final static int MAX_MSG_LEN = 140;

    private String suvToken;
    private String smuToken;

    public static void main(final String[] args) throws Exception {
        SohuPublisher publisher = new SohuPublisher(new Main());
        publisher.onLogin("user", "pass");
        publisher.beforePublish();
        publisher.onPublish("ni hao!");
    }

    public SohuPublisher(final Main main) {
        super(main, main.chkSohu, true, 7);
    }

    @Override
    protected boolean onLogin(final String user, final String pass) {
        try {
            // System.out.println(getName()+": user=" + user + ", pass=" + pass);
            final String encUser = URLEncoder.encode(user, "UTF-8");
            final String encPass = URLEncoder.encode(pass, "UTF-8");
            BufferedReader reader = new BufferedReader(new InputStreamReader(new BufferedInputStream(
                    this.helper.openUrlInputStream(SohuPublisher.LOGIN_URL)), "UTF-8"));
            String line;
            while (null != (line = reader.readLine())) {
                // ignore me
            }
            reader.close();
            this.suvToken = this.helper.getCookie("suvc");
            if (this.suvToken != null) {
                Thread.sleep(600);
                final String postData = String.format(SohuPublisher.SESSIONS_PATTERN, encUser, encPass);
                reader = new BufferedReader(new InputStreamReader(new BufferedInputStream(
                        this.helper.openUrlInputStream(SohuPublisher.SESSIONS_URL, true, postData)), "UTF-8"));
                while (null != (line = reader.readLine())) {
                    // ignore me
                }
                reader.close();

                System.out.println(getName() + ": location=" + this.helper.lastLocation);
                this.smuToken = this.helper.getCookie("s_m_u");
            } else {
                System.err.println(getName() + ": suv cookie not found!");
            }
        } catch (final Exception e) {
            System.err.println(getName() + ": login failed: " + e.toString());
        }
        if (this.smuToken != null) {
            System.out.println(getName() + ": smu-token=" + this.smuToken);
            System.out.println(getName() + ": suv-token=" + this.suvToken);
            if ((this.smuToken != null) && (this.suvToken != null)) {
                return true;
            }
        }
        return false;

    }

    @Override
    protected void beforePublish() throws Exception {
    }

    @Override
    protected void onPublish(String update) throws Exception {
        String getUrl = String.format(SohuPublisher.POST_URL, URLEncoder.encode(this.smuToken, "UTF-8"),
                URLEncoder.encode(this.suvToken, "UTF-8"));
        String postData = String.format(SohuPublisher.POST_PATTERN, URLEncoder.encode(update, "UTF-8"));
        try {
            System.out.println(getName() + ": publish=" + update);
            BufferedReader reader = new BufferedReader(new InputStreamReader(new BufferedInputStream(
                    this.helper.openUrlInputStream(getUrl, true, postData)), "UTF-8"));
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
        return "sohu";
    }

}
