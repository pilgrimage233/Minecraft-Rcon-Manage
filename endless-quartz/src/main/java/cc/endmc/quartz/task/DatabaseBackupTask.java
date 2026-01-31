package cc.endmc.quartz.task;

import cc.endmc.framework.database.service.DatabaseBackupExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 数据库备份定时任务
 * 负责调度数据库备份执行器
 *
 * @author Memory
 * @since 2026-01-31
 */
@Slf4j
@Component("databaseBackupTask")
@RequiredArgsConstructor
public class DatabaseBackupTask {

    private final DatabaseBackupExecutor backupExecutor;

    /**
     * 执行定时备份任务
     * 由定时任务调度器调用
     */
    public void backupDatabase() {
        backupExecutor.executeScheduledBackup();
    }
}
