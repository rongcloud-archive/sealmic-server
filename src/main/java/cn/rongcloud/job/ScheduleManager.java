package cn.rongcloud.job;

import cn.rongcloud.config.RoomProperties;
import cn.rongcloud.dao.RoomMemberDao;
import cn.rongcloud.im.IMHelper;
import cn.rongcloud.service.RoomService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.FixedDelayTask;
import org.springframework.scheduling.config.ScheduledTask;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by weiqinxiao on 2019/3/15.
 */
@Slf4j
@Service
public class ScheduleManager implements SchedulingConfigurer {
    private ScheduledTaskRegistrar taskRegistrar;

    @Autowired
    RoomProperties roomProperties;

    @Autowired
    IMHelper imHelper;

    @Autowired
    RoomMemberDao roomMemberDao;

    private ConcurrentHashMap<String, ScheduledTask> schedulingTasks = new ConcurrentHashMap<>();

    @Override
    public void configureTasks(ScheduledTaskRegistrar scheduledTaskRegistrar) {
        this.taskRegistrar = scheduledTaskRegistrar;
        log.info("init schedule configureTasks: ttl = {}", roomProperties.getDelayTtl());
    }

    public void addExpiredTask(String roomId, RoomService service) {
        log.info("addExpiredTask: {}", roomId);
        ScheduledTask task = taskRegistrar.scheduleFixedDelayTask(new FixedDelayTask(new Runnable() {
            @Override
            public void run() {
                ScheduledTask t = schedulingTasks.remove(roomId);
                t.cancel();
                try {
                    service.destroyRoom(roomId);
                } catch (Exception e) {
                    log.error("addExpiredTask exception: {}", e.getMessage());
                }
            }
        }, roomProperties.getDelayTtl() * 10, roomProperties.getDelayTtl()));

        schedulingTasks.put(roomId, task);
    }
}
