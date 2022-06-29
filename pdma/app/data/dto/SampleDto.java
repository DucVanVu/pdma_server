package org.pepfar.pdma.app.data.dto;

import java.io.Serializable;

public class SampleDto implements Serializable {

    private Integer subcategoryid;

    private String categoryname;

    private Integer menuid;

    private String name;

    private Float price;

    private String type;

    public void setSubcategoryid(Integer subcategoryid) {
        this.subcategoryid = subcategoryid;
    }

    public void setCategoryname(String categoryname) {
        this.categoryname = categoryname;
    }

    public void setMenuid(Integer menuid) {
        this.menuid = menuid;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPrice(Float price) {
        this.price = price;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getSubcategoryid() {
        return subcategoryid;
    }

    public String getCategoryname() {
        return categoryname;
    }

    public Integer getMenuid() {
        return menuid;
    }

    public String getName() {
        return name;
    }

    public Float getPrice() {
        return price;
    }

    public String getType() {
        return type;
    }
}
