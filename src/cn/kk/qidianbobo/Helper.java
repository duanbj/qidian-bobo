/*  Copyright (c) 2010 Xiaoyun Zhu
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

import java.awt.Desktop;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.filechooser.FileSystemView;

public final class Helper {
    public static final List<String> EMPTY_STRING_LIST = Collections.emptyList();

    public static final String[] EMPTY_STRING_ARRAY = new String[0];

    public static final String EMPTY_STRING = "";

    public static final boolean checkPort() {
        try {
            final InetAddress host = InetAddress.getByName(System.getProperty("http.proxyHost"));
            final Socket so = new Socket(host, Integer.parseInt(System.getProperty("http.proxyPort")));
            so.close();
            return true;
        } catch (final Exception e) {
        }
        return false;
    }

    public static final boolean checkUrl(final String link) {
        try {
            final URL url = new URL(link);
            final URLConnection conn = url.openConnection();
            conn.connect();
            return true;
        } catch (final Exception e) {
        }
        return false;
    }

    public static final String chopNull(final String str) {
        if (str == null) {
            return "";
        } else {
            return str;
        }
    }

    public static final String escapeFileName(final String str) {
        final char fileSep = '/';
        final char escape = '_';
        final int len = str.length();
        final StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            final char ch = str.charAt(i);
            if ((ch < ' ') || (ch >= 0x7F) || (ch == fileSep) || (ch == '?') || (ch == '*') || (ch == '\\')
                    || (ch == ':') || (ch == '"') || (ch == '/') || (ch == '+') || (ch == '$') || (ch == '|')
                    || (ch == '&') || ((ch == '.') && (i == 0)) || (ch == escape)) {
                sb.append(escape);
                if (ch < 0x10) {
                    sb.append('0');
                }
                sb.append(Integer.toHexString(ch));
            } else {
                sb.append(ch);
            }
        }
        return sb.toString();
    }

    public static final void fromString(final String boolsString, final boolean[] bools) {
        if (boolsString.length() == bools.length) {
            for (int i = 0; i < bools.length; i++) {
                bools[i] = boolsString.charAt(i) == '1';
            }
        }
    }

    public static boolean isEmptyOrNull(final String text) {
        return (text == null) || text.isEmpty();
    }

    public final static boolean isNotEmptyOrNull(final String text) {
        return (text != null) && (text.length() > 0);
    }

    public static final String substringBetween(final String text, final String start, final String end) {
        return Helper.substringBetween(text, start, end, true);
    }

    public static final String substringBetween(final String text, final String start, final String end,
            final boolean trim) {
        final int nStart = text.indexOf(start);
        final int nEnd = text.indexOf(end, nStart + start.length() + 1);
        if ((nStart != -1) && (nEnd > nStart)) {
            if (trim) {
                return text.substring(nStart + start.length(), nEnd).trim();
            } else {
                return text.substring(nStart + start.length(), nEnd);
            }
        } else {
            return null;
        }
    }

    public final static String substringBetweenLast(final String text, final String start, final String end) {
        return Helper.substringBetweenLast(text, start, end, true);
    }

    public final static String substringBetweenLast(final String text, final String start, final String end,
            final boolean trim) {
        final int nEnd = text.lastIndexOf(end);
        int nStart = -1;
        if (nEnd > 1) {
            nStart = text.lastIndexOf(start, nEnd - 1);
        } else {
            return null;
        }
        if ((nStart < nEnd) && (nStart != -1) && (nEnd != -1)) {
            if (trim) {
                return text.substring(nStart + start.length(), nEnd).trim();
            } else {
                return text.substring(nStart + start.length(), nEnd);
            }
        } else {
            return null;
        }

    }

    public static String substringBetweenNarrow(final String text, final String start, final String end) {
        final int nEnd = text.indexOf(end);
        int nStart = -1;
        if (nEnd != -1) {
            nStart = text.lastIndexOf(start, nEnd - 1);
        }
        if ((nStart < nEnd) && (nStart != -1) && (nEnd != -1)) {
            return text.substring(nStart + start.length(), nEnd);
        } else {
            return null;
        }
    }

    public static final String toString(final boolean[] bools) {
        final StringBuffer sb = new StringBuffer(bools.length);
        for (final boolean b : bools) {
            sb.append(b ? '1' : '0');
        }
        return sb.toString();
    }

    public String lastLocation = "";

    public String userAgent = "Mozilla/5.0 (Windows NT " + (((int) (Math.random() * 2) + 5)) + ".1) Firefox/"
            + (((int) (Math.random() * 8) + 3)) + "." + (((int) (Math.random() * 6) + 0)) + "."
            + (((int) (Math.random() * 6) + 0));

    public String cookieString = "";

    public final Map<String, String> connectionHeaders = new HashMap<String, String>();

    public static final Charset CHARSET_UTF8 = Charset.forName("UTF-8");

    static {
        System.setProperty("http.keepAlive", "false");
        CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));
        HttpURLConnection.setFollowRedirects(false);
    }

    private StringBuffer cookie = new StringBuffer(512);

    private static final FileSystemView FILE_SYSTEM = FileSystemView.getFileSystemView();

    public final static String cut(final String msg, final int maxMsgLen) {
        if (msg.length() <= maxMsgLen) {
            return msg;
        } else {
            return msg.substring(0, maxMsgLen);
        }
    }

    /**
     * <pre>
     * Find resource in possible directories:
     * 1. find in the running directory
     * 2. find in the user directory
     * 3. find in the user document directory
     * 4. find on the user desktop
     * 5. get from root of the running directory
     * 6. load from class path and system path
     * 7. find in all root directories e.g. C:, D:
     * 8. find in temporary directory
     * </pre>
     * 
     * @param resource
     * @return
     * @throws IllegalArgumentException
     * @throws IOException
     */
    public static final File findResource(final String resource) throws IllegalArgumentException {
        File resFile = null;
        if (new File(resource).isFile()) {
            // in run directory
            resFile = new File(resource);
        }
        if (resFile == null) {
            // in user directory
            final String dir = System.getProperty("user.home");
            if (!Helper.isEmptyOrNull(dir)) {
                if (new File(dir, resource).isFile()) {
                    resFile = new File(dir, resource);
                }
            }
        }
        if (resFile == null) {
            // in user document directory
            final File dir = Helper.FILE_SYSTEM.getDefaultDirectory();
            if (dir != null) {
                if (new File(dir, resource).isFile()) {
                    resFile = new File(dir, resource);
                }
            }
        }
        if (resFile == null) {
            // in user desktop directory
            final File dir = Helper.FILE_SYSTEM.getHomeDirectory();
            if (dir != null) {
                if (new File(dir, resource).isFile()) {
                    resFile = new File(dir, resource);
                }
            }
        }
        if (resFile == null) {
            // get from root of run directory
            final File dir = new File("/");
            if (dir.isDirectory()) {
                if (new File(dir, resource).isFile()) {
                    resFile = new File(dir, resource);
                }
            }
        }
        if (resFile == null) {
            // get from class path (root)
            final URL resUrl = Helper.class.getResource("/" + resource);
            if (resUrl != null) {
                try {
                    resFile = File.createTempFile(resource, null);
                    Helper.writeToFile(Helper.class.getResourceAsStream("/" + resource), resFile);
                } catch (final IOException e) {
                    System.err.println("从JAR导出'" + resource + "'时出错：" + e.toString());
                }
                if ((resFile != null) && !resFile.isFile()) {
                    resFile = null;
                }
            }
        }
        if (resFile == null) {
            // find in root directories, e.g. c:\, d:\, e:\, x:\
            final File[] dirs = File.listRoots();
            for (final File dir : dirs) {
                if (dir.isDirectory()) {
                    if (new File(dir, resource).isFile()) {
                        resFile = new File(dir, resource);
                    }
                }
            }
        }
        if (resFile == null) {
            // in temp directory
            final String dir = System.getProperty("java.io.tmpdir");
            if (!Helper.isEmptyOrNull(dir)) {
                if (new File(dir, resource).isFile()) {
                    resFile = new File(dir, resource);
                }
            }
        }
        return resFile;
    }

    public static final FileInputStream findResourceAsStream(final String resource) throws IllegalArgumentException,
            IOException {
        final File file = Helper.findResource(resource);
        if (null != file) {
            return new FileInputStream(file);
        } else {
            return null;
        }
    }

    public final static String formatDuration(final long duration) {
        if (duration == -1) {
            return "???";
        } else {
            final long v = Math.abs(duration);
            final long days = v / 1000 / 60 / 60 / 24;
            final long hours = (v / 1000 / 60 / 60) % 24;
            final long mins = (v / 1000 / 60) % 60;
            final long secs = (v / 1000) % 60;
            final long millis = v % 1000;
            final StringBuilder out = new StringBuilder();
            if (days > 0) {
                out.append(days).append(':').append(Helper.padding(hours, 2, '0')).append(':')
                        .append(Helper.padding(mins, 2, '0')).append(":").append(Helper.padding(secs, 2, '0'))
                        .append(".").append(Helper.padding(millis, 3, '0'));
            } else if (hours > 0) {
                out.append(hours).append(':').append(Helper.padding(mins, 2, '0')).append(":")
                        .append(Helper.padding(secs, 2, '0')).append(".").append(Helper.padding(millis, 3, '0'));
            } else if (mins > 0) {
                out.append(mins).append(":").append(Helper.padding(secs, 2, '0')).append(".")
                        .append(Helper.padding(millis, 3, '0'));
            } else {
                out.append(secs).append(".").append(Helper.padding(millis, 3, '0'));
            }
            return out.toString();
        }
    }

    public final static byte[] fromHex(final String hexString) {
        final String[] data = hexString.trim().split(" ");
        final byte[] result = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            result[i] = (byte) Integer.parseInt(data[i]);
        }
        return result;
    }

    public static final void openUri(final URI uri) {
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().browse(uri);
            } catch (final IOException e) {
                e.printStackTrace();
            }
        } else {
            System.err.println("Not supported!");
        }
    }

    public final static String padding(final long value, final int len, final char c) {
        return Helper.padding(String.valueOf(value), len, c);
    }

    public final static String padding(final String text, final int len, final char c) {
        if ((text != null) && (len > text.length())) {
            final char[] spaces = new char[len - text.length()];
            Arrays.fill(spaces, c);
            return new String(spaces) + text;
        } else {
            return text;
        }
    }

    public static final void write(final InputStream in, final OutputStream out) throws IOException {
        int len;
        final byte[] bytes = new byte[1024 * 1024];
        while ((len = in.read(bytes)) > 0) {
            out.write(bytes, 0, len);
        }
    }

    public static final void writeToFile(final InputStream in, final File file) throws IOException {
        final BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file));
        final byte[] bytes = new byte[1024 * 1024];
        int len;
        while ((len = in.read(bytes)) > 0) {
            out.write(bytes, 0, len);
        }
        out.close();
    }

    public Helper() {
        resetConnectionHeaders();
    }

    public final boolean appendCookies(final StringBuffer cookie, final HttpURLConnection conn) throws IOException {
        try {
            boolean changed = false;
            final Map<String, List<String>> headerFields = conn.getHeaderFields();
            this.lastLocation = conn.getHeaderField("Location");
            if (this.lastLocation == null) {
                this.lastLocation = "";
            }
            final List<String> values = headerFields.get("Set-Cookie");
            if (values != null) {
                for (final String v : values) {
                    if (v.indexOf("deleted") == -1) {
                        if (cookie.length() > 0) {
                            cookie.append("; ");
                        }
                        cookie.append(v.split(";")[0]);
                        changed = true;
                    }
                }
            }
            return changed;
        } catch (final Throwable e) {
            throw new IOException(e);
        }
    }

    public String download(final String url, final String to, final boolean overwrite) throws IOException {
        final File toFile = new File(to);
        // 925 -> forbidden file
        if (!overwrite && toFile.exists() && (toFile.length() > 0) && (toFile.length() != 925)) {
            return null;
        } else {
            OutputStream out = null;
            InputStream in = null;
            try {
                out = new BufferedOutputStream(new FileOutputStream(to));
                in = new BufferedInputStream(openUrlInputStream(url));
                Helper.write(in, out);
            } catch (final IOException e) {
                new File(to).delete();
                e.printStackTrace();
                System.err.println("错误：" + e.toString());
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (final IOException e) {
                        e.printStackTrace();
                    }
                }
                if (out != null) {
                    try {
                        out.close();
                    } catch (final IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return to;
        }
    }

    public final String getHeader(final String key) {
        return this.connectionHeaders.get(key);
    }

    public final HttpURLConnection getUrlConnection(final String url) throws Exception {
        return getUrlConnection(url, false, null);
    }

    public final HttpURLConnection getUrlConnection(final String url, final boolean post, final String output)
            throws IOException {
        int retries = 0;
        HttpURLConnection conn;
        while (true) {
            try {
                final URL urlObj = new URL(url);
                conn = (HttpURLConnection) urlObj.openConnection();
                conn.setConnectTimeout(30 * 1000);
                conn.setReadTimeout(5 * 60 * 1000);
                if (post) {
                    conn.setRequestMethod("POST");
                }
                final String referer;
                final int pathIdx;
                if ((pathIdx = url.lastIndexOf('/')) > "https://".length()) {
                    referer = url.substring(0, pathIdx);
                } else {
                    referer = url;
                }
                conn.setRequestProperty("Referer", referer);
                final Set<String> keys = this.connectionHeaders.keySet();
                for (final String k : keys) {
                    final String value = this.connectionHeaders.get(k);
                    if (value != null) {
                        conn.setRequestProperty(k, value);
                    }
                }
                conn.setUseCaches(false);
                if (output != null) {
                    conn.setDoOutput(true);
                    final BufferedOutputStream out = new BufferedOutputStream(conn.getOutputStream());
                    out.write(output.getBytes(Helper.CHARSET_UTF8));
                    out.close();
                }
                if (appendCookies(this.cookie, conn)) {
                    putConnectionHeader("Cookie", this.cookie.toString());
                }
                break;
            } catch (final Throwable e) {
                // 连接中断
                e.printStackTrace();
                if (retries++ > 20) {
                    throw new IOException(e);
                } else {
                    try {
                        Thread.sleep((60 * retries * 100) + ((int) Math.random() * 100 * 60 * retries));
                    } catch (final InterruptedException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        }
        return conn;
    }

    public final InputStream openUrlInputStream(final String url) throws MalformedURLException, IOException {
        return openUrlInputStream(url, false, null);
    }

    public final InputStream openUrlInputStream(final String url, final boolean post, final String output)
            throws IOException {
        return getUrlConnection(url, post, output).getInputStream();
    }

    public final void putConnectionHeader(final String key, final String value) {
        this.connectionHeaders.put(key, value);
    }

    public final void resetConnectionHeaders() {
        this.connectionHeaders.clear();
        this.connectionHeaders.put("User-Agent", this.userAgent);
        if (Helper.isNotEmptyOrNull(this.cookieString)) {
            this.cookie = new StringBuffer(this.cookieString);
        } else {
            this.cookie = new StringBuffer();
        }
        putConnectionHeader("Cookie", this.cookie.toString());
        this.connectionHeaders.put("Cache-Control", "no-cache");
        this.connectionHeaders.put("Pragma", "no-cache");
    }

    public final void setMobileUserAgent() {
        this.userAgent = "Mozilla/5.0 (Linux; U; Android 2."
                + (((int) (Math.random() * 3) + 0))
                + "; en-us; Nexus One Build/FRF91) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1";
        this.connectionHeaders.put("User-Agent", this.userAgent);
    }

    public String getCookie(String key) {
        final String cookie = getHeader("Cookie");
        if (Helper.isNotEmptyOrNull(cookie) && cookie.contains(key)) {
            final List<String> cookies = Helper.splitList(cookie, ';');
            String keyPrefix = key + "=";
            for (String c : cookies) {
                if (c.startsWith(keyPrefix)) {
                    return (c.substring(keyPrefix.length())).trim();
                }
            }
        }
        return null;

    }

    public static List<String> splitList(final String toSplit, final char delimiter) {
        if (Helper.isEmptyOrNull(toSplit)) {
            return Helper.EMPTY_STRING_LIST;
        }

        List<String> split = new LinkedList<String>();
        int nextBegin = 0;
        for (int i = 0; i < toSplit.length(); i++) {
            if (toSplit.charAt(i) == delimiter) {
                if (i > nextBegin) {
                    split.add(toSplit.substring(nextBegin, i).trim());
                } else {
                    split.add(Helper.EMPTY_STRING);
                }
                nextBegin = i + 1;
            }
        }
        if (toSplit.charAt(toSplit.length() - 1) != delimiter) {
            split.add(toSplit.substring(nextBegin, toSplit.length()).trim());
        }

        if (split.isEmpty()) {
            return Helper.EMPTY_STRING_LIST;
        } else {
            return split;
        }
    }

    public static String[] split(final String toSplit, final char delimiter) {
        List<String> result = Helper.splitList(toSplit, delimiter);
        if (result.isEmpty()) {
            return Helper.EMPTY_STRING_ARRAY;
        } else {
            return result.toArray(new String[result.size()]);
        }
    }

    private static MessageDigest MD5;
    static {
        try {
            Helper.MD5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public static byte[] md5(final String text) {
        return Helper.md5(text.getBytes());
    }

    public static byte[] md5(final byte[] data) {
        Helper.MD5.reset();
        Helper.MD5.update(data);
        return Helper.MD5.digest();
    }

    public static String toHexString(final byte[] data) {
        return Helper.toHexString(data, 0, data.length);
    }

    public static String toHexString(final byte[] data, final int offset, final int len) {
        StringBuffer sb = new StringBuffer(len * 2);
        for (int idx = offset; idx < (offset + len); idx++) {
            byte b = data[idx];
            int i = b & 0xff;
            if (i < 0xf) {
                sb.append('0');
            }
            sb.append(Integer.toHexString(i));
        }
        return sb.toString().toUpperCase();
    }

    public static byte[] merge(byte[] a1, byte[] a2) {
        byte[] result = new byte[a1.length + a2.length];
        System.arraycopy(a1, 0, result, 0, a1.length);
        System.arraycopy(a2, 0, result, a1.length, a2.length);
        return result;
    }

}
