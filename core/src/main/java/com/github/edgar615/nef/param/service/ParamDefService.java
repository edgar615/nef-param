package com.github.edgar615.nef.param.service;

import com.github.edgar615.nef.param.model.SaveParamDefModel;

public interface ParamDefService {

  long insert(SaveParamDefModel saveParamDefModel);

  int updateById(SaveParamDefModel saveParamDefModel, long id);

  int deleteById(long id);
}
