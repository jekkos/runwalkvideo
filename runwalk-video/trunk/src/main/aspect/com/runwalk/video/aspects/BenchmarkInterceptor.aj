package com.runwalk.video.aspects;

import java.util.concurrent.TimeUnit;
import org.jdesktop.application.Task;
import org.slf4j.LoggerFactory;

public aspect BenchmarkInterceptor {

    after() returning : execution(* com.runwalk.video.tasks.AbstractTask.doInBackground()) {
    	Task<?, ?> task = (Task<?, ?>) thisJoinPoint.getTarget();
    	if (!task.getClass().isAnonymousClass()) {
    		long ms = task.getExecutionDuration(TimeUnit.MILLISECONDS);
    		long s = task.getExecutionDuration(TimeUnit.SECONDS);
    		String duration = ms > 10000 ? s + "s" : ms + "ms";
    		LoggerFactory.getLogger(BenchmarkInterceptor.class).debug(task.getClass().getSimpleName() + " finished in " + duration);
    	}
     }
	
}
