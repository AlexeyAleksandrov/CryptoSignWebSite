package com.webapi.application.repositories;

import com.webapi.application.models.sign.SignTemplateModel;
import com.webapi.application.models.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SignTemplatesRepository extends JpaRepository<SignTemplateModel, Long>
{
    SignTemplateModel findByUser(User user);
}
