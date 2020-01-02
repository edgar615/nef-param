package com.github.edgar615.nef.param.service;

import com.github.edgar615.nef.param.entity.ParamGroup;
import com.github.edgar615.nef.param.model.ParamGroupQueryModel;
import com.github.edgar615.util.page.Pagination;

/**
 * 参数分组的service
 */
public interface ParamGroupService {

  Pagination<ParamGroup> pagination(ParamGroupQueryModel queryModel, int page, int pageSize);

  long insert(ParamGroup paramGroup);

  int deleteById(long id);

}
