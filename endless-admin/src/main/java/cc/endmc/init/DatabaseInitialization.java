package cc.endmc.init;

import cc.endmc.framework.database.service.DatabaseMigrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 数据库初始化组件
 * 在 Bean 初始化时最先执行，确保数据库结构完整
 * 使用 InitializingBean 接口 + @Order 注解，优先级最高
 * <p>
 * 执行顺序：
 * 1. DatabaseInitialization (InitializingBean, Order = HIGHEST_PRECEDENCE) - 最先执行
 * 2. EndlessInitialization (InitializingBean, 默认优先级) - 之后执行
 *
 * @author Memory
 * @since 2026-01-29
 */
@Slf4j
@Component("databaseInitialization")
@Order(Ordered.HIGHEST_PRECEDENCE) // 最高优先级
@RequiredArgsConstructor
public class DatabaseInitialization implements InitializingBean {

    private final DatabaseMigrationService migrationService;

    @Override
    public void afterPropertiesSet() {
        try {
            log.info("🔄 DATABASE INIT: 开始数据库初始化检查...");
            long startTime = System.currentTimeMillis();

            // 执行数据库迁移
            DatabaseMigrationService.MigrationResult result = migrationService.migrate();

            long elapsedTime = System.currentTimeMillis() - startTime;

            if (result.isSuccess()) {
                if (result.getExecutedCount() > 0) {
                    log.info("✅ DATABASE INIT: 数据库初始化完成 - {} (耗时 {} ms)",
                            result.getMessage(), elapsedTime);
                } else {
                    log.info("✅ DATABASE INIT: {} (耗时 {} ms)",
                            result.getMessage(), elapsedTime);
                }
            } else {
                log.error("❌ DATABASE INIT: 数据库初始化失败 - {}", result.getMessage());
                if (result.getError() != null) {
                    log.error("❌ DATABASE INIT: 错误详情", result.getError());
                }
                throw new RuntimeException("数据库初始化失败: " + result.getMessage(), result.getError());
            }

        } catch (Exception e) {
            log.error("❌ DATABASE INIT: 数据库初始化异常，应用启动失败", e);
            // 打印更详细的错误信息
            if (e.getCause() != null) {
                log.error("❌ DATABASE INIT: 根本原因", e.getCause());
            }
            throw new RuntimeException("数据库初始化异常，应用启动失败", e);
        }
    }
}
