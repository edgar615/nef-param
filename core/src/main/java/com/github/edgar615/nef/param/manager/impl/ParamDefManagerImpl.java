package com.github.edgar615.nef.param.manager.impl;

import com.github.edgar615.nef.commons.constant.ParamConsts;
import com.github.edgar615.nef.param.dao.ParamDefDao;
import com.github.edgar615.nef.param.dao.ParamOptionDao;
import com.github.edgar615.nef.param.entity.ParamDef;
import com.github.edgar615.nef.param.entity.ParamOption;
import com.github.edgar615.nef.param.model.ParamDefViewModel;
import com.github.edgar615.nef.param.manager.ParamDefManager;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ParamDefManagerImpl implements ParamDefManager {

  @Autowired
  private ParamDefDao paramDefDao;

  @Autowired
  private ParamOptionDao paramOptionDao;

  @Override
  public List<ParamDefViewModel> findByGroup(long paramGroupId) {
    List<ParamDef> paramDefList = paramDefDao.findByGroup(paramGroupId);
    List<ParamDefViewModel> paramDefViewModelList = new ArrayList<>();
    for (ParamDef paramDef : paramDefList) {
      ParamDefViewModel paramDefViewModel = new ParamDefViewModel();
      paramDefViewModelList.add(paramDefViewModel);
      BeanUtils.copyProperties(paramDef, paramDefViewModel);
      if (paramDefViewModel.getType().equals(ParamConsts.PARAM_TYPE_OPTION_SINGLE)
          || paramDefViewModel.getType().equals(ParamConsts.PARAM_TYPE_OPTION_MULTI)) {
        List<ParamOption> paramOptions = paramOptionDao.findByDef(paramDef.getParamDefId());
        paramDefViewModel.setOptions(paramOptions);
      }
    }
    return paramDefViewModelList;
  }
}
