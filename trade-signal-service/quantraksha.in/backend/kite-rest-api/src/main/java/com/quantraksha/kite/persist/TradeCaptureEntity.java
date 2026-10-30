package com.quantraksha.kite.persist;



import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "trade_capture")
public class TradeCaptureEntity {

    @Id
    @Column(name = "CaptureID", length = 64, nullable = false)
    private String captureId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "StrategyID", nullable = false)
    private StrategyEntity strategyEntity;

    @Column(name = "Security", length = 64, nullable = false)
    private String security;

    @Enumerated(EnumType.STRING)
    @Column(name = "Side", length = 10, nullable = false)
    private TradeSide side;

    @Column(name = "LotSize", nullable = false)
    private Integer lotSize;

    @Column(name = "TargetQuantity", precision = 18, scale = 4, nullable = false)
    private BigDecimal targetQuantity;

    // Date & Time
    @Column(name = "EntryDate", nullable = false)
    private LocalDate entryDate;

    @Column(name = "EntryTime", nullable = false)
    private LocalTime entryTime;

    @Column(name = "ExitDate")
    private LocalDate exitDate;

    @Column(name = "ExitTime")
    private LocalTime exitTime;

    // Calculated Targets (Model Outputs)
    @Column(name = "CalcEntry", precision = 18, scale = 4, nullable = false)
    private BigDecimal calcEntry;

    @Column(name = "CalcTG", precision = 18, scale = 4, nullable = false)
    private BigDecimal calcTg;

    @Column(name = "CalcSL", precision = 18, scale = 4, nullable = false)
    private BigDecimal calcSl;

    @Column(name = "CalcTSL", precision = 18, scale = 4)
    private BigDecimal calcTsl;

    // Actual Execution Targets
    @Column(name = "ActEntry", precision = 18, scale = 4)
    private BigDecimal actEntry;

    @Column(name = "ActTG", precision = 18, scale = 4)
    private BigDecimal actTg;

    @Column(name = "ActSL", precision = 18, scale = 4)
    private BigDecimal actSl;

    @Column(name = "ActTSL", precision = 18, scale = 4)
    private BigDecimal actTsl;

    // Price Structure
    @Column(name = "HH", precision = 18, scale = 4)
    private BigDecimal hh;

    @Column(name = "LL", precision = 18, scale = 4)
    private BigDecimal ll;

    // Risk Parameters
    @Column(name = "RiskRewardRatio", precision = 10, scale = 4)
    private BigDecimal riskRewardRatio;

    @Column(name = "RiskInPercentage", precision = 8, scale = 4)
    private BigDecimal riskInPercentage;

    @Column(name = "RewardInPercentage", precision = 8, scale = 4)
    private BigDecimal rewardInPercentage;

    // Evaluation State
    @Enumerated(EnumType.STRING)
    @Column(name = "GoNoDecision", length = 10, nullable = false)
    private GoNoDecision goNoDecision;

    @Column(name = "CaptureTimestamp", updatable = false)
    private LocalDateTime captureTimestamp = LocalDateTime.now();

    // Constructors
    public TradeCaptureEntity() {}

    // Getters and Setters
    public String getCaptureId() {
        return captureId;
    }

    public void setCaptureId(String captureId) {
        this.captureId = captureId;
    }

    public StrategyEntity getStrategy() {
        return strategyEntity;
    }

    public void setStrategy(StrategyEntity strategyEntity) {
        this.strategyEntity = strategyEntity;
    }

    public String getSecurity() {
        return security;
    }

    public void setSecurity(String security) {
        this.security = security;
    }

    public TradeSide getSide() {
        return side;
    }

    public void setSide(TradeSide side) {
        this.side = side;
    }

    public Integer getLotSize() {
        return lotSize;
    }

    public void setLotSize(Integer lotSize) {
        this.lotSize = lotSize;
    }

    public BigDecimal getTargetQuantity() {
        return targetQuantity;
    }

    public void setTargetQuantity(BigDecimal targetQuantity) {
        this.targetQuantity = targetQuantity;
    }

    public LocalDate getEntryDate() {
        return entryDate;
    }

    public void setEntryDate(LocalDate entryDate) {
        this.entryDate = entryDate;
    }

    public LocalTime getEntryTime() {
        return entryTime;
    }

    public void setEntryTime(LocalTime entryTime) {
        this.entryTime = entryTime;
    }

    public LocalDate getExitDate() {
        return exitDate;
    }

    public void setExitDate(LocalDate exitDate) {
        this.exitDate = exitDate;
    }

    public LocalTime getExitTime() {
        return exitTime;
    }

    public void setExitTime(LocalTime exitTime) {
        this.exitTime = exitTime;
    }

    public BigDecimal getCalcEntry() {
        return calcEntry;
    }

    public void setCalcEntry(BigDecimal calcEntry) {
        this.calcEntry = calcEntry;
    }

    public BigDecimal getCalcTg() {
        return calcTg;
    }

    public void setCalcTg(BigDecimal calcTg) {
        this.calcTg = calcTg;
    }

    public BigDecimal getCalcSl() {
        return calcSl;
    }

    public void setCalcSl(BigDecimal calcSl) {
        this.calcSl = calcSl;
    }

    public BigDecimal getCalcTsl() {
        return calcTsl;
    }

    public void setCalcTsl(BigDecimal calcTsl) {
        this.calcTsl = calcTsl;
    }

    public BigDecimal getActEntry() {
        return actEntry;
    }

    public void setActEntry(BigDecimal actEntry) {
        this.actEntry = actEntry;
    }

    public BigDecimal getActTg() {
        return actTg;
    }

    public void setActTg(BigDecimal actTg) {
        this.actTg = actTg;
    }

    public BigDecimal getActSl() {
        return actSl;
    }

    public void setActSl(BigDecimal actSl) {
        this.actSl = actSl;
    }

    public BigDecimal getActTsl() {
        return actTsl;
    }

    public void setActTsl(BigDecimal actTsl) {
        this.actTsl = actTsl;
    }

    public BigDecimal getHh() {
        return hh;
    }

    public void setHh(BigDecimal hh) {
        this.hh = hh;
    }

    public BigDecimal getLl() {
        return ll;
    }

    public void setLl(BigDecimal ll) {
        this.ll = ll;
    }

    public BigDecimal getRiskRewardRatio() {
        return riskRewardRatio;
    }

    public void setRiskRewardRatio(BigDecimal riskRewardRatio) {
        this.riskRewardRatio = riskRewardRatio;
    }

    public BigDecimal getRiskInPercentage() {
        return riskInPercentage;
    }

    public void setRiskInPercentage(BigDecimal riskInPercentage) {
        this.riskInPercentage = riskInPercentage;
    }

    public BigDecimal getRewardInPercentage() {
        return rewardInPercentage;
    }

    public void setRewardInPercentage(BigDecimal rewardInPercentage) {
        this.rewardInPercentage = rewardInPercentage;
    }

    public GoNoDecision getGoNoDecision() {
        return goNoDecision;
    }

    public void setGoNoDecision(GoNoDecision goNoDecision) {
        this.goNoDecision = goNoDecision;
    }

    public LocalDateTime getCaptureTimestamp() {
        return captureTimestamp;
    }

    public void setCaptureTimestamp(LocalDateTime captureTimestamp) {
        this.captureTimestamp = captureTimestamp;
    }
}