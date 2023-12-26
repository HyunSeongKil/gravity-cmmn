package dev.hyunlab.gravity.cmmn.interceptor;

import java.util.Date;

import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

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
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        request.setAttribute("startDt", new Date());

        // TODO 업무로직

        log.debug("<< {}", request.getRequestURI());
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
            @Nullable ModelAndView modelAndView) throws Exception {

        // 소요시간
        log.debug("<< DURATION:{}ms {}", (new Date().getTime() - ((Date) request.getAttribute("startDt")).getTime()),
                request.getRequestURI());
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
            @Nullable Exception ex) throws Exception {

        log.debug("<<");
    }

}
