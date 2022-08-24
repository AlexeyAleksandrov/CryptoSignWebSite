package com.webapi.application.models.user;

import com.webapi.application.models.sign.SignTemplateModel;
import lombok.Data;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Data
@Table(name = "users")
@Entity
public class User
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "username")
    private String username;

    @Column(name = "firstname")
    private String name;

    @Column(name = "surname")
    private String surname;

    @Column(name = "patronymic")
    private String patronymic;

    @Column(name = "passwd")
    private String password;

    @Column(name = "status")
    @Enumerated(value = EnumType.STRING)
    private Status status = Status.ACTIVE;

    @OneToMany(mappedBy = "user")
    private List<SignTemplateModel> signTemplates = new ArrayList<>();

    public List<SignTemplateModel> getSignTemplates()
    {
        signTemplates.sort(new Comparator<SignTemplateModel>()
        {
            @Override
            public int compare(SignTemplateModel o1, SignTemplateModel o2)
            {
                return o1.getId().compareTo(o2.getId());
            }
        });     // выполняем сортировку списка по возрастанию id
        return signTemplates;
    }
}
