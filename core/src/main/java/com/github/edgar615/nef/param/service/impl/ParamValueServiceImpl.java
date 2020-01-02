package com.github.edgar615.nef.param.service.impl;

import com.github.edgar615.nef.commons.constant.ParamConsts;
import com.github.edgar615.nef.commons.error.ParamError;
import com.github.edgar615.nef.param.dao.ParamDefDao;
import com.github.edgar615.nef.param.dao.ParamGroupDao;
import com.github.edgar615.nef.param.dao.ParamOptionDao;
import com.github.edgar615.nef.param.dao.ParamValueDao;
import com.github.edgar615.nef.param.entity.ParamDef;
import com.github.edgar615.nef.param.entity.ParamGroup;
import com.github.edgar615.nef.param.entity.ParamOption;
import com.github.edgar615.nef.param.entity.ParamValue;
import com.github.edgar615.nef.param.model.SaveParamValueModel;
import com.github.edgar615.nef.param.model.SingleParamValue;
import com.github.edgar615.nef.param.model.ViewParamDefModel;
import com.github.edgar615.nef.param.reuse.ParamDefReuseService;
import com.github.edgar615.nef.param.service.ParamValueService;
import com.github.edgar615.util.exception.DefaultErrorCode;
import com.github.edgar615.util.exception.SystemException;
import com.github.edgar615.util.validation.Rule;
import com.github.edgar615.util.validation.Validations;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ParamValueServiceImpl implements ParamValueService {

  @Autowired
  private ParamValueDao paramValueDao;

  @Autowired
  private ParamGroupDao paramGroupDao;

  @Autowired
  private ParamDefDao paramDefDao;

  @Autowired
  private ParamOptionDao paramOptionDao;

  @Autowired
  private ParamDefReuseService paramDefReuseService;

  @Override
  @Transactional
  public void insert(SaveParamValueModel saveParamValueModel) {
    if (Strings.isNullOrEmpty(saveParamValueModel.getParamGroup())) {
      throw SystemException.create(DefaultErrorCode.MISSING_ARGS)
          .setDetails("paramGroup");
    }
    if (saveParamValueModel.getParamValues() == null
        || saveParamValueModel.getParamValues().isEmpty()) {
      throw SystemException.create(DefaultErrorCode.MISSING_ARGS)
          .setDetails("singleParamValues");
    }
    Long groupId = paramGroupDao.findByName(saveParamValueModel.getParamGroup());
    if (groupId == null) {
      throw SystemException.create(ParamError.GROUP_NOT_FOUND)
          .setDetails(saveParamValueModel.getParamGroup());
    }
    List<ViewParamDefModel> paramDefList = paramDefReuseService.list(groupId);
    Multimap<String, Rule> rules = transformToRules(paramDefList);
    Validations.validate(saveParamValueModel.getParamValues(), rules);
    // 先删除，再添加
    for (ViewParamDefModel viewParamDefModel : paramDefList) {
      paramValueDao.deleteByDef(viewParamDefModel.getParamDefId());
    }
    for (ViewParamDefModel viewParamDefModel : paramDefList) {
      ParamValue paramValue = new ParamValue();
//      paramValue.set
      paramValueDao.deleteByDef(viewParamDefModel.getParamDefId());
    }


  }

  private Multimap<String, Rule> transformToRules(List<ViewParamDefModel> paramDefModelList) {
    Multimap<String, Rule> rules = ArrayListMultimap.create();
    for (ViewParamDefModel viewParamDefModel : paramDefModelList) {
      rules.put(viewParamDefModel.getName(), Rule.required());

      if (viewParamDefModel.getType().equals(ParamConsts.PARAM_TYPE_INT)) {
        rules.put(viewParamDefModel.getName(), Rule.intRule());
      } else if (viewParamDefModel.getType().equals(ParamConsts.PARAM_TYPE_BOOL)) {
        rules.put(viewParamDefModel.getName(), Rule.bool());
      }
      if (viewParamDefModel.getType().equals(ParamConsts.PARAM_TYPE_OPTION_SINGLE)) {
        List<Object> options =
            viewParamDefModel.getOptions().stream().map(o -> o.getOptionValue()).collect(Collectors.toList());
        rules.put(viewParamDefModel.getName(), Rule.optional(options));
      }
      if (viewParamDefModel.getType().equals(ParamConsts.PARAM_TYPE_OPTION_MULTI)) {
        // TODO 没实现
        rules.put(viewParamDefModel.getName(), Rule.list());
      }
      if (viewParamDefModel.getMinValue() != null) {
        rules.put(viewParamDefModel.getName(), Rule.min(viewParamDefModel.getMinValue().intValue()));
      }
      if (viewParamDefModel.getMaxValue() != null) {
        rules.put(viewParamDefModel.getName(), Rule.max(viewParamDefModel.getMaxValue().intValue()));
      }
      if (viewParamDefModel.getMinLength() != null) {
        rules.put(viewParamDefModel.getName(), Rule.minLength(viewParamDefModel.getMinLength()));
      }
      if (viewParamDefModel.getMaxLength() != null) {
        rules.put(viewParamDefModel.getName(), Rule.maxLength(viewParamDefModel.getMaxLength()));
      }
      if (!Strings.isNullOrEmpty(viewParamDefModel.getRegex())) {
        rules.put(viewParamDefModel.getName(), Rule.regex(viewParamDefModel.getRegex()));
      }
      if (!Strings.isNullOrEmpty(viewParamDefModel.getPossibleValues())) {
        List<Object> optional = new ArrayList();
        Splitter.on(",").trimResults().omitEmptyStrings().splitToList(viewParamDefModel.getPossibleValues())
            .forEach(s -> optional.add(s));
        rules.put(viewParamDefModel.getName(), Rule.optional(optional));
      }
    }
    return rules;
  }
}
