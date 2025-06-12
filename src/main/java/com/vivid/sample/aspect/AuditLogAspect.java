package com.vivid.sample.aspect;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vivid.sample.annotation.AuditAction;
import com.vivid.sample.entity.AuditLog;
import com.vivid.sample.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

@Aspect
@Component
public class AuditLogAspect {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AuditLogRepository auditLogRepository; // AuditLogRepository 주입

    public AuditLogAspect(AuditLogRepository auditLogRepository) { // 생성자 주입
        this.auditLogRepository = auditLogRepository;
    }

    // Pointcut: com.vivid.sample.controller 패키지 하위의 모든 클래스 내의 모든 public 메소드
    @Pointcut("within(com.vivid.sample.controller..*)")
    public void controllerPointcut() {}


    // 메소드 실행 *후* (성공/실패 모두) 감사 로그 기록 시도
    // @Around 를 사용하거나 @AfterReturning, @AfterThrowing 조합 사용 가능
    // 여기서는 @AfterReturning 과 @AfterThrowing 을 사용
    @AfterReturning(pointcut = "controllerPointcut()", returning = "result")
    public void logAuditAfterReturning(JoinPoint joinPoint, Object result) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        int statusCode = getStatusCodeFromResult(result); // 상태 코드 추출
        saveAuditLog(joinPoint, request, statusCode, null); // 성공 시 감사 로그 저장
    }

    @AfterThrowing(pointcut = "controllerPointcut()", throwing = "exception")
    public void logAuditAfterThrowing(JoinPoint joinPoint, Throwable exception) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        // 예외 발생 시 상태 코드를 어떻게 기록할지 결정 필요 (예: 500)
        // 여기서는 상태 코드를 null 또는 특정 에러 코드로 저장 가능
        saveAuditLog(joinPoint, request, 500, exception); // 실패 시 감사 로그 저장 (상태코드 500 예시)
    }

    // 감사 로그 저장 로직 (비동기 처리 고려)
    @Async // 별도 스레드에서 비동기로 실행하여 요청 처리 시간에 영향 최소화
    public void saveAuditLog(JoinPoint joinPoint, HttpServletRequest request, Integer statusCode, Throwable exception) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = (authentication != null) ? authentication.getName() : "anonymous"; // 인증 정보 없으면 anonymous

            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            String actionDescription = method.getName(); // 기본값: 메소드 이름
            AuditAction auditActionAnnotation = method.getAnnotation(AuditAction.class);
            if (auditActionAnnotation != null) {
                actionDescription = auditActionAnnotation.value(); // 어노테이션 값 사용
            }

            Object[] args = joinPoint.getArgs();
            List<Object> filteredArgsList = new ArrayList<>();
            for (Object arg : args) {
                // 각 인자를 필터링 로직을 통해 처리 (결과는 필터링된 Map 또는 원본 객체)
                filteredArgsList.add(filterArgument(arg));
            }

            String paramsJson = "[]"; // 기본값: 빈 JSON 배열
            if (!filteredArgsList.isEmpty()) {
                try {
                    // 필터링된 인자 목록을 JSON 배열 문자열로 변환
                    paramsJson = objectMapper.writeValueAsString(filteredArgsList);
                } catch (JsonProcessingException e) {
                    log.warn("Failed to convert audit log parameters to JSON string. Falling back to toString().", e);
                    // JSON 변환 실패 시, 각 인자의 toString() 결과를 콤마로 연결 (기존 방식과 유사)
                    paramsJson = filteredArgsList.stream()
                            .map(obj -> obj != null ? obj.toString() : "null")
                            .collect(Collectors.joining(", "));
                }
            }

            AuditLog audit = AuditLog.builder()
                    .username(username)
                    .requestUri(request.getRequestURI())
                    .httpMethod(request.getMethod())
                    .parameters(paramsJson)
                    .statusCode(statusCode)
                    .action(actionDescription)
                    .build();

            auditLogRepository.save(audit);

        } catch (Exception e) {
            // 감사 로그 저장 실패 시 에러 로깅 (원본 요청 처리에 영향 주지 않도록)
            log.error("Failed to save audit log", e);
        }
    }

    private int getStatusCodeFromResult(Object result) {
        if (result instanceof ResponseEntity) {
            return ((ResponseEntity<?>) result).getStatusCode().value();
        }
        return 200;
    }

    /**
     * 인자를 필터링합니다. DTO나 Map인 경우 민감 정보를 마스킹한 Map을 반환하고,
     * 그 외의 경우는 원본 객체를 반환하여 Jackson이 처리하도록 합니다.
     */
    private Object filterArgument(Object obj) {
        if (obj == null) {
            return null; // JSON null로 표현되도록 null 반환
        }
        if (isDtoOrMap(obj)) {
            try {
                // 객체를 Map으로 변환
                Map<String, Object> map = objectMapper.convertValue(obj, Map.class);
                Map<String, Object> filteredMap = new HashMap<>();
                map.forEach((key, value) -> {
                    // 'password' 포함 키는 마스킹, 나머지는 그대로 유지
                    if (key.toLowerCase().contains("password")) {
                        filteredMap.put(key, "****");
                    } else {
                        // 필요시 다른 민감 정보(email, phone 등) 필터링 로직 추가
                        filteredMap.put(key, value);
                    }
                });
                return filteredMap; // 필터링된 Map 반환
            } catch (IllegalArgumentException e) {
                // Map 변환 실패 시 (예: 호환되지 않는 타입), 원본 객체의 toString() 반환 또는 로깅
                log.warn("Could not convert argument of type {} to Map for filtering. Using toString().", obj.getClass().getName());
                return obj.toString();
            }
        } else {
            // DTO나 Map이 아닌 경우 (String, Long, Integer 등 기본 타입 또는 처리 불가능한 객체)
            // 원본 객체를 그대로 반환하여 Jackson이 JSON으로 변환하도록 위임
            return obj;
        }
    }

    /**
     * 객체가 필터링 대상인 DTO 또는 Map인지 확인합니다.
     * 실제 프로젝트의 DTO 패키지 구조에 맞게 수정해야 합니다.
     */
    private boolean isDtoOrMap(Object obj) {
        if (obj == null) return false;
        Class<?> objClass = obj.getClass();
        // Map 타입이거나 특정 패키지에 속하는 클래스인지 확인
        return Map.class.isAssignableFrom(objClass) ||
                (objClass.getPackage() != null && objClass.getPackage().getName().startsWith("com.vivid.sample.dto"));
    }

}
