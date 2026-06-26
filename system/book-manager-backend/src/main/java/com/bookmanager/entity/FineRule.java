package com.bookmanager.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class FineRule {
    private Integer id;
    private BigDecimal finePerDay;
    private BigDecimal maxFine;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public BigDecimal getFinePerDay() { return finePerDay; }
    public void setFinePerDay(BigDecimal finePerDay) { this.finePerDay = finePerDay; }
    public BigDecimal getMaxFine() { return maxFine; }
    public void setMaxFine(BigDecimal maxFine) { this.maxFine = maxFine; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}
