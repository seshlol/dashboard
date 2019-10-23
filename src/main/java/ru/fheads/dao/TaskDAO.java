package ru.fheads.dao;

import ru.fheads.entities.Task;

import java.util.List;

public interface TaskDAO {

    List<Task> getActiveTasks();
}
