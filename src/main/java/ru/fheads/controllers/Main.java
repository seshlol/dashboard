package ru.fheads.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ru.fheads.dao.TaskDAO;
import ru.fheads.entities.SavedTask;
import ru.fheads.entities.Task;
import ru.fheads.dao.dashboard.SavedTaskRepository;
import ru.fheads.services.TaskService;

import java.util.*;
import java.util.concurrent.*;

@Slf4j
@Controller
public class Main {

    private final List<TaskDAO> taskDAOS;
    private final SavedTaskRepository savedTaskRepository;
    private final TaskService taskService;
    private final ExecutorService executorService;


    @Autowired
    public Main(List<TaskDAO> taskDAOS, SavedTaskRepository savedTaskRepository, TaskService taskService, ExecutorService executorService) {
        this.taskDAOS = taskDAOS;
        this.taskService = taskService;
        this.savedTaskRepository = savedTaskRepository;
        this.executorService = executorService;
    }

    @GetMapping(value = "/")
    public String home() {
        return "home";
    }

    @ResponseBody
    @GetMapping(value = "/getData")
    public Map<String, Object> getTasks(@RequestParam String executorName, @RequestParam String status) {
        List<Task> queriedList = new ArrayList<>();
        List<Task> resultList = new ArrayList<>();
        List<Task> freshList = new ArrayList<>();
        Map<String, Object> data = new HashMap<>();

        taskService.getTasks(queriedList, taskDAOS, executorService);
        queriedList = taskService.setGeneralProperties(queriedList);

        taskService.fillSelectorsList(data, queriedList);

        queriedList = taskService.filter(queriedList, executorName, status);
        queriedList = taskService.sortByPriorityThenByCreationDate(queriedList);

        List<SavedTask> savedList = savedTaskRepository.findAllByExecutorNameAndStatusOrderByPosition(executorName, status);
        if (savedList.isEmpty()) {
            resultList = queriedList;
        } else {
            taskService.restoreSavedOrder(queriedList, savedList, resultList, freshList);
            taskService.fillWithNewTasks(queriedList, savedList, freshList);
            taskService.insertIntoByPriority(resultList, freshList);
        }
        data.put("resultList", resultList);
        return data;
    }

    @ResponseBody
    @PostMapping(value = "/changeOrder")
    public void changeOrder(@RequestParam String executorName, @RequestParam String status,
                            @RequestBody List<SavedTask> savedTaskList) {
        savedTaskRepository.deleteByExecutorNameAndStatus(executorName, status);
        savedTaskRepository.saveAll(savedTaskList);
    }
}
