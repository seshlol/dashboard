package ru.fheads.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import ru.fheads.dao.TaskDAO;
import ru.fheads.entities.SavedTask;
import ru.fheads.entities.Task;
import ru.fheads.dao.dashboard.SavedTaskRepository;
import ru.fheads.services.TaskService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

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
    @GetMapping(value = "/getTasks")
    public List<Task> getTasks() {
        List<Task> queriedList = new ArrayList<>();
        List<Task> resultList = new ArrayList<>();
        List<Task> freshList = new ArrayList<>();

        List<Future<List<Task>>> futures = taskDAOS.stream()
                .map(dao -> executorService.submit(dao::getTasks))
                .collect(Collectors.toList());
        for (Future<List<Task>> future : futures){
            try {
                queriedList.addAll(future.get(3, TimeUnit.SECONDS));
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                e.printStackTrace();
                return null;
            }
        }

        queriedList = taskService.setGeneralProperties(queriedList);
        queriedList = taskService.sortByPriorityThenByCreationDate(queriedList);

        List<SavedTask> savedList = (List<SavedTask>) savedTaskRepository.findAll();
        if (savedList.isEmpty()) {
            return queriedList;
        } else {
            taskService.restoreSavedOrder(queriedList, savedList, resultList, freshList);
            taskService.fillWithNewTasks(queriedList, savedList, freshList);
            taskService.insertIntoByPriority(resultList, freshList);
            return resultList;
        }
    }

    @ResponseBody
    @PostMapping(value = "/changeOrder")
    public void changeOrder(@RequestBody List<SavedTask> savedTaskList) {
        savedTaskRepository.deleteAll();
        savedTaskRepository.saveAll(savedTaskList);
    }
}
