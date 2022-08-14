package com.webapi.application.models.sign;

import com.webapi.application.models.user.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "sign_templates")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SignTemplateModel
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(optional=false, cascade=CascadeType.ALL)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "sign_owner")
    private String signOwner;   // поле владельца подписи

    @Column(name = "certificate")
    private String signCertificate; // поле номер сертификата

    @Column(name = "date_start")
    private String signDateStart;   // поле начала действия сертификата

    @Column(name = "date_end")
    private String signDateEnd;     // поле окончания действия сертификата

    @Column(name = "isDrawLogo")
    private boolean drawLogo;   // флаг отрисовки герба

    @Column(name = "checkNewPage")
    private boolean checkTransitionToNewPage;   // флаг проверки перехода на новую страницу

    @Column(name = "insertType")
    private int insertType; // тип вставки (0 - классический, 1 - по координатам, 2 - по тэгу)
}
