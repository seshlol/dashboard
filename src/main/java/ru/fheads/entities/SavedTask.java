package ru.fheads.entities;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Data
public class SavedTask {

    @Id
    private String compositeId;

    private Byte priority;

    private Boolean isDragged;
}
