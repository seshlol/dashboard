package ru.fheads.dao.redmine;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import ru.fheads.dao.TaskDAO;
import ru.fheads.entities.Task;

import javax.persistence.EntityManagerFactory;
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
        return redmineEntityManagerFactory
                .createEntityManager()
                .createNativeQuery(
                        "SELECT issues.id                                                 AS id, " +
                                "       'Redmine'                                            AS src, " +
                                "       issues.created_on                                    AS creationDateTime, " +
                                "       projects.name                                        AS client, " +
                                "       CONCAT(creators.lastname, ' ', creators.firstname)   AS creatorName, " +
                                "       CONCAT(executors.lastname, ' ', executors.firstname) AS executorName, " +
                                "       issues.subject                                       AS description, " +
                                "       issues.updated_on                                    AS lastChangedDateTime, " +
                                "       issues.priority_id                                   AS priority, " +
                                "       issue_statuses.name                                  AS status, " +
                                "       FALSE                                                AS isDragged, " +
                                "       FALSE                                                AS isAlmostExpired, " +
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
                                "WHERE issues.status_id != 5 " +
                                "  AND issues.status_id != 6;"
                        , Task.class)
                .getResultList();
    }
}
