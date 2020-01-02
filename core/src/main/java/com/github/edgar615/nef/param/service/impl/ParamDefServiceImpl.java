package com.github.edgar615.nef.param.service.impl;

import com.github.edgar615.nef.commons.constant.ParamConsts;
import com.github.edgar615.nef.param.dao.ParamDefDao;
import com.github.edgar615.nef.param.dao.ParamOptionDao;
import com.github.edgar615.nef.param.dao.ParamValueDao;
import com.github.edgar615.nef.param.entity.ParamDef;
import com.github.edgar615.nef.param.entity.ParamDefKit;
import com.github.edgar615.nef.param.entity.ParamDefRule;
import com.github.edgar615.nef.param.entity.ParamOption;
import com.github.edgar615.nef.param.entity.ParamOptionKit;
import com.github.edgar615.nef.param.entity.ParamOptionRule;
import com.github.edgar615.nef.param.model.SaveParamDefModel;
import com.github.edgar615.nef.param.service.ParamDefService;
import com.github.edgar615.util.exception.DefaultErrorCode;
import com.github.edgar615.util.exception.SystemException;
import com.github.edgar615.util.validation.Rule;
import com.github.edgar615.util.validation.Validations;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import javax.annotation.PostConstruct;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ParamDefServiceImpl implements ParamDefService {

  private final Multimap<String, Rule> insertRules = ArrayListMultimap.create();

  private final Multimap<String, Rule> insertOptionRules = ArrayListMultimap.create();

  private final Multimap<String, Rule> updateRules = ArrayListMultimap.create();

  @Autowired
  private ParamDefDao paramDefDao;

  @Autowired
  private ParamOptionDao paramOptionDao;

  @Autowired
  private ParamValueDao paramValueDao;

  @Override
  @CacheEvict(cacheNames = "paramDefCache", key = "#p0.paramGroupId")
  @Transactional
  public long insert(SaveParamDefModel saveParamDefModel) {
    Validations.validate(saveParamDefModel, insertRules);
    ParamDef paramDef = new ParamDef();
    BeanUtils.copyProperties(saveParamDefModel, paramDef);
    if (paramDef.getType() == ParamConsts.PARAM_TYPE_INT
        || paramDef.getType() == ParamConsts.PARAM_TYPE_FLOAT) {
      paramDef.setMaxLength(null);
      paramDef.setMinLength(null);
    }
    if (paramDef.getType() == ParamConsts.PARAM_TYPE_STR) {
      paramDef.setMinValue(null);
      paramDef.setMaxValue(null);
    }
    if (paramDef.getType() == ParamConsts.PARAM_TYPE_BOOL) {
      paramDef.setMinValue(null);
      paramDef.setMaxValue(null);
      paramDef.setMaxLength(null);
      paramDef.setMinLength(null);
    }
    if (paramDef.getType() == ParamConsts.PARAM_TYPE_OPTION_SINGLE
        || paramDef.getType() == ParamConsts.PARAM_TYPE_OPTION_MULTI) {
      if (saveParamDefModel.getOptions() == null
          || saveParamDefModel.getOptions().isEmpty()) {
        throw SystemException.create(DefaultErrorCode.MISSING_ARGS)
            .setDetails("options");
      } else {
        // 校验option
        for (ParamOption paramOption : saveParamDefModel.getOptions()) {
          Validations.validate(paramOption, insertOptionRules);
        }
      }
    }
    paramDefDao.insertAndGeneratedKey(paramDef);
    if (paramDef.getType() == ParamConsts.PARAM_TYPE_OPTION_SINGLE
        || paramDef.getType() == ParamConsts.PARAM_TYPE_OPTION_MULTI) {
      for (ParamOption paramOption : saveParamDefModel.getOptions()) {
        paramOption.setParamDefId(paramDef.getParamDefId());
        paramOptionDao.insertAndGeneratedKey(paramOption);
      }
    }
    return paramDef.getParamDefId();

  }

  @Override
  public int updateById(SaveParamDefModel saveParamDefModel, long id) {
    ParamDef paramDef = new ParamDef();
    BeanUtils.copyProperties(saveParamDefModel, paramDef);
    if (paramDef.getType() == ParamConsts.PARAM_TYPE_INT
        || paramDef.getType() == ParamConsts.PARAM_TYPE_FLOAT) {
      paramDef.setMaxLength(null);
      paramDef.setMinLength(null);
    }
    if (paramDef.getType() == ParamConsts.PARAM_TYPE_STR) {
      paramDef.setMinValue(null);
      paramDef.setMaxValue(null);
    }
    if (paramDef.getType() == ParamConsts.PARAM_TYPE_BOOL) {
      paramDef.setMinValue(null);
      paramDef.setMaxValue(null);
      paramDef.setMaxLength(null);
      paramDef.setMinLength(null);
    }
    if (paramDef.getType() == ParamConsts.PARAM_TYPE_OPTION_SINGLE
        || paramDef.getType() == ParamConsts.PARAM_TYPE_OPTION_MULTI) {
      if (saveParamDefModel.getOptions() != null || !saveParamDefModel.getOptions()
          .isEmpty()) {
        for (ParamOption paramOption : saveParamDefModel.getOptions()) {
          Validations.validate(paramOption, insertOptionRules);
        }
      }
    }
    int result = paramDefDao.updateById(paramDef, id);
    paramOptionDao.deleteByDef(id);
    // 检查option
    for (ParamOption paramOption : saveParamDefModel.getOptions()) {
      paramOption.setParamDefId(paramDef.getParamDefId());
      paramOptionDao.insertAndGeneratedKey(paramOption);
    }
    return result;
  }

  @Override
  public int deleteById(long id) {
    int result = paramDefDao.deleteById(id);
    paramOptionDao.deleteByDef(id);
    paramValueDao.deleteByDef(id);
    return result;
  }

  @PostConstruct
  public void initRule() {
    insertRules.put(ParamDefKit.TYPE, Rule.required());
    insertRules.put(ParamDefKit.TYPE,
        Rule.optional(Lists.newArrayList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14)));
    insertRules.put(ParamDefKit.NAME, Rule.required());
    insertRules.put(ParamDefKit.NAME, Rule.alphaUnderscore());
    insertRules.put(ParamDefKit.ALIAS, Rule.required());
    insertRules.put(ParamDefKit.ALIAS, Rule.required());
    insertRules.putAll(ParamDefRule.maxLengthRule());

    updateRules.put(ParamDefKit.TYPE, Rule.prohibited());
    updateRules.put(ParamDefKit.NAME, Rule.prohibited());
    updateRules.putAll(ParamDefRule.maxLengthRule());

    insertOptionRules.put(ParamOptionKit.OPTION_TEXT, Rule.required());
    insertOptionRules.put(ParamOptionKit.OPTION_VALUE, Rule.required());
    insertOptionRules.putAll(ParamOptionRule.maxLengthRule());

  }
}
