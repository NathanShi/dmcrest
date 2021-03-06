package org.dmc.services.tasks;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;

import org.json.JSONException;

import org.dmc.services.DBConnector;
import org.dmc.services.DMCError;
import org.dmc.services.DMCServiceException;
import org.dmc.services.Id;
import org.dmc.services.ServiceLogger;
import org.dmc.services.projects.ProjectDao;
import org.dmc.services.data.dao.user.UserDao;

public class TaskDao {
    private static final String logTag = TaskDao.class.getName();

    public TaskDao() {
    }

    public Task getTask(String taskID, String userEPPN) throws DMCServiceException {
        final ResultSet resultSet = queryTask(taskID);
        ArrayList<Task> taskList = createListFromResultSet(resultSet);
        if (null == taskList || taskList.size() <= 0) {
            throw new DMCServiceException(DMCError.BadURL, "Unknown task: " + taskID);
        }
        return taskList.get(0);
    }

    public Id deleteTask(String taskID, String userEPPN) throws DMCServiceException {
        ServiceLogger.log(logTag, "deleteTask; TaskID " + taskID + " User: " + userEPPN);
        Connection connection = DBConnector.connection();
        int rowsAffected;

        ResultSet resultSet = queryTask(taskID);
        if (null == resultSet) {
            throw new DMCServiceException(DMCError.OtherSQLError, "Task ID " + taskID + " does not exist");
        }
        Task task = createListFromResultSet(resultSet).get(0);

        int taskId = Integer.parseInt(taskID);
        int returnedTaskId = Integer.parseInt(task.getId());
        if (taskId != returnedTaskId) {
            ServiceLogger.log(logTag, "deleteTask; Task retrieval error");
            throw new DMCServiceException(DMCError.OtherSQLError, "Task retrieval error");
        }

        try {
            connection.setAutoCommit(false);
            int reporterId = UserDao.getUserID(task.getReporter());
            int assigneeId = UserDao.getUserID(task.getAssignee());

            int userID = UserDao.getUserID(userEPPN);
            if (userID != assigneeId && userID != reporterId) {
                ServiceLogger.log(logTag,
                        "deleteTask; User " + userEPPN + " can not delete requested task. User is not reporter ("
                                + task.getReporter() + ") or assignee (" + task.getAssignee() + ")");
                throw new DMCServiceException(DMCError.UnauthorizedAccessAttempt, "User can not delete requested task");
            } // else the tasks can be deleted

            final String query = "DELETE FROM project_assigned_to WHERE project_task_id = ?";
            PreparedStatement preparedStatement = DBConnector.prepareStatement(query);
            preparedStatement.setInt(1, taskId);
            rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected < 1) {
                connection.rollback();
                throw new DMCServiceException(DMCError.OtherSQLError, "error trying to remove task " + taskID);
            }

            final String deleteQuery = "DELETE FROM project_task WHERE project_task_id = ?";
            PreparedStatement statement = DBConnector.prepareStatement(deleteQuery);
            statement.setInt(1, taskId);
            rowsAffected = statement.executeUpdate();
            if (rowsAffected < 1) {
                connection.rollback();
                throw new DMCServiceException(DMCError.OtherSQLError, "error trying to remove task " + taskID);
            }

            return new Id.IdBuilder(taskId).build();
        } catch (SQLException e) {
            ServiceLogger.log(logTag, "deleteTask; Other SQL Error");
            try {
                connection.rollback();
            } catch (SQLException e1) {
                ServiceLogger.log(logTag, e1.getMessage());
            }
            throw new DMCServiceException(DMCError.OtherSQLError, e.getMessage());
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException se) {
                ServiceLogger.log(logTag, se.getMessage());
            }
        }
    }

    public Task patchTask(String taskIdStr, Task task, String userEPPN) throws DMCServiceException {
        int id = -99999;
        final ProjectDao projectDao = new ProjectDao();
        Connection connection = DBConnector.connection();
        int rowsAffected;

        try {
            connection.setAutoCommit(false);
            int projectId = 0;
            String projectQuery = "SELECT group_project_id FROM project_task WHERE project_task_id = ?";
            PreparedStatement preparedStatementProject = DBConnector.prepareStatement(projectQuery);
            preparedStatementProject.setInt(1, Integer.parseInt(taskIdStr));
            preparedStatementProject.execute();

            ResultSet resultSetProject = preparedStatementProject.getResultSet();
            if (resultSetProject.next()) {
                projectId = resultSetProject.getInt("group_project_id");
            }
            if (!projectDao.hasProjectRole(projectId, userEPPN)) {
                throw new DMCServiceException(DMCError.MemberNotAssignedToProject,
                        "User is not authorized to edit task");
            }

            final Integer taskId = Integer.parseInt(taskIdStr);
            final String title;
            final String description;
            final Integer priority;
            final java.sql.Timestamp endDate;
            final Integer statusId;
            final String query;
            final PreparedStatement preparedStatement;
            if (task.getStatus() != null) {
                statusId = TaskStatus.StatusValues.valueOf(task.getStatus().toUpperCase()).getValue();
                query = "UPDATE project_task SET status_id=? WHERE project_task_id=?";
                preparedStatement = DBConnector.prepareStatement(query);
                preparedStatement.setInt(1, statusId);
                preparedStatement.setInt(2, taskId);

                rowsAffected = preparedStatement.executeUpdate();
                if (rowsAffected < 1) {
                    connection.rollback();
                    throw new DMCServiceException(DMCError.OtherSQLError, "error trying to remove task " + taskIdStr);
                }
                id = taskId.intValue();
            } else {
                title = task.getTitle();
                description = task.getAdditionalDetails();
                priority = task.getPriority();
                endDate = new java.sql.Timestamp(task.getDueDate());
                query = "UPDATE project_task SET summary=?, details=?, priority=?, end_date=? WHERE project_task_id=?";
                preparedStatement = DBConnector.prepareStatement(query);
                preparedStatement.setString(1, title);
                preparedStatement.setString(2, description);
                preparedStatement.setInt(3, priority);
                preparedStatement.setTimestamp(4, endDate);
                preparedStatement.setInt(5, taskId);

                rowsAffected = preparedStatement.executeUpdate();
                if (rowsAffected < 1) {
                    connection.rollback();
                    throw new DMCServiceException(DMCError.OtherSQLError, "error trying to remove task " + taskIdStr);
                }
                id = taskId.intValue();

                assignTask(id, task.getAssigneeId());
            }

            ServiceLogger.log(logTag, "created task ID in insertTask: " + id);
        } catch (SQLException e) {
            ServiceLogger.log(logTag, e.getMessage());
            try {
                connection.rollback();
            } catch (SQLException e1) {
                ServiceLogger.log(logTag, e1.getMessage());
            }
            throw new DMCServiceException(DMCError.OtherSQLError, e.getMessage());
        } catch (JSONException e) {
            ServiceLogger.log(logTag, e.getMessage());
            throw new DMCServiceException(DMCError.OtherSQLError, e.getMessage());
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException se) {
                ServiceLogger.log(logTag, se.getMessage());
            }
        }
        return getTask(Integer.toString(id), userEPPN);
    }

    public Id createTask(TaskToCreate task, String userEPPN) throws DMCServiceException {
        Connection connection = DBConnector.connection();
        // let's start a transaction
        try {
            connection.setAutoCommit(false);
        } catch (SQLException ex) {
            return null;
        }

        try {
            final Id id = insertTask(task, userEPPN);
            if (task.getAssignee() != null) {
                assignTask(id.getId(), task.getAssigneeId());
            }
            return id;
        } catch (DMCServiceException e) {
            ServiceLogger.log(logTag, "got Exception in createTask: " + e.getMessage());
            if (connection != null) {
                try {
                    ServiceLogger.log(logTag, "Transaction Profile Update Rolled back");
                    connection.rollback();
                } catch (SQLException ex) {
                    ServiceLogger.log(logTag, ex.getMessage());
                }
            }
            throw e;
        } finally {
            // let's end the transaction
            if (null != connection) {
                try {
                    connection.setAutoCommit(true);
                } catch (SQLException ex) {
                    // don't really need to do anything
                }
            }
        }
    }

    /*
     * Task Fields id : alphanumeric title : string project : object id title
     * assignee : string reporter : string dueDate : MM/DD/YY priority :
     * 'Today', '<Date>'
     */
    private Id insertTask(TaskToCreate task, String userEPPN) {

        int id = -99999;

        /**
         * Foreign Key Constraints on table project_task: - created_by -> users
         * table - status_id -> table to be identified
         */
        ServiceLogger.log(logTag, "ID in insertTask: " + id);
        try {
            int userID = UserDao.getUserID(userEPPN);
            String title = task.getTitle();
            String description = task.getAdditionalDetails();
            Integer priority = task.getPriority();
            Date startDateTemp = new Date();
            java.sql.Timestamp startDate = new java.sql.Timestamp(startDateTemp.getTime());
            java.sql.Timestamp endDate = new java.sql.Timestamp(task.getDueDate());
            Integer createdBy = userID;
            Integer groupId = Integer.parseInt(task.getProjectId());
            Integer statusId = TaskStatus.StatusValues.valueOf(task.getStatus().toUpperCase()).getValue();

            String query = "INSERT INTO project_task (summary, details, priority, start_date, end_date, created_by, group_project_id, status_id)"
                    + "values ( ?, ?, ?, ?, ?, ?, ?, ?)";

            PreparedStatement preparedStatement = DBConnector.prepareStatement(query);
            preparedStatement.setString(1, title);
            preparedStatement.setString(2, description);
            preparedStatement.setInt(3, priority);
            preparedStatement.setTimestamp(4, startDate);
            preparedStatement.setTimestamp(5, endDate);
            preparedStatement.setInt(6, createdBy);
            preparedStatement.setInt(7, groupId);
            preparedStatement.setInt(8, statusId);
            preparedStatement.executeUpdate();

            query = "select currval('project_task_pk_seq') as id";
            ResultSet resultSet = DBConnector.executeQuery(query);
            while (resultSet.next()) {
                id = resultSet.getInt("id");
            }
            ServiceLogger.log(logTag, "created task ID in insertTask: " + id);
        } catch (SQLException e) {
            ServiceLogger.log(logTag, e.getMessage());
            return null;
        } catch (JSONException e) {
            ServiceLogger.log(logTag, e.getMessage());
            return null;
        }
        return new Id.IdBuilder(id).build();
    }

    public boolean assignTask(Integer taskId, String assigneeIdStr) throws DMCServiceException {
        ServiceLogger.log(logTag, "assign task " + taskId + " to " + assigneeIdStr);
        try {
            int assigneeId = Integer.parseInt(assigneeIdStr);
            if (assigneeId <= 0) {
                ServiceLogger.log(logTag, "assignee " + assigneeIdStr + " not found: " + assigneeId);
                throw new DMCServiceException(DMCError.OtherSQLError, "user " + assigneeIdStr + " not found");
            }
            final String deleteQuery = "DELETE FROM project_assigned_to WHERE project_task_id = ?";
            PreparedStatement statement = DBConnector.prepareStatement(deleteQuery);
            statement.setInt(1, taskId);
            statement.executeUpdate();

            final String query = createAssignTaskQuery();
            PreparedStatement preparedStatement = DBConnector.prepareStatement(query);
            preparedStatement.setInt(1, taskId);
            preparedStatement.setInt(2, assigneeId);
            return preparedStatement.execute();
        } catch (SQLException e) {
            ServiceLogger.log(logTag, "assignee " + assigneeIdStr + " cannot be assigned to task id " + taskId
                    + ", may already be assigned");
            throw new DMCServiceException(DMCError.OtherSQLError, e.getMessage());
        }
    }

    private ResultSet queryAllTasks() {
        final String query = createTaskListQuery(null, null);
        ServiceLogger.log(logTag, "query for all tasks: " + query);
        return DBConnector.executeQuery(query);
    }

    private ResultSet queryTask(String taskID) {
        String query = createTaskListQuery(null, Integer.parseInt(taskID));
        ServiceLogger.log(logTag, "queryTask sql = " + query);
        try {
            PreparedStatement stmt = DBConnector.prepareStatement(query);
            return stmt.executeQuery();
        } catch (SQLException e) {
            ServiceLogger.log(logTag, e.getMessage());
        }
        return null;
    }

    private ResultSet queryTasksForProject(int projectId) {
        final String query = createTaskListQuery(projectId, null);
        ServiceLogger.log(logTag, "query for task list for project: " + query);
        return DBConnector.executeQuery(query);
    }

    /*
     * sample query: select T.project_task_id, 'unknown' as title,
     * T.group_project_id, G.group_name as project_title, x.user_name as
     * assignee, U.user_name as created_by, T.end_date, T.priority, T.summary,
     * T.details, S.status_name as status from project_task T LEFT JOIN ( select
     * * from Users U2, project_assigned_to p, groups G2 where U2.user_id =
     * p.assigned_to_id and G2.group_id = p.project_assigned_id) x on
     * T.project_task_id = x.project_task_id, users U,groups G, project_status S
     * where T.created_by=U.user_id and G.group_id=T.group_project_id and
     * T.status_id = S.status_id
     *
     * for specific project or task - will add to where clause
     *
     * NOTE: need LEFT JOIN for cases where no assignee is specified so that
     * tasks are still returned
     */

    private String createTaskListQuery(Integer projectId, Integer taskId) {
        String query = "select " + "T.project_task_id," + "T.summary as title," + "T.group_project_id,"
                + "G.group_name as project_title," + "x.user_name as assignee, x.realname as assigneeName, " + "U.user_name as created_by, U.realname as createdByName, "
                + "x.assigned_to_id, " + "T.end_date, " + "T.priority, " + "T.details, " + "S.status_name as status "
                + "from project_task T " + "LEFT JOIN (select * from Users U2, project_assigned_to p "
                + "where U2.user_id = p.assigned_to_id) x " + "on T.project_task_id = x.project_task_id, "
                + "users U,groups G, project_status S "
                + "where T.created_by=U.user_id and G.group_id=T.group_project_id " + "and T.status_id = S.status_id ";
        if (null != projectId) {
            query += "and G.group_id = " + projectId + " ";
        }
        if (null != taskId) {
            query += "and T.project_task_id = " + taskId + " ";
        }
        return query;
    }

    private String createAssignTaskQuery() {
        final String query = "insert into project_assigned_to(project_task_id, assigned_to_id) values (?, ?)";
        return query;
    }

    public ArrayList<Task> getTaskList() throws DMCServiceException {
        try {
            ResultSet resultSet = queryAllTasks();
            return createListFromResultSet(resultSet);
        } catch (DMCServiceException e) {
            ServiceLogger.log(logTag, e.getMessage());
            throw new DMCServiceException(DMCError.OtherSQLError, "failed to execute query to get tasks");
        }
    }

    public ArrayList<Task> getTaskList(int projectId) throws DMCServiceException {
        try {
            ResultSet resultSet = queryTasksForProject(projectId);
            return createListFromResultSet(resultSet);
        } catch (DMCServiceException e) {
            ServiceLogger.log(logTag, e.getMessage());
            throw new DMCServiceException(DMCError.OtherSQLError, "failed to execute query to get tasks");
        }
    }

    private ArrayList<Task> createListFromResultSet(ResultSet resultSet) throws DMCServiceException {
        ArrayList<Task> tasks = new ArrayList<Task>();
        ServiceLogger.log(logTag, "in createListFromResultSet: ");
        int countItems = 0;
        try {
            while (resultSet.next()) {
                Task task = readTaskFromResultSet(resultSet);
                if (null != task) {
                    tasks.add(task);
                    countItems++;
                }
            }
            ServiceLogger.log(logTag, "added " + countItems + "items.");
            return tasks;
        } catch (DMCServiceException dmce) {
            throw dmce;
        } catch (SQLException e) {
            ServiceLogger.log(logTag, e.getMessage());
            throw new DMCServiceException(DMCError.OtherSQLError, "unable to process task list query results");
        }
    }

    private Task readTaskFromResultSet(ResultSet resultSet) throws DMCServiceException {
        try {
            Integer id = resultSet.getInt("project_task_id");
            String title = resultSet.getString("title");
            Integer group_project_id = resultSet.getInt("group_project_id");
            String projectTitle = resultSet.getString("project_title");

            String assignee = resultSet.getString("assigneeName");
            int assigneeId = resultSet.getInt("assigned_to_id");
            String assigneeIdStr = null;
            if (assigneeId > 0) {
                assigneeIdStr = Integer.toString(assigneeId);
            }

            String reporter = resultSet.getString("createdByName");
            String reporterEPPN = resultSet.getString("created_by");
            int reporterId = UserDao.getUserID(reporterEPPN);
            String reporterIdStr = null;
            if (reporterId > 0) {
                reporterIdStr = Integer.toString(reporterId);
            }

            String details = resultSet.getString("details");
            Integer priority = resultSet.getInt("priority");

            java.sql.Timestamp dueDate = resultSet.getTimestamp("end_date");
            String status = resultSet.getString("status");

            TaskProject taskProject = new TaskProject(group_project_id, projectTitle);

            Task task = new Task();

            task.setId(id.toString());
            task.setTitle(title);
            task.setProject(taskProject);
            task.setAssignee(assignee);
            task.setAssigneeId(assigneeIdStr);
            task.setReporter(reporter);
            task.setReporterId(reporterIdStr);
            task.setDueDate(dueDate.getTime());
            task.setAdditionalDetails(details);
            task.setStatus(status);
            task.setPriority(priority);

            return task;
        } catch (Exception e) {
            ServiceLogger.log(logTag, "problem reading task resultset: " + e.getMessage());
            throw new DMCServiceException(DMCError.OtherSQLError, "unable to process task result");
        }
    }
}
