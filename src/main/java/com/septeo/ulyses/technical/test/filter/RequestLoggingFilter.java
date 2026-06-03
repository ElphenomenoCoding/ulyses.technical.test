package com.septeo.ulyses.technical.test.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Servlet filter that logs every incoming request and outgoing response to a file.
 * Each line records the request timestamp, HTTP method, URL, response status and
 * the processing time in milliseconds. Registered first in the chain so that responses
 * short-circuited by security (e.g. 401) are still logged.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestLoggingFilter implements Filter {

    private static final String LOG_FILE_PATH = "logs/api-requests.log";
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final long NANOS_PER_MILLI = 1_000_000L;

    private final Path logFile = Paths.get(LOG_FILE_PATH);
    private final Object writeLock = new Object();

    public RequestLoggingFilter() {
        Path parent = logFile.getParent();
        if (parent != null) {
            try {
                Files.createDirectories(parent);
            } catch (IOException e) {
                System.err.println("Failed to create request log directory: " + e.getMessage());
            }
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        LocalDateTime requestTime = LocalDateTime.now();
        long startNanos = System.nanoTime();
        try {
            chain.doFilter(request, response);
        } finally {
            long processingMillis = (System.nanoTime() - startNanos) / NANOS_PER_MILLI;
            writeLogLine(requestTime, httpRequest, httpResponse, processingMillis);
        }
    }

    /**
     * Append a single formatted log line to the log file, serialising concurrent writes.
     *
     * @param requestTime      the time the request was received
     * @param request          the incoming request
     * @param response         the outgoing response
     * @param processingMillis the processing time in milliseconds
     */
    private void writeLogLine(LocalDateTime requestTime, HttpServletRequest request,
                              HttpServletResponse response, long processingMillis) {
        String line = formatLine(requestTime, request, response, processingMillis);
        synchronized (writeLock) {
            try {
                Files.writeString(logFile, line, StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            } catch (IOException e) {
                System.err.println("Failed to write request log: " + e.getMessage());
            }
        }
    }

    /**
     * Build the log line for a request/response pair.
     *
     * @param requestTime      the time the request was received
     * @param request          the incoming request
     * @param response         the outgoing response
     * @param processingMillis the processing time in milliseconds
     * @return the formatted log line, terminated by a line separator
     */
    private String formatLine(LocalDateTime requestTime, HttpServletRequest request,
                              HttpServletResponse response, long processingMillis) {
        return String.format("%s | %s | %s | %d | %d ms%n",
                TIMESTAMP_FORMAT.format(requestTime),
                request.getMethod(),
                buildUrl(request),
                response.getStatus(),
                processingMillis);
    }

    /**
     * Build the full request URL, including the query string when present.
     *
     * @param request the incoming request
     * @return the request URL
     */
    private String buildUrl(HttpServletRequest request) {
        String url = request.getRequestURL().toString();
        String query = request.getQueryString();
        return query == null ? url : url + "?" + query;
    }
}
