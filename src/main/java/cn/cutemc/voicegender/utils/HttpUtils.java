package cn.cutemc.voicegender.utils;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.web.client.RestTemplate;

@CommonsLog
public class HttpUtils {

    /**
     * 发送GET请求到URL
     *
     * @param url URL
     * @param retryTimes 重试次数
     * @return 响应内容
     */
    public static String get(String url, int retryTimes) {
        RestTemplate restTemplate = new RestTemplate();

        String result = null;
        Exception e = null;

        for (int i = 0; i < retryTimes; i++) {
            try {
                result = restTemplate.getForObject(url, String.class);
                break;
            } catch (Exception ex) {
                e = ex;
            }

            log.warn("Retrying " + url + " for " + (i + 1) + " time(s).");
        }

        if (result == null) {
            throw new RuntimeException(e);
        }

        return result;
    }

    /**
     * 发送POST请求到URL
     * @param url URL
     * @param body 请求体
     * @param retryTimes 重试次数
     * @return 响应内容
     */
    public static String post(String url, String body, int retryTimes) {
        RestTemplate restTemplate = new RestTemplate();

        String result = null;
        Exception e = null;

        for (int i = 0; i < retryTimes; i++) {
            try {
                result = restTemplate.postForObject(url, body, String.class);
                break;
            } catch (Exception ex) {
                e = ex;
            }

            log.warn("Retrying " + url + " for " + (i + 1) + " time(s).");
        }

        if (result == null) {
            throw new RuntimeException(e);
        }

        return result;
    }

    /**
     * 获取请求的真实IP地址
     * @param request 请求
     * @return IP地址
     */
    public static String getRemoteAddr(HttpServletRequest request) {
        String addr = request.getRemoteAddr();

        if (request.getHeader("x-forwarded-for") != null) {
            addr = request.getHeader("x-forwarded-for");
        }

        return addr;
    }
}
