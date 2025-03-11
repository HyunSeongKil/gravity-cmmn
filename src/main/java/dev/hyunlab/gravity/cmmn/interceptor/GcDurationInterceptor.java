package dev.hyunlab.gravity.cmmn.interceptor;

import java.util.Date;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import dev.hyunlab.gravity.cmmn.misc.GcUtils;
import jakarta.annotation.Nonnull;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component(value = "gcDurationInterceptor")
public class GcDurationInterceptor implements HandlerInterceptor {

    @PostConstruct
    private void init() {
        log.info("<<.init");
    }

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
            @NonNull Object handler)
            throws Exception {
        if (request.getMethod().equals("OPTIONS")) {
            return true;
        }

        request.setAttribute("startDt", new Date());

        // TODO 업무로직

        // log.info("<< {}", request.getRequestURI());
        return true;
    }

    @Override
    public void postHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
            @NonNull Object handler,
            @Nullable ModelAndView modelAndView) throws Exception {
        if (request.getMethod().equals("OPTIONS")) {
            return;
        }

        // 소요시간
        log.info("<< {}ms\t{}\t{}\t{}",
                (new Date().getTime() - ((Date) request.getAttribute("startDt")).getTime()),
                request.getMethod(),
                request.getRequestURI(),
                request.getQueryString() != null ? request.getQueryString() : "");

    }

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
            @NonNull Object handler,
            @Nullable Exception ex) throws Exception {

        log.debug("<<");
    }

}
