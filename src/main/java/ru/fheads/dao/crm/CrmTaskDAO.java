package ru.fheads.dao.crm;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import ru.fheads.dao.TaskDAO;
import ru.fheads.entities.Task;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.ArrayList;
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
                            "   ''                                                                                                           AS client, " +
                            "   CONCAT(vtiger_users.last_name, ' ', vtiger_users.first_name)                                                 AS creatorName, " +
                            "   CONCAT(vtiger_users.last_name, ' ', vtiger_users.first_name)                                                 AS executorName, " +
                            "   vtiger_activity.subject                                                                                      AS description, " +
                            "   STR_TO_DATE(CONCAT(vtiger_activity.date_start, ' ', vtiger_activity.time_start), '%Y-%m-%d %H:%i:%s')        AS lastChangedDateTime, " +
                            "   vtiger_activity.due_date                                                                                     AS plannedEndDateTime, " +
                            "   vtiger_crmentity.description                                                                                 AS lastComment, " +
                            "   vtiger_taskpriority.taskpriorityid                                                                           AS priority, " +
                            "   vtiger_activity.status                                                                                       AS status, " +
                            "   FALSE                                                                                                        AS isDragged, " +
                            "   FALSE                                                                                                        AS isExpired, " +
                            "   FALSE                                                                                                        AS priorityChanged, " +
                            "   CONCAT('http://crm.f-heads.com/index.php?module=Potentials&view=Detail&record=', vtiger_activity.activityid) AS href " +
                            "FROM vtiger_activity " +
                            "   INNER JOIN vtiger_taskpriority " +
                            "       ON vtiger_activity.priority = vtiger_taskpriority.taskpriority " +
                            "   INNER JOIN vtiger_salesmanactivityrel " +
                            "       ON vtiger_activity.activityid = vtiger_salesmanactivityrel.activityid " +
                            "   INNER JOIN vtiger_users " +
                            "       ON vtiger_salesmanactivityrel.smid = vtiger_users.id " +
                            "   INNER JOIN vtiger_crmentity " +
                            "       ON vtiger_activity.activityid = vtiger_crmentity.crmid " +
                            "WHERE vtiger_activity.activitytype = 'Task' " +
                            "   AND vtiger_activity.status NOT IN ('Completed', 'Завершено', 'Deffered');"
                    , Task.class)
                    .getResultList();
        } finally {
            entityManager.close();
        }
    }
}
