package com.example.chatsales.sqlserver.entity;

// 移除JPA注解，将实体类转为DTO
public class SourceRecord {
    private String skuCode; // 产品号
    private String skuName; // 产品名称
    private String eName; // 产品英文名称
    //private String invAttr; // 产品类别
    private String storageType; // 存储方式
    private Double qty; // 主数量
    private String mainUnit; // 主单位
    private Double udf31; // 辅数量
    private String udf30; // 辅单位
    private String freshFrozenFlag; // 鲜转冻标识
    private String udf27; // 生产日期
    private Integer shelfLifeDays; // 保质期天数
    private String locCode; // 货位号
    private String locName; // 货位名称

    public String getSkuCode() { return skuCode; }
    public void setSkuCode(String skuCode) { this.skuCode = skuCode; }
    public String getSkuName() { return skuName; }
    public void setSkuName(String skuName) { this.skuName = skuName; }
    public String getEName() { return eName; }
    public void setEName(String eName) { this.eName = eName; }
//    public String getInvAttr() { return invAttr; }
//    public void setInvAttr(String invAttr) { this.invAttr = invAttr; }
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