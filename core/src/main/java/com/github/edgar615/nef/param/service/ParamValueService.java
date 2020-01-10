package com.github.edgar615.nef.param.service;

import com.github.edgar615.nef.param.model.ParamValueGetModel;
import com.github.edgar615.nef.param.model.ParamValueSaveModel;
import java.util.Map;

public interface ParamValueService {

  void insert(ParamValueSaveModel paramValueSaveModel);

  /**
   * 查找参数值
   * @param paramValueGetModel
   * @return 返回参数的map对象
   */
  Map<String, Object> findByGroup(ParamValueGetModel paramValueGetModel);
}
