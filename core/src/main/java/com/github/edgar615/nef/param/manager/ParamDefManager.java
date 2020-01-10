package com.github.edgar615.nef.param.manager;

import com.github.edgar615.nef.param.model.ParamDefViewModel;
import java.util.List;

/**
 * 公用业务逻辑
 */
public interface ParamDefManager {

  List<ParamDefViewModel> findByGroup(long paramGroupId);
}
