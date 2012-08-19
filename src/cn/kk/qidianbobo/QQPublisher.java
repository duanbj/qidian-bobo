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

public class QQPublisher extends Publisher {
    private final static String LOGIN_URL = "http://t.qq.com/";

    // http://check.ptlogin2.qq.com/check?uin=miaomiaokatze-1@yahoo.com&appid=46000101&ptlang=2052&r=0.04309582148079838
    private final static String VERIFY_URL = "http://check.ptlogin2.qq.com/check?uin=%s&appid=46000101&ptlang=2052&r=%s";

    // http://ptlogin2.qq.com/login?ptlang=2052&u=%s&p=259030B85A3253A2C32B1B8C1AE5795D&verifycode=!HVI&low_login_enable=1&low_login_hour=720&aid=46000101&u1=http%3A%2F%2Ft.qq.com&ptredirect=1&h=1&from_ui=1&dumy=&fp=loginerroralert&action=3-42-26726&g=1&t=1&dummy=
    private final static String SESSIONS_URL = "http://ptlogin2.qq.com/login?ptlang=2052&u=%s&p=%s&verifycode=%s&low_login_enable=1&low_login_hour=720&aid=46000101&u1=http%%3A%%2F%%2Ft.qq.com&ptredirect=1&h=1&from_ui=1&dumy=&fp=loginerroralert&action=3-42-26726&g=1&t=1&dummy=";

    private final static String POST_URL = "http://api.t.qq.com/old/publish.php";

    // content=test%20test&startTime=1342515558626&endTime=1342515566133&countType=&viewModel=&attips=&pic=&apiType=8&syncQzone=0&syncQQSign=0
    private final static String POST_PATTERN = "content=%s&startTime=%s&endTime=%s&countType=&viewModel=&attips=&pic=&apiType=8&syncQzone=0&syncQQSign=0";

    private final static int MAX_MSG_LEN = 140;

    public static void main(final String[] args) throws Exception {
        QQPublisher publisher = new QQPublisher(new Main());
        publisher.onLogin("user", "pass");
        publisher.beforePublish();
        publisher.onPublish("ni hao!");
    }

    public QQPublisher(final Main main) {
        super(main, main.chkQQ);
    }

    @Override
    protected boolean onLogin(final String user, final String pass) {
        boolean success = false;
        try {
            // System.out.println(getName()+": user=" + user + ", pass=" + pass);
            final String encUser = URLEncoder.encode(user, "UTF-8");
            final String encPass = URLEncoder.encode(pass, "UTF-8");

            BufferedReader reader = new BufferedReader(new InputStreamReader(new BufferedInputStream(
                    this.helper.openUrlInputStream(QQPublisher.LOGIN_URL)), "UTF-8"));
            String line;
            while (null != (line = reader.readLine())) {
                // ignore me
            }
            reader.close();

            String verifyCode = null;
            String uin = null;
            String verifyUrl = String.format(QQPublisher.VERIFY_URL, encUser, String.valueOf(Math.random()));
            reader = new BufferedReader(new InputStreamReader(new BufferedInputStream(
                    this.helper.openUrlInputStream(verifyUrl)), "UTF-8"));
            while (null != (line = reader.readLine())) {
                if (line.contains("ptui_checkVC(")) {
                    System.out.println(line);
                    verifyCode = Helper.substringBetween(line, "'0','", "','");
                    uin = Helper.substringBetween(line, "','\\x", "')");
                    break;
                }
            }
            reader.close();

            if ((verifyCode != null) && (uin != null)) {
                Thread.sleep(600);
                byte[] uinData = QQPublisher.decodeHex(uin);
                final String sessionsUrl = String.format(QQPublisher.SESSIONS_URL, encUser,
                        QQPublisher.encrypt(encPass, verifyCode, uinData), verifyCode);
                System.out.println(sessionsUrl);
                reader = new BufferedReader(new InputStreamReader(new BufferedInputStream(
                        this.helper.openUrlInputStream(sessionsUrl)), "UTF-8"));
                while (null != (line = reader.readLine())) {
                    // ignore me
                    if (line.contains("登录成功")) {
                        success = true;
                        break;
                    }
                }
                reader.close();
            } else {
                System.err.println(getName() + ": verifyCode not found!");
            }
        } catch (final Exception e) {
            e.printStackTrace();
            System.err.println(getName() + ": login failed with error: " + e.toString());
        }
        return success;
    }

    private final static byte[] decodeHex(String uin) {
        final String[] split = uin.split("\\\\x");
        final byte[] uinData = new byte[split.length];
        for (int i = 0; i < split.length; i++) {
            String s = split[i];
            uinData[i] = (byte) Integer.parseInt(s, 16);
        }
        return uinData;
    }

    private static String encrypt(String pass, String verifyCode, byte[] uin) {
        // md5(md5(hexchar2bin(md5(pw)) + pt.uin) + verifyCode.toUpper())
        byte[] result = Helper.md5(Helper.toHexString(Helper.md5(Helper.merge(Helper.md5(pass), uin)))
                + verifyCode.toUpperCase());
        return Helper.toHexString(result);

    }

    @Override
    protected void beforePublish() throws Exception {

    }

    @Override
    protected void onPublish(String update) throws Exception {
        String start = String.valueOf(System.currentTimeMillis());
        String end = String.valueOf(System.currentTimeMillis() + 8);

        String postData = String.format(QQPublisher.POST_PATTERN, URLEncoder.encode(update, "UTF-8"), start, end);
        try {
            System.out.println(getName() + ": publish=" + update);
            BufferedReader reader = new BufferedReader(new InputStreamReader(new BufferedInputStream(
                    this.helper.openUrlInputStream(QQPublisher.POST_URL, true, postData)), "UTF-8"));
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
        return "qq";
    }

}
