package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.Site;

public interface SiteRepository extends JpaRepository<Site, Integer> {

    @Modifying()
    @Transactional
    void deleteByUrl(String url);
}
