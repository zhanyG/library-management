package com.example.library;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 图书管理系统启动类。
 *
 * <p>分层结构（详见 specs/07-architecture.md 与 specs/08-package-diagram.puml）：
 * presentation（MVC 的 Controller 与 DTO）、application（用例编排与事务边界）、
 * domain（实体、值对象、策略与领域服务、Repository 端口）、
 * infrastructure（持久化适配器）。</p>
 */
@SpringBootApplication
public class LibraryApplication {

    public static void main(String[] args) {
        SpringApplication.run(LibraryApplication.class, args);
    }
}
