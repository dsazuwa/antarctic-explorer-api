package com.antarctica.explorer.api.model;

import com.antarctica.explorer.api.pojo.ExpeditionFilter;
import com.antarctica.explorer.api.pojo.RangedFilter;
import jakarta.persistence.criteria.Expression;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public class ExpeditionSpec {

  private ExpeditionSpec() {}

  public static Specification<Expedition> filterBy(ExpeditionFilter filter) {
    return Specification.where(hasCruiseLine(filter.cruiseLines()))
        .and(hasDuration(filter.duration()));
  }

  private static Specification<Expedition> hasCruiseLine(List<String> cruiseLines) {
    return (root, query, criteriaBuilder) ->
        cruiseLines == null
            ? criteriaBuilder.conjunction()
            : root.join("cruiseLine").get("name").in(cruiseLines);
  }

  private static Specification<Expedition> hasDuration(RangedFilter filter) {
    return (root, query, criteriaBuilder) -> {
      if (filter == null) return criteriaBuilder.conjunction();
      int min = filter.min(), max = filter.max();

      //  SELECT *
      //  FROM antarctica.expeditions
      //      WHERE
      //  CASE
      //  WHEN POSITION('-' IN duration) > 0 THEN
      //  CAST(SPLIT_PART(duration, '-', 1) AS INTEGER) BETWEEN 8 AND 14
      //  OR CAST(SPLIT_PART(duration, '-', 2) AS INTEGER) BETWEEN 8 AND 14
      //  ELSE
      //  CAST(duration AS INTEGER) BETWEEN 8 AND 14
      //  END;

      Expression<Integer> part1 =
          criteriaBuilder
              .function(
                  "SPLIT_PART",
                  String.class,
                  root.get("duration"),
                  criteriaBuilder.literal("-"),
                  criteriaBuilder.literal(1))
              .as(Integer.class);

      return criteriaBuilder.between(part1, min, max);

      //            Expression<String> part2 =
      //                criteriaBuilder.function(
      //                    "SPLIT_PART",
      //                    String.class,
      //                    root.get("duration"),
      //                    criteriaBuilder.literal("-"),
      //                    criteriaBuilder.literal(2));

      //      return criteriaBuilder
      //          .selectCase()
      //          .when(criteriaBuilder.equal(part2, ""), criteriaBuilder.between(part1, min, max))
      //          .otherwise(
      //              criteriaBuilder.or(
      //                  criteriaBuilder.between(part1, min, max),
      //                  criteriaBuilder.between(part2.as(Integer.class), min, max)));

      //            return criteriaBuilder.or(
      //                criteriaBuilder.between(part1, min, max),
      //                criteriaBuilder.and(
      //                    criteriaBuilder.equal(part2, ""), criteriaBuilder.between(part1, min,
      // max)),
      //                criteriaBuilder.between(part2.as(Integer.class), min, max));
    };
  }
}
