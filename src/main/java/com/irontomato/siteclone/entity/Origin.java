package com.irontomato.siteclone.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.Date;

@Entity
public class Origin {

    @Id
    @GeneratedValue
    private Integer id;

    @Column(length = 2000)
    private String url;

    @Column(unique = true)
    private String urlDigest;

    private Date createTime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrlDigest() {
        return urlDigest;
    }

    public void setUrlDigest(String urlDigest) {
        this.urlDigest = urlDigest;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
