package com.github.edgar615.nef.param.reuse.impl;

import com.github.edgar615.nef.commons.constant.ParamConsts;
import com.github.edgar615.nef.param.dao.ParamDefDao;
import com.github.edgar615.nef.param.dao.ParamOptionDao;
import com.github.edgar615.nef.param.entity.ParamDef;
import com.github.edgar615.nef.param.entity.ParamOption;
import com.github.edgar615.nef.param.model.ViewParamDefModel;
import com.github.edgar615.nef.param.reuse.ParamDefReuseService;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ParamDefReuseServiceImpl implements ParamDefReuseService {

  @Autowired
  private ParamDefDao paramDefDao;

  @Autowired
  private ParamOptionDao paramOptionDao;

  @Override
  public List<ViewParamDefModel> list(long paramGroupId) {
    List<ParamDef> paramDefList = paramDefDao.findByGroup(paramGroupId);
    List<ViewParamDefModel> viewParamDefModelList = new ArrayList<>();
    for (ParamDef paramDef : paramDefList) {
      ViewParamDefModel viewParamDefModel = new ViewParamDefModel();
      viewParamDefModelList.add(viewParamDefModel);
      BeanUtils.copyProperties(paramDef, viewParamDefModel);
      if (viewParamDefModel.getType().equals(ParamConsts.PARAM_TYPE_OPTION_SINGLE)
          || viewParamDefModel.getType().equals(ParamConsts.PARAM_TYPE_OPTION_MULTI)) {
        List<ParamOption> paramOptions = paramOptionDao.findByDef(paramDef.getParamDefId());
        viewParamDefModel.setOptions(paramOptions);
      }
    }
    return viewParamDefModelList;
  }
}
