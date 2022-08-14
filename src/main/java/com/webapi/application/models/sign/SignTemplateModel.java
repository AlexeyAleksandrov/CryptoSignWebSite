package com.webapi.application.models.sign;

import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "sign_templates")
@Data
public class SignTemplateModel
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "owner")
    private String signOwner;   // поле владельца подписи

    @Column
    private String signCertificate; // поле номер сертификата
    private String signDateStart;   // поле начала действия сертификата
    private String signDateEnd;     // поле окончания действия сертификата
    private boolean drawLogo;   // флаг отрисовки герба
    private boolean checkTransitionToNewPage;   // флаг проверки перехода на новую страницу
    private int insertType; // тип вставки (0 - классический, 1 - по координатам, 2 - по тэгу)
}
