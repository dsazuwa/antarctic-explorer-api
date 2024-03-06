package com.antarctica.explorer.api.repository;

import com.antarctica.explorer.api.model.CruiseLine;
import com.antarctica.explorer.api.model.Extension;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExtensionRepository extends JpaRepository<Extension, Long> {

  Optional<Extension> findByCruiseLineAndName(CruiseLine cruiseLine, String nameT);
}
