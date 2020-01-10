package com.github.edgar615.nef.param.service.impl;

import com.github.edgar615.nef.commons.constant.GlobalConsts;
import com.github.edgar615.nef.commons.constant.ParamConsts;
import com.github.edgar615.nef.commons.error.ParamError;
import com.github.edgar615.nef.param.dao.ParamDefDao;
import com.github.edgar615.nef.param.dao.ParamGroupDao;
import com.github.edgar615.nef.param.dao.ParamOptionDao;
import com.github.edgar615.nef.param.dao.ParamValueDao;
import com.github.edgar615.nef.param.entity.ParamGroup;
import com.github.edgar615.nef.param.entity.ParamValue;
import com.github.edgar615.nef.param.manager.ParamDefManager;
import com.github.edgar615.nef.param.model.ParamDefViewModel;
import com.github.edgar615.nef.param.model.ParamValueGetModel;
import com.github.edgar615.nef.param.model.ParamValueSaveModel;
import com.github.edgar615.nef.param.service.ParamValueService;
import com.github.edgar615.util.exception.DefaultErrorCode;
import com.github.edgar615.util.exception.SystemException;
import com.github.edgar615.util.search.Example;
import com.github.edgar615.util.validation.Rule;
import com.github.edgar615.util.validation.Validations;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.persistence.criteria.CriteriaBuilder.In;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
  private ParamDefManager paramDefManager;

  @Override
  @Transactional
  public void insert(ParamValueSaveModel paramValueSaveModel) {
    checkArgGroupName(paramValueSaveModel.getParamGroup());
    checkArgParamValues(paramValueSaveModel.getParamValues());
    ParamGroup paramGroup = findParamGroup(paramValueSaveModel.getParamGroup());
    checkArgsWithGroupType(paramGroup, paramValueSaveModel.getApplicationId(), paramValueSaveModel.getUserId(), paramGroup.getParamGroupId());
    List<ParamDefViewModel> paramDefList = paramDefManager.findByGroup(paramGroup.getParamGroupId());
    Multimap<String, Rule> rules = transformToRules(paramDefList);
    Validations.validate(paramValueSaveModel.getParamValues(), rules);
    // 先删除，再添加
    for (ParamDefViewModel paramDefViewModel : paramDefList) {
      paramValueDao.deleteByDef(paramDefViewModel.getParamDefId());
    }
    for (ParamDefViewModel paramDefViewModel : paramDefList) {
      ParamValue paramValue = new ParamValue();
      paramValue.setParamValue(
          paramValueSaveModel.getParamValues().get(paramDefViewModel.getName()).toString());
      paramValue.setParamDefId(paramDefViewModel.getParamDefId());
      if (checkApplication(paramGroup.getType())) {
        paramValue.setApplicationId(paramValueSaveModel.getApplicationId());
      }
      if (checkUser(paramGroup.getType())) {
        paramValue.setSubjectId(paramValueSaveModel.getUserId());
      }
      if (checkGroup(paramGroup.getType())) {
        paramValue.setSubjectId(paramValueSaveModel.getGroupId());
      }
      paramValue.setCreateTime(Instant.now().getEpochSecond());
      paramValueDao.insert(paramValue);
    }
  }

  private ParamGroup findParamGroup(String groupName) {
    Long paramGroupId = paramGroupDao.findByName(groupName);
    if (paramGroupId == null) {
      throw SystemException.create(ParamError.GROUP_NOT_FOUND)
          .setDetails(groupName);
    }
    ParamGroup paramGroup = paramGroupDao.findById(paramGroupId);
    if (paramGroup == null) {
      throw SystemException.create(ParamError.GROUP_NOT_FOUND)
          .setDetails(groupName);
    }
    return paramGroup;
  }

  @Override
  public Map<String, Object> findByGroup(ParamValueGetModel paramValueGetModel) {
    checkArgGroupName(paramValueGetModel.getParamGroup());
    ParamGroup paramGroup = findParamGroup(paramValueGetModel.getParamGroup());
    checkArgsWithGroupType(paramGroup, paramValueGetModel.getApplicationId(), paramValueGetModel.getUserId(), paramGroup.getParamGroupId());
    List<ParamDefViewModel> paramDefList = paramDefManager.findByGroup(paramGroup.getParamGroupId());
    Long paramGroupId = paramGroup.getParamGroupId();
    Long applicationId = paramValueGetModel.getApplicationId();
    Long userId = paramValueGetModel.getUserId();
    Long userGroupId = paramValueGetModel.getGroupId();
    Long subjectId = GlobalConsts.NULL_LONG;
    if (!checkApplication(paramGroup.getType())) {
      applicationId = GlobalConsts.NULL_LONG;
    }
    if (checkUser(paramGroup.getType())) {
      subjectId = userId;
    }
    if (!checkGroup(paramGroup.getType())) {
      subjectId = userGroupId;
    }

    List<ParamValue> paramValueList = findPlatformValue(paramGroupId, applicationId, subjectId);

    Map<String, Object> values = new HashMap<>();
    // 依次查找所有的参数，如果有缺失的不报错，由调用方处理
    for (ParamDefViewModel paramDefViewModel : paramDefList) {
      Object value = paramValueList.stream().filter(paramValue -> paramDefViewModel.getParamDefId().equals(paramValue.getParamDefId()))
          .map(paramValue -> paramValue.getParamValue())
          .findFirst()
          .orElse(null);
    }
    return values;
  }

  List<ParamValue> findPlatformValue(long groupId, long applicationId, long subjectId) {
    Example example = Example.create()
        .equalsTo("paramGroupId", groupId)
        .equalsTo("applicationId", applicationId)
        .equalsTo("subjectId", subjectId);
    return paramValueDao.findByExample(example);
  }

  private boolean checkApplication(int type) {
    return type == ParamConsts.PARAM_GROUP_TYPE_APPLICATION
        || type == ParamConsts.PARAM_GROUP_TYPE_APPLICATION_GROUP
        || type == ParamConsts.PARAM_GROUP_TYPE_APPLICATION_USER;
  }

  private boolean checkUser(int type) {
    return type == ParamConsts.PARAM_GROUP_TYPE_APPLICATION_USER
        || type == ParamConsts.PARAM_GROUP_TYPE_PLATFORM_USER;
  }

  private boolean checkGroup(int type) {
    return type == ParamConsts.PARAM_GROUP_TYPE_APPLICATION_GROUP
        || type == ParamConsts.PARAM_GROUP_TYPE_PLATFORM_GROUP;
  }

  private void checkArgsWithGroupType(      ParamGroup paramGroup, Long applicationId, Long userId, Long groupId) {
    if (checkApplication(paramGroup.getType()) && applicationId == null && applicationId != GlobalConsts.ROOT_ID) {
      throw SystemException.create(DefaultErrorCode.MISSING_ARGS)
          .setDetails("applicationId");
    }
    if (checkUser(paramGroup.getType()) && userId == null && applicationId != GlobalConsts.ROOT_ID) {
      throw SystemException.create(DefaultErrorCode.MISSING_ARGS)
          .setDetails("userId");
    }
    if (checkGroup(paramGroup.getType()) && groupId == null && applicationId != GlobalConsts.ROOT_ID) {
      throw SystemException.create(DefaultErrorCode.MISSING_ARGS)
          .setDetails("groupId");
    }
  }

  private void checkArgGroupName(String groupName) {
    if (Strings.isNullOrEmpty(groupName)) {
      throw SystemException.create(DefaultErrorCode.MISSING_ARGS)
          .setDetails("paramGroup");
    }
  }

  private void checkArgParamValues(Map<String, Object> paramValues) {
    if (paramValues == null || paramValues.isEmpty()) {
      throw SystemException.create(DefaultErrorCode.MISSING_ARGS)
          .setDetails("singleParamValues");
    }
  }

  private Object transformToValue(ParamDefViewModel paramDefViewModel, Object value) {
    if (value == null) {
      return value;
    }
    if (paramDefViewModel.getType().equals(ParamConsts.PARAM_TYPE_INT)) {
      return Integer.parseInt(value.toString());
    }
    if (paramDefViewModel.getType().equals(ParamConsts.PARAM_TYPE_BOOL)) {
      return Boolean.parseBoolean(value.toString());
    }
    if (paramDefViewModel.getType().equals(ParamConsts.PARAM_TYPE_OPTION_SINGLE)) {
      List<Integer> options =
          paramDefViewModel.getOptions().stream().map(o -> o.getOptionValue())
              .collect(Collectors.toList());
      Integer optionValue = Integer.parseInt(value.toString());
      return options.stream().filter(ov -> ov.equals(optionValue))
          .findFirst()
          .orElse(null);
    }
    if (paramDefViewModel.getType().equals(ParamConsts.PARAM_TYPE_OPTION_MULTI)) {
      // TODO 没实现
    }
    return value;
  }


  private Multimap<String, Rule> transformToRules(List<ParamDefViewModel> paramDefModelList) {
    Multimap<String, Rule> rules = ArrayListMultimap.create();
    for (ParamDefViewModel paramDefViewModel : paramDefModelList) {
      rules.put(paramDefViewModel.getName(), Rule.required());

      if (paramDefViewModel.getType().equals(ParamConsts.PARAM_TYPE_INT)) {
        rules.put(paramDefViewModel.getName(), Rule.intRule());
      }
      if (paramDefViewModel.getType().equals(ParamConsts.PARAM_TYPE_BOOL)) {
        rules.put(paramDefViewModel.getName(), Rule.bool());
      }
      if (paramDefViewModel.getType().equals(ParamConsts.PARAM_TYPE_OPTION_SINGLE)) {
        List<Object> options =
            paramDefViewModel.getOptions().stream().map(o -> o.getOptionValue())
                .collect(Collectors.toList());
        rules.put(paramDefViewModel.getName(), Rule.optional(options));
      }
      if (paramDefViewModel.getType().equals(ParamConsts.PARAM_TYPE_OPTION_MULTI)) {
        // TODO 没实现
//        rules.put(paramDefViewModel.getName(), Rule.list());
      }
      if (paramDefViewModel.getMinValue() != null) {
        rules
            .put(paramDefViewModel.getName(), Rule.min(paramDefViewModel.getMinValue().intValue()));
      }
      if (paramDefViewModel.getMaxValue() != null) {
        rules
            .put(paramDefViewModel.getName(), Rule.max(paramDefViewModel.getMaxValue().intValue()));
      }
      if (paramDefViewModel.getMinLength() != null) {
        rules.put(paramDefViewModel.getName(), Rule.minLength(paramDefViewModel.getMinLength()));
      }
      if (paramDefViewModel.getMaxLength() != null) {
        rules.put(paramDefViewModel.getName(), Rule.maxLength(paramDefViewModel.getMaxLength()));
      }
      if (!Strings.isNullOrEmpty(paramDefViewModel.getRegex())) {
        rules.put(paramDefViewModel.getName(), Rule.regex(paramDefViewModel.getRegex()));
      }
      if (!Strings.isNullOrEmpty(paramDefViewModel.getPossibleValues())) {
        List<Object> optional = new ArrayList();
        Splitter.on(",").trimResults().omitEmptyStrings()
            .splitToList(paramDefViewModel.getPossibleValues())
            .forEach(s -> optional.add(s));
        rules.put(paramDefViewModel.getName(), Rule.optional(optional));
      }
    }
    return rules;
  }
}
