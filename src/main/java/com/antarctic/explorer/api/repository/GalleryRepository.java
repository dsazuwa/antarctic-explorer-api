package com.antarctic.explorer.api.repository;

import com.antarctic.explorer.api.model.Expedition;
import com.antarctic.explorer.api.model.Gallery;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GalleryRepository extends JpaRepository<Gallery, Integer> {
  Optional<Gallery> findByExpeditionAndPhotoUrl(Expedition expedition, String photoUrl);
}
