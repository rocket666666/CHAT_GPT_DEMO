package com.example.chatsales.mysql.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "inventory_pro")
public class Sals {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sku_code")
    private String skuCode; // 产品号
    
    @Column(name = "sku_name")
    private String skuName; // 产品名称
    
    @Column(name = "e_name")
    private String eName; // 产品英文名称
    
    @Column(name = "inv_attr")
    private String invAttr; // 产品类别
    
    @Column(name = "storage_type")
    private String storageType; // 存储方式
    
    @Column(name = "qty")
    private Double qty; // 主数量
    
    @Column(name = "main_unit")
    private String mainUnit; // 主单位
    
    @Column(name = "udf31")
    private Double udf31; // 辅数量
    
    @Column(name = "udf30")
    private String udf30; // 辅单位
    
    @Column(name = "fresh_frozen_flag")
    private String freshFrozenFlag; // 鲜转冻标识
    
    @Column(name = "udf27")
    private String udf27; // 生产日期
    
    @Column(name = "shelf_life_days")
    private Integer shelfLifeDays; // 保质期天数
    
    @Column(name = "loc_code")
    private String locCode; // 货位号
    
    @Column(name = "loc_name")
    private String locName; // 货位名称

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getSkuCode() { return skuCode; }
    public void setSkuCode(String skuCode) { this.skuCode = skuCode; }
    public String getSkuName() { return skuName; }
    public void setSkuName(String skuName) { this.skuName = skuName; }
    public String getEName() { return eName; }
    public void setEName(String eName) { this.eName = eName; }
    public String getInvAttr() { return invAttr; }
    public void setInvAttr(String invAttr) { this.invAttr = invAttr; }
    public String getStorageType() { return storageType; }
    public void setStorageType(String storageType) { this.storageType = storageType; }
    public Double getQty() { return qty; }
    public void setQty(Double qty) { this.qty = qty; }
    public String getMainUnit() { return mainUnit; }
    public void setMainUnit(String mainUnit) { this.mainUnit = mainUnit; }
    public Double getUdf31() { return udf31; }
    public void setUdf31(Double udf31) { this.udf31 = udf31; }
    public String getUdf30() { return udf30; }
    public void setUdf30(String udf30) { this.udf30 = udf30; }
    public String getFreshFrozenFlag() { return freshFrozenFlag; }
    public void setFreshFrozenFlag(String freshFrozenFlag) { this.freshFrozenFlag = freshFrozenFlag; }
    public String getUdf27() { return udf27; }
    public void setUdf27(String udf27) { this.udf27 = udf27; }
    public Integer getShelfLifeDays() { return shelfLifeDays; }
    public void setShelfLifeDays(Integer shelfLifeDays) { this.shelfLifeDays = shelfLifeDays; }
    public String getLocCode() { return locCode; }
    public void setLocCode(String locCode) { this.locCode = locCode; }
    public String getLocName() { return locName; }
    public void setLocName(String locName) { this.locName = locName; }
} 