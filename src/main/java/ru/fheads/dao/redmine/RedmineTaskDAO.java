package ru.fheads.dao.redmine;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import ru.fheads.dao.TaskDAO;
import ru.fheads.entities.Task;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.ArrayList;
import java.util.List;

@Repository
public class RedmineTaskDAO implements TaskDAO {

    private final EntityManagerFactory redmineEntityManagerFactory;

    public RedmineTaskDAO(@Qualifier("redmineEntityManagerFactory") EntityManagerFactory redmineEntityManagerFactory) {
        this.redmineEntityManagerFactory = redmineEntityManagerFactory;
    }

    @Override
    @SuppressWarnings("all")
    public List<Task> getTasks() {
        EntityManager entityManager = redmineEntityManagerFactory.createEntityManager();
        try {
            return entityManager.createNativeQuery(
                    "SELECT issues.id                                                 AS id, " +
                            "       'Redmine'                                            AS src, " +
                            "       issues.created_on                                    AS creationDateTime, " +
                            "       projects.name                                        AS client, " +
                            "       CONCAT(creators.lastname, ' ', creators.firstname)   AS creatorName, " +
                            "       CONCAT(executors.lastname, ' ', executors.firstname) AS executorName, " +
                            "       issues.subject                                       AS description, " +
                            "       issues.updated_on                                    AS lastChangedDateTime, " +
                            "       custom_values.value                                  AS plannedEndDateTime, " +
                            "       (SELECT notes FROM journals" +
                            "           WHERE journalized_id = issues.id " +
                            "           AND notes != '' AND notes IS NOT NULL " +
                            "           ORDER BY created_on DESC LIMIT 1) AS lastComment, " +
                            "       issues.priority_id                                   AS priority, " +
                            "       issue_statuses.name                                  AS status, " +
                            "       FALSE                                                AS isDragged, " +
                            "       FALSE                                                AS isExpired, " +
                            "       FALSE                                                AS priorityChanged, " +
                            "       CONCAT('https://redmine.f-heads.com/issues/', issues.id) AS href " +
                            "FROM issues " +
                            "         INNER JOIN projects " +
                            "                    ON issues.project_id = projects.id " +
                            "         INNER JOIN users as creators " +
                            "                    ON issues.author_id = creators.id " +
                            "         INNER JOIN users as executors " +
                            "                    ON issues.assigned_to_id = executors.id " +
                            "         INNER JOIN issue_statuses " +
                            "                    ON issues.status_id = issue_statuses.id " +
                            "         INNER JOIN custom_values" +
                            "                    ON custom_values.customized_id = issues.id " +
                            "WHERE issues.status_id != 5 " +
                            "  AND issues.status_id != 6;"
                    , Task.class)
                    .getResultList();
        } finally {
            entityManager.close();
        }
    }
}
