package org.dmc.services.tasks;

import org.dmc.services.Id;
import org.dmc.services.ServiceLogger;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;

@RestController
public class TaskController {
	
	private final String logTag = "TASK_CONTROLLER";
	private TaskDao task = new TaskDao();
	
	@RequestMapping(value = "tasks/{taskID}", method = RequestMethod.GET)
	public Task getTask(@PathVariable("taskID") String taskID, @RequestHeader(value="AJP_eppn", defaultValue="testUser") String userEPPN) {
		ServiceLogger.log(logTag, "UserName: " + userEPPN);
		return task.getTask(taskID);
	}
	
    @RequestMapping(value = "/tasks/create", method = RequestMethod.POST, headers = {"Content-type=text/plain"})
    @ResponseBody
    public Id createTask(@RequestBody String payload) {
    	ServiceLogger.log(logTag, "Payload: " + payload);
    	
    	return task.createTask(payload);
    }
	@RequestMapping(value = "tasks", method = RequestMethod.GET)
	public ArrayList<Task> getTaskList() {
		return task.getTaskList();
	}
	
	@RequestMapping(value = "/projects/{projectID}/tasks", method = RequestMethod.GET)
	public ArrayList<Task> getTaskList(@PathVariable("projectID") int projectId, @RequestHeader(value="AJP_eppn", defaultValue="testUser") String userEPPN) {
		ServiceLogger.log(logTag, "UserName: " + userEPPN);
		return task.getTaskList(projectId);
	}
}