package com.webapi.application.models.sign;

import com.webapi.application.models.user.User;
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

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "template_name")
    private String templateName;    // название подписи

    @Column(name = "sign_owner")
    private String signOwner;   // поле владельца подписи

    @Column(name = "certificate")
    private String signCertificate; // поле номер сертификата

    @Column(name = "date_start")
    private String signDateStart;   // поле начала действия сертификата

    @Column(name = "date_end")
    private String signDateEnd;     // поле окончания действия сертификата

    @Column(name = "is_draw_logo")
    private boolean drawLogo;   // флаг отрисовки герба

    @Column(name = "check_new_page")
    private boolean checkTransitionToNewPage;   // флаг проверки перехода на новую страницу

    @Column(name = "insert_type")
    private int insertType; // тип вставки (0 - классический, 1 - по координатам, 2 - по тэгу)

    public CreateSignFormModel toCreateSignFormModel()
    {
        CreateSignFormModel createSignFormModel = new CreateSignFormModel();
        createSignFormModel.setSignOwner(this.getSignOwner());
        createSignFormModel.setSignCertificate(this.getSignCertificate());
        createSignFormModel.setSignDateStart(this.getSignDateStart());
        createSignFormModel.setSignDateEnd(this.getSignDateEnd());
        createSignFormModel.setDrawLogo(this.isDrawLogo());
        createSignFormModel.setCheckTransitionToNewPage(this.isCheckTransitionToNewPage());
        createSignFormModel.setInsertType(this.getInsertType());

        return createSignFormModel;
    }
}
