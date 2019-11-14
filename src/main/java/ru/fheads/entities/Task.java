package ru.fheads.entities;

import lombok.Data;
import org.hibernate.annotations.Type;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

@Entity
@Data
public class Task {

    @Id
    private Long id;

    private String src;

    @Temporal(TemporalType.TIMESTAMP)
    private Date creationDateTime;

    private String client;

    private String creatorName;

    private String executorName;

    private String description;

    @Temporal(TemporalType.TIMESTAMP)
    private Date lastChangedDateTime;

    @Temporal(TemporalType.TIMESTAMP)
    private Date plannedEndDateTime;

    private String lastComment;

    private Byte priority;

    private String status;

    private Boolean isDragged;

    private Boolean isExpired;

    private Boolean priorityChanged;

    private String href;
}
