package ru.nsu.db.tables;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;

@Entity
@Table(name = "invitation")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Invitation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_from", nullable = false)
    @JsonIgnore
    private Users userFrom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_to", nullable = false)
    @JsonIgnore
    private Users userTo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    @JsonIgnore
    private Groups group;

    @Column(nullable = false)
    private String status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Users getUserFrom() {
        return userFrom;
    }

    public void setUserFrom(Users userFrom) {
        this.userFrom = userFrom;
    }

    public Users getUserTo() {
        return userTo;
    }

    public void setUserTo(Users userTo) {
        this.userTo = userTo;
    }

    public Groups getGroup() {
        return group;
    }

    public void setGroup(Groups group) {
        this.group = group;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}