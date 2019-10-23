package ru.fheads.dao.dashboard;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.fheads.entities.SavedTask;

@Repository
public interface SavedTaskRepository extends CrudRepository<SavedTask, String> {
}
