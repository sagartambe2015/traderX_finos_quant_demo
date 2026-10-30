package com.quantraksha.kite.persist.entity;


import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "strategies")
public class StrategyEntity {

    @Id
    @Column(name = "StrategyID", length = 64, nullable = false)
    private String strategyId;

    @Column(name = "StrategyName", length = 128, nullable = false)
    private String strategyName;

    @Column(name = "Description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "IsActive")
    private Boolean isActive = true;

    @Column(name = "CreatedDate", updatable = false)
    private LocalDateTime createdDate = LocalDateTime.now();

    @OneToMany(mappedBy = "strategyEntity", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<TradeCaptureEntity> tradeCaptureEntities = new ArrayList<>();

    // Constructors
    public StrategyEntity() {}

    public StrategyEntity(String strategyId, String strategyName) {
        this.strategyId = strategyId;
        this.strategyName = strategyName;
    }

    // Getters and Setters
    public String getStrategyId() {
        return strategyId;
    }

    public void setStrategyId(String strategyId) {
        this.strategyId = strategyId;
    }

    public String getStrategyName() {
        return strategyName;
    }

    public void setStrategyName(String strategyName) {
        this.strategyName = strategyName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public List<TradeCaptureEntity> getTradeCaptures() {
        return tradeCaptureEntities;
    }

    public void setTradeCaptures(List<TradeCaptureEntity> tradeCaptureEntities) {
        this.tradeCaptureEntities = tradeCaptureEntities;
    }
}