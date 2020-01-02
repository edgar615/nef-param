package com.github.edgar615.nef.param.model;

import java.util.Map;

public class SaveParamValueModel {

  private String paramGroup;

  private Map<String, Object> paramValues;

  public String getParamGroup() {
    return paramGroup;
  }

  public void setParamGroup(String paramGroup) {
    this.paramGroup = paramGroup;
  }

  public Map<String, Object> getParamValues() {
    return paramValues;
  }

  public void setParamValues(Map<String, Object> paramValues) {
    this.paramValues = paramValues;
  }
}
