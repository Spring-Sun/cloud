package com.cloud.job.controller;

import com.cloud.common.core.domain.R;
import com.cloud.job.domain.JobExecResult;
import com.cloud.job.task.DemoScheduledTask;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 定时任务管理接口。通过网关以 {@code /job/**} 访问（前缀会被剔除）。
 */
@RestController
@RequestMapping("/task")
public class JobController {

    private final DemoScheduledTask demoScheduledTask;

    public JobController(DemoScheduledTask demoScheduledTask) {
        this.demoScheduledTask = demoScheduledTask;
    }

    /**
     * 手动触发示例任务（同样受分布式锁保护）。
     */
    @PostMapping("/demo/run")
    public R<JobExecResult> runDemo() {
        return R.ok(demoScheduledTask.execute());
    }

    /**
     * 查询示例任务状态。
     */
    @GetMapping("/demo/status")
    public R<Map<String, Object>> demoStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("jobName", demoScheduledTask.getJobName());
        status.put("lastExecutedAt", demoScheduledTask.lastExecutedAt());
        return R.ok(status);
    }
}
