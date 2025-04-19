package com.finapp.backend.repository;

import com.finapp.backend.model.FundBox;
import com.finapp.backend.model.FundBoxCollaborator;
import com.finapp.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FundBoxCollaboratorRepository extends JpaRepository<FundBoxCollaborator, Long> {

    List<FundBoxCollaborator> findByFundBox(FundBox fundBox);

    List<FundBoxCollaborator> findByUser(User user);

    Optional<FundBoxCollaborator> findByFundBoxAndUser(FundBox fundBox, User user);

    boolean existsByFundBoxAndUser(FundBox fundBox, User user);

    void deleteByFundBoxAndUser(FundBox fundBox, User user);
}
