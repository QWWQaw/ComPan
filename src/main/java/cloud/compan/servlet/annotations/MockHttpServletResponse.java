package cloud.compan.servlet.annotations;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

/**
 * 模拟 HttpServletResponse 实现
 * 用于单元测试环境
 */
public class MockHttpServletResponse implements HttpServletResponse, MockHttpServletResponse_encodeUrl {

    private int status = HttpServletResponse.SC_OK;
    private String statusMessage;
    private final Map<String, List<String>> headers = new HashMap<>();
    private final List<Cookie> cookies = new ArrayList<>();
    private String characterEncoding = "UTF-8";
    private String contentType;
    private Locale locale = Locale.getDefault();
    private final ByteArrayOutputStream content = new ByteArrayOutputStream(1024);
    private final StringWriter writerContent = new StringWriter();
    private boolean committed;
    private int bufferSize = 4096;
    private String redirectUrl;
    private String errorMessage;
    private int errorCode;

    @Override
    public void addCookie(Cookie cookie) {
        cookies.add(cookie);
    }

    @Override
    public boolean containsHeader(String name) {
        return headers.containsKey(name);
    }

    @Override
    public String encodeURL(String url) {
        return url;
    }

    @Override
    public String encodeRedirectURL(String url) {
        return url;
    }




    @Override
    public void sendError(int sc, String msg) throws IOException {
        this.status = sc;
        this.errorMessage = msg;
        this.errorCode = sc;
        this.committed = true;
    }

    @Override
    public void sendError(int sc) throws IOException {
        this.status = sc;
        this.errorCode = sc;
        this.committed = true;
    }

    @Override
    public void sendRedirect(String location) throws IOException {
        this.status = HttpServletResponse.SC_FOUND;
        this.redirectUrl = location;
        setHeader("Location", location);
        this.committed = true;
    }

    @Override
    public void setDateHeader(String name, long date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        setHeader(name, dateFormat.format(new Date(date)));
    }

    @Override
    public void addDateHeader(String name, long date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        addHeader(name, dateFormat.format(new Date(date)));
    }

    @Override
    public void setHeader(String name, String value) {
        List<String> values = new ArrayList<>();
        values.add(value);
        headers.put(name, values);
    }

    @Override
    public void addHeader(String name, String value) {
        headers.computeIfAbsent(name, k -> new ArrayList<>()).add(value);
    }

    @Override
    public void setIntHeader(String name, int value) {
        setHeader(name, String.valueOf(value));
    }

    @Override
    public void addIntHeader(String name, int value) {
        addHeader(name, String.valueOf(value));
    }

    @Override
    public void setStatus(int sc) {
        this.status = sc;
    }



    @Override
    public int getStatus() {
        return status;
    }

    @Override
    public String getHeader(String name) {
        List<String> values = headers.get(name);
        return (values != null && !values.isEmpty()) ? values.get(0) : null;
    }

    @Override
    public Collection<String> getHeaders(String name) {
        return headers.getOrDefault(name, new ArrayList<>());
    }

    @Override
    public Collection<String> getHeaderNames() {
        return headers.keySet();
    }

    @Override
    public String getCharacterEncoding() {
        return characterEncoding;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public ServletOutputStream getOutputStream() {
        return new ServletOutputStream() {
            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setWriteListener(WriteListener writeListener) {
                // 不支持异步
            }

            @Override
            public void write(int b) {
                content.write(b);
            }

            @Override
            public void write(byte[] b, int off, int len) {
                content.write(b, off, len);
            }
        };
    }

    @Override
    public PrintWriter getWriter() {
        return new PrintWriter(writerContent) {
            @Override
            public void flush() {
                super.flush();
                try {
                    content.write(writerContent.toString().getBytes(characterEncoding));
                } catch (IOException e) {
                    // 使用默认编码
                    try {
                        content.write(writerContent.toString().getBytes());
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
        };
    }

    @Override
    public void setCharacterEncoding(String charset) {
        this.characterEncoding = charset;
    }

    @Override
    public void setContentType(String type) {
        this.contentType = type;
        if (type != null && characterEncoding != null) {
            setHeader("Content-Type", type + ";charset=" + characterEncoding);
        } else if (type != null) {
            setHeader("Content-Type", type);
        }
    }

    @Override
    public void setContentLength(int len) {
        setIntHeader("Content-Length", len);
    }

    @Override
    public void setContentLengthLong(long len) {
        setHeader("Content-Length", String.valueOf(len));
    }

    @Override
    public void setBufferSize(int size) {
        this.bufferSize = size;
    }

    @Override
    public int getBufferSize() {
        return bufferSize;
    }

    @Override
    public void flushBuffer() {
        committed = true;
    }

    @Override
    public void resetBuffer() {
        content.reset();
        writerContent.getBuffer().setLength(0);
    }

    @Override
    public boolean isCommitted() {
        return committed;
    }

    @Override
    public void reset() {
        resetBuffer();
        headers.clear();
        cookies.clear();
        status = HttpServletResponse.SC_OK;
        statusMessage = null;
        characterEncoding = "UTF-8";
        contentType = null;
        committed = false;
        redirectUrl = null;
        errorMessage = null;
        errorCode = 0;
    }

    @Override
    public void setLocale(Locale loc) {
        this.locale = loc;
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    // ============ 自定义方法 ============

    /**
     * 获取响应内容字节数组
     */
    public byte[] getContentAsByteArray() {
        try {
            flushBuffer();
            return content.toByteArray();
        } finally {
            resetBuffer();
        }
    }

    /**
     * 获取响应内容字符串
     */
    public String getContentAsString() {
        try {
            return new String(getContentAsByteArray(), characterEncoding);
        } catch (UnsupportedEncodingException e) {
            return new String(getContentAsByteArray());
        }
    }

    /**
     * 获取重定向URL
     */
    public String getRedirectUrl() {
        return redirectUrl;
    }

    /**
     * 获取错误消息
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * 获取错误码
     */
    public int getErrorCode() {
        return errorCode;
    }

    /**
     * 获取所有Cookies
     */
    public Cookie[] getCookies() {
        return cookies.toArray(new Cookie[0]);
    }

    /**
     * 获取指定名称的Cookie
     */
    public Cookie getCookie(String name) {
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(name)) {
                return cookie;
            }
        }
        return null;
    }
}