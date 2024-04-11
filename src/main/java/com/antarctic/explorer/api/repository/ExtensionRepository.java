package com.antarctic.explorer.api.repository;

import com.antarctic.explorer.api.model.CruiseLine;
import com.antarctic.explorer.api.model.Extension;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface ExtensionRepository extends JpaRepository<Extension, Long> {

  @Modifying(clearAutomatically = true)
  @Query("DELETE FROM Extension e WHERE e.cruiseLine.id = :cruiseLineId")
  void deleteByCruiseLineId(Integer cruiseLineId);

  Optional<Extension> findByCruiseLineAndName(CruiseLine cruiseLine, String nameT);
}
