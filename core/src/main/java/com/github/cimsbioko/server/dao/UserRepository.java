package com.github.cimsbioko.server.dao;

import com.github.cimsbioko.server.domain.AccessToken;
import com.github.cimsbioko.server.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends PagingAndSortingRepository<User, String>, UserSearch {

    User findByUsernameAndDeletedIsNull(String username);

    Page<User> findByDeletedIsNull(Pageable pageable);

    @Query("select u from #{#entityName} u where u.deleted is null order by u.username")
    List<User> findSelectableForCampaign();

    @Query("select distinct u from #{#entityName} u join u.tokens t where :token = t")
    Optional<User> findByToken(@NotNull AccessToken token);

}
