package com.webapi.application.security;

import com.webapi.application.models.Status;
import com.webapi.application.models.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SecurityUserDetailsService implements UserDetailsService
{
    List<User> users = new ArrayList<>(2);

    public SecurityUserDetailsService()
    {
        User user = new User();
        user.setEmail("test@mail.ru");
        user.setPassword("$2a$12$Ywt7QmgBirp23A3u4Bz/oeKuD0Lbu3G1BOYa2jVUmzj5ahgiNKxpC");   // 123456
        user.setStatus(Status.ACTIVE);
        users.add(user);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException
    {
        return SecurityUser.fromUser(users.get(0));
    }
}
