package com.mmorpg.mbdl.framework.thread.task;

import java.io.Serializable;

/**
 * 对任务队列而言同步执行的任务
 */
public abstract class Task extends AbstractTask {
    public Task(Serializable dispatcherId) {
        super(dispatcherId);
    }

    @Override
    public TaskType taskType() {
        return TaskType.TASK;
    }

}
