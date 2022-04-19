package ru.incomeandexpenses.models;

import lombok.Data;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@Table(name = "income")
public class Income extends Operation{

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "value")
    private int value;

    @Column(name = "author")
    private UUID authorId;

    @Column(name = "client")
    private UUID clientId;

    @Column(name = "date")
    private LocalDateTime date;

    @Column(name = "purpose")
    private String purpose;
}
