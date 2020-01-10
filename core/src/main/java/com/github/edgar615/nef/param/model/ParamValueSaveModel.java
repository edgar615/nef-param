package com.github.edgar615.nef.param.model;

import java.util.Map;

public class ParamValueSaveModel {

  /**
   * 参数组名称
   */
  private String paramGroup;

  /**
   * 应用ID，应用类的参数必填项
   */
  private Long applicationId;

  /**
   * 用户ID，用户类的参数必填项
   */
  private Long userId;

  /**
   * 群组ID，群组类的参数必填项
   */
  private Long groupId;

  private Map<String, Object> paramValues;

  public String getParamGroup() {
    return paramGroup;
  }

  public void setParamGroup(String paramGroup) {
    this.paramGroup = paramGroup;
  }

  public Long getApplicationId() {
    return applicationId;
  }

  public void setApplicationId(Long applicationId) {
    this.applicationId = applicationId;
  }

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public Long getGroupId() {
    return groupId;
  }

  public void setGroupId(Long groupId) {
    this.groupId = groupId;
  }

  public Map<String, Object> getParamValues() {
    return paramValues;
  }

  public void setParamValues(Map<String, Object> paramValues) {
    this.paramValues = paramValues;
  }
}
