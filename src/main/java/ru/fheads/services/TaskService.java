package ru.fheads.services;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import ru.fheads.entities.SavedTask;
import ru.fheads.entities.Task;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskService {

    private static final String STATUS_NEW = "Новая";
    private static final String STATUS_IN_WORK = "В работе";
    private static final String STATUS_UNDER_CONSIDERATION = "На рассмотрении";
    private static final String STATUS_FOR_REVISION = "На доработку";
    private static final String STATUS_PENDING = "В ожидании";
    private static final String STATUS_PLANNED = "Запланирована";

    private static final Comparator<Task> BY_PRIORITY_THEN_BY_DATE = Comparator.comparing(Task::getPriority)
            .thenComparing(Task::getCreationDateTime, Comparator.reverseOrder());

    public List<Task> sortByPriorityThenByCreationDate(List<Task> list) {
        return list.stream()
                .sorted(BY_PRIORITY_THEN_BY_DATE)
                .collect(Collectors.toList());
    }

    private void setSdProperPriority(Task task) {
        switch (task.getPriority()) {
            case 1: {
                task.setPriority((byte) 4);
                break;
            }
            case 3: {
                task.setPriority((byte) 1);
                break;
            }
        }
        switch (task.getStatus()) {
            case "ожидает обработки": {
                task.setStatus(STATUS_NEW);
                break;
            }
            case "в процессе выполнения": {
                task.setStatus(STATUS_IN_WORK);
                break;
            }
            case "на рассмотрении": {
                task.setStatus(STATUS_UNDER_CONSIDERATION);
                break;
            }
            case "требует доработки": {
                task.setStatus(STATUS_FOR_REVISION);
                break;
            }
            case "в ожидании": {
                task.setStatus(STATUS_PENDING);
                break;
            }
        }
    }

    private void setRedmineProperPriority(Task task) {
        switch (task.getPriority()) {
            case 3: {
                task.setPriority((byte) 2);
                break;
            }
            case 4: {
                task.setPriority((byte) 3);
                break;
            }
            case 5: {
                task.setPriority((byte) 4);
                break;
            }
        }
        switch (task.getStatus()) {
            case "Ожидает обработки": {
                task.setStatus(STATUS_NEW);
                break;
            }
            case "Запланирована": {
                task.setStatus(STATUS_PLANNED);
                break;
            }
            case "Приостановлена": {
                task.setStatus(STATUS_PENDING);
                break;
            }
        }
    }

    private void setCrmProperPriority(Task task) {
        switch (task.getPriority()) {
            case 2: {
                task.setPriority((byte) 3);
                break;
            }
            case 3: {
                task.setPriority((byte) 4);
                break;
            }
        }
        switch (task.getStatus()) {
            case "Deferred":
            case "Pending Input":
            case "В ожидании": {
                task.setStatus(STATUS_PENDING);
                break;
            }
            case "In Progress": {
                task.setStatus(STATUS_IN_WORK);
                break;
            }
            case "Not Started": {
                task.setStatus(STATUS_NEW);
                break;
            }
            case "Planned": {
                task.setStatus(STATUS_PLANNED);
                break;
            }
        }
    }

    private String getCompositeId(Task task) {
        return task.getId() + "-" + task.getSrc();
    }

    public void restoreSavedOrder(List<Task> queriedList, List<SavedTask> savedList,
                                  List<Task> resultList, List<Task> freshList) {
        for (SavedTask savedTask : savedList) {
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

    public void fillWithNewTasks(List<Task> queriedList, List<SavedTask> savedList, List<Task> freshList) {
        for (Task task : queriedList) {
            boolean found = false;
            for (SavedTask savedTask : savedList) {
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

    public List<Task> setGeneralProperties(List<Task> list) {
        return list.stream()
                .peek(t -> {
                    switch (t.getSrc()) {
                        case "Service Desk": {
                            setSdProperPriority(t);
                            break;
                        }
                        case "Redmine": {
                            setRedmineProperPriority(t);
                            break;
                        }
                        case "CRM": {
                            setCrmProperPriority(t);
                            break;
                        }
                    }
                }).collect(Collectors.toList());
    }

    public List<Task> filter(List<Task> list, String executorName, String status) {
        if (!"Any".equals(executorName)) {
            list = list.stream()
                    .filter(t -> t.getExecutorName().equals(executorName))
                    .collect(Collectors.toList());
        }
        if (!"Any".equals(status)) {
            list = list.stream()
                    .filter(t -> t.getStatus().equals(status))
                    .collect(Collectors.toList());
        }
        return list;
    }
}
