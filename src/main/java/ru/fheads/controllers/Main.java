package ru.fheads.controllers;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.mapping.Collection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import ru.fheads.dao.TaskDAO;
import ru.fheads.entities.SavedTask;
import ru.fheads.entities.Task;
import ru.fheads.dao.dashboard.SavedTaskRepository;
import ru.fheads.services.TaskService;

import java.util.*;
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
    @GetMapping(value = "/getData")
    public Map<String, Object> getTasks(@RequestParam String executorName, @RequestParam String status) {
        List<Task> queriedList = new ArrayList<>();
        List<Task> resultList = new ArrayList<>();
        List<Task> freshList = new ArrayList<>();

        List<Future<List<Task>>> futures = taskDAOS.stream()
                .map(dao -> executorService.submit(dao::getTasks))
                .collect(Collectors.toList());
        for (Future<List<Task>> future : futures){
            try {
                queriedList.addAll(future.get(2, TimeUnit.SECONDS));
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                e.printStackTrace();
                return null;
            }
        }

        queriedList = taskService.setGeneralProperties(queriedList);

        Map<String, Integer> executorOptMap = new HashMap<>();
        Map<String, Integer> statusOptMap = new HashMap<>();
        queriedList.forEach(t -> {
            String name = t.getExecutorName();
            String stat = t.getStatus();
            if (executorOptMap.containsKey(name)) {
                executorOptMap.put(name, executorOptMap.get(name) + 1);
            } else {
                executorOptMap.put(name, 1);
            }
            if (statusOptMap.containsKey(stat)) {
                statusOptMap.put(stat, statusOptMap.get(stat) + 1);
            } else {
                statusOptMap.put(stat, 1);
            }
        });
        List<String> executorOptList = new ArrayList<>();
        for (Map.Entry<String, Integer> e : executorOptMap.entrySet()) {
            executorOptList.add(e.getKey() + " (" + e.getValue() + ")");
        }
        List<String> statusOptList = new ArrayList<>();
        for (Map.Entry<String, Integer> e : statusOptMap.entrySet()) {
            statusOptList.add(e.getKey() + " (" + e.getValue() + ")");
        }
        Collections.sort(executorOptList);
        executorOptList.add(0, "Любой (" + queriedList.size() + ")");
        Collections.sort(statusOptList);
        statusOptList.add(0, "Любой (" + queriedList.size() + ")");

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

        Map<String, Object> data = new HashMap<>();
        data.put("resultList", resultList);
        data.put("executorOptList", executorOptList);
        data.put("statusOptList", statusOptList);
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
