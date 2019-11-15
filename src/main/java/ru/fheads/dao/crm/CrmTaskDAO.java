package ru.fheads.dao.crm;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import ru.fheads.dao.TaskDAO;
import ru.fheads.entities.Task;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.List;

@Repository
public class CrmTaskDAO implements TaskDAO {

    private final EntityManagerFactory crmEntityManagerFactory;

    public CrmTaskDAO(@Qualifier("crmEntityManagerFactory") EntityManagerFactory crmEntityManagerFactory) {
        this.crmEntityManagerFactory = crmEntityManagerFactory;
    }

    @Override
    @SuppressWarnings("all")
    public List<Task> getTasks() {
        EntityManager entityManager = crmEntityManagerFactory.createEntityManager();
        try {
            return entityManager.createNativeQuery(
                    "SELECT vtiger_activity.activityid                                                                                    AS id, " +
                            "   'CRM'                                                                                                        AS src, " +
                            "   STR_TO_DATE(CONCAT(vtiger_activity.date_start, ' ', vtiger_activity.time_start), '%Y-%m-%d %H:%i:%s')        AS creationDateTime, " +
                            "   (CASE WHEN (SELECT COUNT(*) FROM vtiger_seactivityrel " +
                            "   WHERE activityid = vtiger_activity.activityid) = 1 THEN labels.label ELSE '' END)                                    AS client, " +
                            "   CONCAT(vtiger_users.last_name, ' ', vtiger_users.first_name)                                                 AS creatorName, " +
                            "   CONCAT(vtiger_users.last_name, ' ', vtiger_users.first_name)                                                 AS executorName, " +
                            "   vtiger_activity.subject                                                                                      AS description, " +
                            "   STR_TO_DATE(CONCAT(vtiger_activity.date_start, ' ', vtiger_activity.time_start), '%Y-%m-%d %H:%i:%s')        AS lastChangedDateTime, " +
                            "   vtiger_activity.due_date                                                                                     AS plannedEndDateTime, " +
                            "   descriptions.description                                                                                     AS lastComment, " +
                            "   (CASE WHEN vtiger_activity.priority = '' OR vtiger_activity.priority IS NULL " +
                            "   THEN 3 ELSE vtiger_taskpriority.taskpriorityid END)                                                          AS priority, " +
                            "   vtiger_activity.status                                                                                       AS status, " +
                            "   FALSE                                                                                                        AS isDragged, " +
                            "   FALSE                                                                                                        AS isExpired, " +
                            "   FALSE                                                                                                        AS priorityChanged, " +
                            "   CONCAT('http://crm.f-heads.com/index.php?module=Calendar&view=Detail&record=', vtiger_activity.activityid)   AS href " +
                            "FROM vtiger_activity " +
                            "   LEFT JOIN vtiger_taskpriority " +
                            "       ON vtiger_activity.priority = vtiger_taskpriority.taskpriority " +
                            "   INNER JOIN vtiger_salesmanactivityrel " +
                            "       ON vtiger_activity.activityid = vtiger_salesmanactivityrel.activityid " +
                            "   INNER JOIN vtiger_users " +
                            "       ON vtiger_salesmanactivityrel.smid = vtiger_users.id " +
                            "   INNER JOIN vtiger_crmentity AS descriptions " +
                            "       ON vtiger_activity.activityid = descriptions.crmid " +
                            "   LEFT JOIN vtiger_seactivityrel " +
                            "       ON vtiger_activity.activityid = vtiger_seactivityrel.activityid " +
                            "   LEFT JOIN vtiger_crmentity AS labels " +
                            "       ON vtiger_seactivityrel.crmid = labels.crmid " +
                            "WHERE vtiger_activity.activitytype = 'Task' " +
                            "   AND descriptions.deleted != 1 " +
                            "   AND vtiger_activity.status NOT IN ('Completed', 'Завершено');"
                    , Task.class)
                    .getResultList();
        } finally {
            entityManager.close();
        }
    }
}
