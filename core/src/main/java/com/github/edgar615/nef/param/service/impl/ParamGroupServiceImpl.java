package com.github.edgar615.nef.param.service.impl;

import com.github.edgar615.nef.param.dao.ParamGroupDao;
import com.github.edgar615.nef.param.entity.ParamGroup;
import com.github.edgar615.nef.param.entity.ParamGroupKit;
import com.github.edgar615.nef.param.entity.ParamGroupRule;
import com.github.edgar615.nef.param.model.ParamGroupQueryModel;
import com.github.edgar615.nef.param.service.ParamGroupService;
import com.github.edgar615.util.page.Pagination;
import com.github.edgar615.util.search.Example;
import com.github.edgar615.util.validation.Rule;
import com.github.edgar615.util.validation.Validations;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import java.time.Instant;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ParamGroupServiceImpl implements ParamGroupService {

  private final Multimap<String, Rule> insertRules = ArrayListMultimap.create();

  private static final int DEFAULT_SORTED = 9999;

  @Autowired
  private ParamGroupDao paramGroupDao;

  @Override
  public Pagination<ParamGroup> pagination(ParamGroupQueryModel queryModel, int page, int pageSize) {
    Example example = Example.create()
        .equalsTo(ParamGroupKit.NAME, queryModel.getName())
        .equalsTo(ParamGroupKit.ALIAS, queryModel.getAlias());
    return paramGroupDao.pagination(example, page, pageSize);
  }

  @Override
  public long insert(ParamGroup paramGroup) {
    Validations.validate(paramGroup, insertRules);
    if (paramGroup.getSorted() == null) {
      paramGroup.setSorted(DEFAULT_SORTED);
    }
    paramGroup.setCreateTime(Instant.now().getEpochSecond());
    paramGroupDao.insertAndGeneratedKey(paramGroup);
    return paramGroup.getParamGroupId();
  }

  @Override
  public int deleteById(long id) {
    return paramGroupDao.deleteById(id);
  }

  @PostConstruct
  public void initRule() {
    insertRules.put(ParamGroupKit.TYPE, Rule.required());
    insertRules.put(ParamGroupKit.NAME, Rule.required());
    insertRules.put(ParamGroupKit.ALIAS, Rule.required());
    insertRules.put(ParamGroupKit.TYPE, Rule.optional(Lists.newArrayList(1, 2, 3, 4, 5, 6)));
    insertRules.put(ParamGroupKit.NAME, Rule.alphaUnderscore());
    insertRules.put(ParamGroupKit.ALIAS, Rule.required());
    insertRules.putAll(ParamGroupRule.maxLengthRule());

  }
}
