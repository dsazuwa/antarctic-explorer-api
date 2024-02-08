package com.antarctica.explorer.api.repository;

import com.google.common.base.CaseFormat;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.QueryRewriter;

public class ExpeditionQueryWriter implements QueryRewriter {

  @Override
  public String rewrite(String query, Sort sort) {
    for (Sort.Order order : sort) {
      String property = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, order.getProperty());
      query = query.replaceAll(order.getProperty(), property);
    }

    int descIndex = query.lastIndexOf("desc");
    int ascIndex = query.lastIndexOf("asc");
    int lastIndex = Math.max(descIndex, ascIndex);

    if (lastIndex != -1) {
      String last = (lastIndex == descIndex) ? "desc" : "asc";
      StringBuilder sb = new StringBuilder(query);
      sb.insert(lastIndex + last.length(), " nulls last");
      query = sb.toString();
    }

    return query;
  }
}
