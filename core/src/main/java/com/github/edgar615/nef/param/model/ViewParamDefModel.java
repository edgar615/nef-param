package com.github.edgar615.nef.param.model;

import com.github.edgar615.nef.param.entity.ParamDef;
import com.github.edgar615.nef.param.entity.ParamOption;
import java.util.List;

public class ViewParamDefModel extends ParamDef {

  private List<ParamOption> options;

  public List<ParamOption> getOptions() {
    return options;
  }

  public void setOptions(List<ParamOption> options) {
    this.options = options;
  }
}
