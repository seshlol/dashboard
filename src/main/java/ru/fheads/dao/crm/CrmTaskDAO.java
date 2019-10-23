package ru.fheads.dao.crm;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import ru.fheads.dao.TaskDAO;
import ru.fheads.entities.Task;

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
    public List<Task> getActiveTasks() {
        return crmEntityManagerFactory
                .createEntityManager()
                .createNativeQuery(
                        "SELECT vtiger_activity.activityid                                                                                 AS id, " +
                                "       'CRM'                                                                                                 AS src, " +
                                "       STR_TO_DATE(CONCAT(vtiger_activity.date_start, ' ', vtiger_activity.time_start), '%Y-%m-%d %H:%i:%s') AS creationDateTime, " +
                                "       vtiger_activity.activitytype                                                                          AS client, " +
                                "       ''                                                                                                    AS creatorName, " +
                                "       ''                                                                                                    AS executorName, " +
                                "       vtiger_activity.subject                                                                               AS description, " +
                                "       ''                                                                                                    AS lastChangedDateTime, " +
                                "       vtiger_taskpriority.taskpriorityid                                                                    AS priority, " +
                                "       vtiger_activity.status                                                                                AS status, " +
                                "       FALSE                                                                                                 AS isDragged, " +
                                "       FALSE                                                                                                 AS isAlmostExpired, " +
                                "       FALSE                                                                                                 AS priorityChanged " +
                                "FROM vtiger_activity " +
                                "         JOIN vtiger_taskpriority " +
                                "                    ON vtiger_activity.priority = vtiger_taskpriority.taskpriority " +
                                "WHERE vtiger_activity.status != 'Completed' " +
                                "  AND vtiger_activity.status != 'Завершено' " +
                                "  AND vtiger_activity.status IS NOT NULL;"
                        , Task.class)
                .getResultList();
    }
}
