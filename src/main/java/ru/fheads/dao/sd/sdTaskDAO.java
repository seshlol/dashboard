package ru.fheads.dao.sd;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import ru.fheads.dao.TaskDAO;
import ru.fheads.entities.Task;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.ArrayList;
import java.util.List;

@Repository
public class sdTaskDAO implements TaskDAO {

    private final EntityManagerFactory sdEntityManagerFactory;

    public sdTaskDAO(@Qualifier("sdEntityManagerFactory") EntityManagerFactory sdEntityManagerFactory) {
        this.sdEntityManagerFactory = sdEntityManagerFactory;
    }

    @Override
    @SuppressWarnings("all")
    public List<Task> getTasks() {
        EntityManager entityManager = sdEntityManagerFactory.createEntityManager();
        try {
            return entityManager.createNativeQuery(
                    "SELECT sd_ticket.id               AS id, " +
                            "       'Service Desk'        AS src, " +
                            "       sd_ticket.fd          AS creationDateTime, " +
                            "       sd_client.name        AS client, " +
                            "       sd_worker.name        AS creatorName, " +
                            "       sd_specialist.name    AS executorName, " +
                            "       sd_ticket.txt         AS description, " +
                            "       sd_ticket.sd          AS lastChangedDateTime, " +
                            "       sd_ticket.priority_id AS priority, " +
                            "       sd_status.name        AS status, " +
                            "       FALSE                 AS isDragged, " +
                            "       FALSE                 AS isAlmostExpired, " +
                            "       FALSE                 AS priorityChanged, " +
                            "       CONCAT('http://sd.f-heads.ru/tickets/', sd_ticket.id) AS href " +
                            "FROM sd_ticket " +
                            "         INNER JOIN sd_client " +
                            "                    ON sd_ticket.client_id = sd_client.id " +
                            "         INNER JOIN sd_worker " +
                            "                    ON sd_ticket.worker_id = sd_worker.id " +
                            "         INNER JOIN sd_specialist " +
                            "                    ON sd_ticket.specialist_id = sd_specialist.id " +
                            "         INNER JOIN sd_status " +
                            "                    ON sd_ticket.status_id = sd_status.id " +
                            "WHERE sd_ticket.status_id != 3 " +
                            "  AND sd_ticket.status_id != 4 " +
                            "  AND sd_ticket.status_id != 6;"
                    , Task.class)
                    .getResultList();
        } finally {
            entityManager.close();
        }
    }
}
