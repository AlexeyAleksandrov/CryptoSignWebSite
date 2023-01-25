package com.webapi.application.models.sign;

import com.webapi.application.models.user.User;
import lombok.Data;

import javax.persistence.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

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

    public void setFromModel(SignTemplateModel model)
    {
        this.user = model.user;
        this.templateName = model.templateName;
        this.signOwner = model.signOwner;
        this.signCertificate = model.signCertificate;
        this.signDateStart = model.signDateStart;
        this.signDateEnd = model.signDateEnd;
        this.drawLogo = model.drawLogo;
        this.checkTransitionToNewPage = model.checkTransitionToNewPage;
        this.insertType = model.insertType;
    }

    /** Функция преобразования даты в формате представления для HTML формы в формат для документов
     * @param dateString исходная строка даты в формате yyyy-MM-dd
     * @return дата в формате dd.MM.yyyy
     */
    private String getDateStartInDocumentFormat(String dateString)
    {
        DateFormat dateFormFormat = new SimpleDateFormat("yyyy-MM-dd");     // форматер исходной строки
        DateFormat dateDocumentFormat = new SimpleDateFormat("dd.MM.yyyy"); // форматер конечной строки
        try
        {
            Date date = dateFormFormat.parse(dateString);   // получаем дату из исходной строки
            return dateDocumentFormat.format(date);     // возвращаем отформатированную дату
        }
        catch (ParseException e)
        {
            e.printStackTrace();
            return dateString;      // в случае ошибки, возвращаем ту же самую строку
        }
    }

    /**
     * @return Преобразовывает дату из yyyy-MM-dd в dd.MM.yyyy
     */
    public String getSignDateStartInDocumentFormat()
    {
        return getDateStartInDocumentFormat(signDateStart);
    }

    /**
     * @return Преобразовывает дату из yyyy-MM-dd в dd.MM.yyyy
     */
    public String getSignDateEndInDocumentFormat()
    {
        return getDateStartInDocumentFormat(signDateEnd);
    }
}
