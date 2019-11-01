package ru.fheads.entities;

import lombok.Data;
import org.hibernate.annotations.GeneratorType;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.net.Proxy;

@Entity
@Data
public class SavedTask {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String compositeId;

    private Byte priority;

    private Boolean isDragged;

    private Integer position;

    private String executorName;

    private String status;
}
