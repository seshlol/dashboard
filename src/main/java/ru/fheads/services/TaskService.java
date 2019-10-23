package ru.fheads.services;

import org.springframework.stereotype.Service;
import ru.fheads.entities.SavedTask;
import ru.fheads.entities.Task;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskService {

    private static final Comparator<Task> BY_PRIORITY_THEN_BY_DATE = Comparator.comparing(Task::getPriority)
            .thenComparing(Task::getCreationDateTime, Comparator.reverseOrder());

    public List<Task> sortByPriorityThenByCreationDate(List<Task> list) {
        return list.stream()
                .sorted(BY_PRIORITY_THEN_BY_DATE)
                .collect(Collectors.toList());
    }

    public List<Task> setSdProperPriority(List<Task> list) {
        return list.stream()
                .peek(t -> {
                    switch (t.getPriority()) {
                        case 1: {
                            t.setPriority((byte) 4);
                            break;
                        }
                        case 3: {
                            t.setPriority((byte) 1);
                            break;
                        }
                    }
                }).collect(Collectors.toList());
    }

    public List<Task> setRedmineProperPriority(List<Task> list) {
        return list.stream()
                .peek(t -> {
                    switch (t.getPriority()) {
                        case 3: {
                            t.setPriority((byte) 2);
                            break;
                        }
                        case 4: {
                            t.setPriority((byte) 3);
                            break;
                        }
                        case 5: {
                            t.setPriority((byte) 4);
                            break;
                        }
                    }
                }).collect(Collectors.toList());
    }

    public List<Task> setCrmProperPriority(List<Task> list) {
        return list.stream()
                .peek(t -> {
                    switch (t.getPriority()) {
                        case 2: {
                            t.setPriority((byte) 3);
                            break;
                        }
                        case 3: {
                            t.setPriority((byte) 4);
                            break;
                        }
                    }
                }).collect(Collectors.toList());
    }

    private String getCompositeId(Task task) {
        return task.getId() + "-" + task.getSrc();
    }

    public void restoreSavedOrder(List<Task> queriedList, List<SavedTask> savedTaskList,
                                  List<Task> resultList, List<Task> freshList) {
        for (SavedTask savedTask : savedTaskList) {
            for (Task queriedTask : queriedList) {
                if (savedTask.getCompositeId().equals(getCompositeId(queriedTask))) {
                    if (savedTask.getIsDragged()) {
                        queriedTask.setIsDragged(true);
                        if (queriedTask.getPriority() != savedTask.getPriority()) {
                            queriedTask.setPriorityChanged(true);
                        }
                    } else {
                        if (queriedTask.getPriority() != savedTask.getPriority()) {
                            freshList.add(queriedTask);
                            break;
                        }
                    }
                    resultList.add(queriedTask);
                    break;
                }
            }
        }
    }

    public void fillWithNewTasks(List<Task> queriedList, List<SavedTask> savedTaskList, List<Task> freshList) {
        for (Task task : queriedList) {
            boolean found = false;
            for (SavedTask savedTask : savedTaskList) {
                if (getCompositeId(task).equals(savedTask.getCompositeId())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                freshList.add(task);
            }
        }
    }

    public void insertIntoByPriority(List<Task> resultList, List<Task> freshList) {
        for (byte i = 1; i <= 4; i++) {
            byte finalI = i;
            List<Task> priorityTaskList = freshList.stream()
                    .filter(freshTask -> freshTask.getPriority() == finalI)
                    .collect(Collectors.toList());
            if (!priorityTaskList.isEmpty()) {
                int position = resultList.size();
                for (Task task : resultList) {
                    if (!task.getIsDragged() && task.getPriority() >= finalI) {
                        position = resultList.indexOf(task);
                        break;
                    }
                }
                for (int j = priorityTaskList.size() - 1; j >= 0; j--) {
                    resultList.add(position, priorityTaskList.get(j));
                }
            }
        }
    }
}
