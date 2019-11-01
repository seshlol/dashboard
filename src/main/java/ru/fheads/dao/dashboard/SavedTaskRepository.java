package ru.fheads.dao.dashboard;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.fheads.entities.SavedTask;

@Repository
public interface SavedTaskRepository extends CrudRepository<SavedTask, String> {

    @Transactional
    @Modifying
    @Query(value = "DELETE FROM SavedTask st WHERE st.executorName = ?1 AND st.status = ?2")
    void deleteByExecutorNameAndStatus(String executorName, String status);
}
