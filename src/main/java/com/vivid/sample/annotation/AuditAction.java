package com.vivid.sample.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD) // 메소드에 적용할 어노테이션
@Retention(RetentionPolicy.RUNTIME) // 런타임 시 어노테이션 정보 유지
public @interface AuditAction {
    String value(); // 행위 설명을 저장할 속성
}
