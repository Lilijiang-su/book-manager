package com.bookmanager.entity;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class Book {
    private Integer id;
    @NotBlank(message = "ISBN不能为空")
    @Size(max = 20, message = "ISBN长度不能超过20")
    private String isbn;
    @NotBlank(message = "书名不能为空")
    @Size(max = 200, message = "书名长度不能超过200")
    private String name;
    @NotBlank(message = "作者不能为空")
    @Size(max = 100, message = "作者长度不能超过100")
    private String author;
    @Size(max = 100, message = "出版社长度不能超过100")
    private String publisher;
    private Integer categoryId;
    private String categoryName;
    @Size(max = 1000, message = "简介长度不能超过1000")
    private String description;
    @Size(max = 255, message = "封面URL长度不能超过255")
    private String cover;
    @NotNull(message = "库存不能为空")
    @Min(value = 0, message = "库存不能为负数")
    private Integer stock;
    @NotNull(message = "总数不能为空")
    @Min(value = 0, message = "总数不能为负数")
    private Integer total;
    private Integer borrowCount;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getPublisher() { return publisher; }
    public void setPublisher(String publisher) { this.publisher = publisher; }
    public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCover() { return cover; }
    public void setCover(String cover) { this.cover = cover; }
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
    public Integer getTotal() { return total; }
    public void setTotal(Integer total) { this.total = total; }
    public Integer getBorrowCount() { return borrowCount; }
    public void setBorrowCount(Integer borrowCount) { this.borrowCount = borrowCount; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}
