package kr.ssok.common.logging.feign;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import kr.ssok.common.logging.util.LoggingUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collection;

/**
 * Feign Client 요청에 로깅 정보를 추가하는 인터셉터
 */
@Slf4j
@Component
public class FeignLoggingInterceptor implements RequestInterceptor {

    private static final String TRACE_ID_HEADER = "X-Trace-ID";
    private static final String USER_ID_HEADER = "X-User-Id";

    @Override
    public void apply(RequestTemplate template) {
        // Trace ID
        String traceId = LoggingUtil.getTraceId();
        if (traceId != null && !traceId.isEmpty() &&
                !hasHeader(template, TRACE_ID_HEADER)) {
            template.header(TRACE_ID_HEADER, traceId);
        }

        // User ID
        String userId = LoggingUtil.getUserId();
        if (userId != null && !userId.isEmpty() &&
                !hasHeader(template, USER_ID_HEADER)) {
            template.header(USER_ID_HEADER, userId);
        }

        // Feign 요청 로깅
        log.debug("[{}][FEIGN-REQUEST] {} {} - Headers: {}",
                traceId, template.method(), template.url(), template.headers());
    }

    /**
     * 해당 헤더가 이미 존재하는지 확인
     */
    private boolean hasHeader(RequestTemplate template, String headerName) {
        Collection<String> values = template.headers().get(headerName);
        return values != null && !values.isEmpty();
    }
}