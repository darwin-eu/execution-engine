package eu.darwin.node.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Base64;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;

@Slf4j
public class AuthFilter implements Filter {

    private static final Set<String> PUBLIC_ENDPOINTS = Set.of("/api/v1/error", "/api/v1/execution-engine-ws", "/api/v1/topic", "/api/v1/app", "/api/v1/favicon.ico");
    private static final String AUTHENTICATION_SCHEME = "Bearer";
    private static final ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(120))
            .build();
    private static String USERINFO;
    private static String ISSUER;
    private static final LoadingCache<String, UserInfo> cache = Caffeine.newBuilder()
            .maximumSize(100)
            .expireAfterWrite(20, TimeUnit.MINUTES)
            .build(AuthFilter::processHeader);
    @Autowired
    UserStore userStore;


    @Value("${authentication.enabled}")
    private boolean authEnabled;


    private static UserInfo processHeader(String header) {
        String token = header.replace(AUTHENTICATION_SCHEME, "").trim();
        if (token.isBlank() || token.equals("undefined")) {
            return null;
        }
        DecodedJWT jwt = JWT.decode(token);
        String json;
        try {
            json = new String(Base64.getUrlDecoder().decode(jwt.getPayload()));
        } catch (IllegalArgumentException e) {
            json = new String(Base64.getDecoder().decode(jwt.getPayload()));
        }
        try {
            var payload = mapper.readValue(json, TokenPayload.class);
            if (!payload.issuer().equals(ISSUER)) {
                return null;
            }
            var expiration = payload.expiration();
            var userInfo = getUserInfo(header);
            if (userInfo != null) {
                userInfo.expiration(expiration);
                return userInfo;
            }
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
            return null;
        }
        return null;
    }

    private static HttpRequest buildGetRequest(String token) {
        return HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(USERINFO))
                .setHeader(AUTHORIZATION, token)
                .header(CONTENT_TYPE, "application/json")
                .build();
    }

    private static UserInfo getUserInfo(String header) {
        var request = buildGetRequest(header);
        try {
            HttpResponse<String> resp = httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString()).get();
            if (resp.statusCode() == 200) {
                var body = resp.body();
                return mapper.readValue(body, UserInfo.class);
            } else if (resp.statusCode() == 401) {
                log.warn("401 Unauthorized: {}", resp.body());
            } else {
                log.error("Request [{}] failed with status code {}", request.uri().toString(), resp.statusCode());
                log.error("Response was: {}", resp.body());
            }
        } catch (InterruptedException | ExecutionException | JsonProcessingException e) {
            log.error("Something went wrong authenticating");
            log.error(e.toString());
        }
        return null;
    }

    @Value("${authentication.userinfo}")
    public void setUserInfoStatic(String userInfoEndpoint) {
        AuthFilter.USERINFO = userInfoEndpoint;
    }

    @Value("${authentication.issuer}")
    public void setIssuerStatic(String issuer) {
        AuthFilter.ISSUER = issuer;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {
        if (authEnabled) {
            authenticate(servletRequest, servletResponse, chain);
        } else {
            chain.doFilter(servletRequest, servletResponse);
        }
    }

    private void authenticate(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {
        try {
            if (servletRequest instanceof HttpServletRequest request) {
                String path = request.getRequestURI().toLowerCase();
                if (request.getMethod().equals("OPTIONS") || PUBLIC_ENDPOINTS.contains(path) || path.contains("/swagger-ui") || path.contains("/v3/api-docs")) {
                    chain.doFilter(servletRequest, servletResponse);
                } else {
                    String details = requestDetails(request);
                    String authorizationHeader = request.getHeader(AUTHORIZATION);
                    if (!isValid(authorizationHeader)) {
                        abortWithUnauthorized(servletResponse, details);
                    } else {
                        log.info(details);
                        chain.doFilter(servletRequest, servletResponse);
                    }
                }
            } else {
                log.warn("Received a request that was not an HttpServletRequest {}", servletRequest);
            }
        } finally {
            userStore.clear();
        }

    }

    private String requestDetails(HttpServletRequest request) {
        String path = request.getRequestURI();
        StringBuilder params = new StringBuilder();
        request.getParameterMap().forEach((k, v) -> params.append(k).append("=").append(v[0]).append(" "));
        return request.getMethod() + " " + path + " " + params.toString().trim();
    }

    private void abortWithUnauthorized(ServletResponse servletResponse, String path) throws IOException {
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        log.warn("UNAUTHORIZED {}", path);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        String errorJson = "{\"Unauthorized\": \"You did not say the magic word, if you are certain you did call the helpdesk\"}";
        response.getWriter().write(errorJson);
        response.getWriter().close();
        response.getWriter().flush();
    }

    private boolean isValid(String header) {
        if (header == null || !header.startsWith(AUTHENTICATION_SCHEME)) {
            return false;
        }
        var userInfoFromCache = cache.getIfPresent(header);
        log.debug("Loaded user from from cache");
        if (userInfoFromCache != null) {
            userStore.userInfo(userInfoFromCache);
            return userInfoFromCache.expiration() > System.currentTimeMillis() / 1000L;
        }

        // Check expiration
        UserInfo userInfo = processHeader(header);
        if (userInfo == null || userInfo.expiration() < System.currentTimeMillis() / 1000L) {
            return false;
        } else {
            userStore.userInfo(userInfo);
            return true;
        }
    }
}
