package com.github.edgar615.nef.param.reuse;

import com.github.edgar615.nef.param.model.ViewParamDefModel;
import java.util.List;

public interface ParamDefReuseService {

  List<ViewParamDefModel> list(long paramGroupId);
}
