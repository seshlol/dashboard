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

@Slf4j
@Controller
public class Main {

    private final TaskDAO redmineTaskDAO;
    private final TaskDAO crmTaskDAO;
    private final TaskDAO sdTaskDAO;
    private final TaskService taskService;
    private final SavedTaskRepository savedTaskRepository;

    @Autowired
    public Main(TaskDAO redmineTaskDAO, TaskDAO crmTaskDAO, TaskDAO sdTaskDAO, TaskService taskService, SavedTaskRepository savedTaskRepository, SavedTaskRepository savedTaskRepository1) {
        this.redmineTaskDAO = redmineTaskDAO;
        this.crmTaskDAO = crmTaskDAO;
        this.sdTaskDAO = sdTaskDAO;
        this.taskService = taskService;
        this.savedTaskRepository = savedTaskRepository1;
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

        //todo add async
        queriedList.addAll(taskService.setRedmineProperPriority(redmineTaskDAO.getActiveTasks()));
        queriedList.addAll(taskService.setSdProperPriority(sdTaskDAO.getActiveTasks()));
        queriedList.addAll(taskService.setCrmProperPriority(crmTaskDAO.getActiveTasks()));
        queriedList = taskService.sortByPriorityThenByCreationDate(queriedList);

        List<SavedTask> savedTaskList = (List<SavedTask>) savedTaskRepository.findAll();
        taskService.restoreSavedOrder(queriedList, savedTaskList, resultList, freshList);
        taskService.fillWithNewTasks(queriedList, savedTaskList, freshList);
        taskService.insertIntoByPriority(resultList, freshList);

        return resultList;
    }

    @ResponseBody
    @PostMapping(value = "/changeOrder")
    public void changeOrder(@RequestBody List<SavedTask> savedTaskList) {
        savedTaskRepository.deleteAll();
        savedTaskRepository.saveAll(savedTaskList);
    }
}
