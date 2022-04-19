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
@Table(name = "verify")
public class Verify {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "date")
    private LocalDateTime date;
}
