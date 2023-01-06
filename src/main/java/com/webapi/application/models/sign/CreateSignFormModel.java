package com.webapi.application.models.sign;

import com.webapi.application.services.cryptopro.jsp.CryptoPROCertificateModel;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

@Data
public class CreateSignFormModel
{
    private String fileName;    // путь к файлу
    private String signOwner;   // поле владельца подписи
    private String signCertificate; // поле номер сертификата
    private String signDateStart;   // поле начала действия сертификата
    private String signDateEnd;     // поле окончания действия сертификата
    private boolean drawLogo;   // флаг отрисовки герба
    private boolean checkTransitionToNewPage;   // флаг проверки перехода на новую страницу
    private int insertType; // тип вставки (0 - классический, 1 - по координатам, 2 - по тэгу)
    private MultipartFile file; // передаваемый файл

    @Override
    public String toString()
    {
        return "FileConvertParamsModel{" + "fileName='" + fileName + '\'' + ", signOwner='" + signOwner + '\'' + ", signCertificate='" + signCertificate + '\'' + ", signDateStart='" + signDateStart + '\'' + ", signDateEnd='" + signDateEnd + '\'' + ", drawLogo=" + drawLogo + ", checkTransitionToNewPage=" + checkTransitionToNewPage + '}';
    }

    /** Конвертер из Модели сертификатов RuToken в модель формы
     * @param ruTokenModel модель сертификата RuToken
     * @return Новый объект формы, основанный на данных RoTokenModel, поля fileName, drawLogo и т.д. - пустые
     */
    public static CreateSignFormModel fromRuTokenModel(RuTokenSignModel ruTokenModel)
    {
        CreateSignFormModel createSignFormModel = new CreateSignFormModel();    // модель

        createSignFormModel.setSignOwner(ruTokenModel.getSignOwner());   // владелец
        createSignFormModel.setSignCertificate(ruTokenModel.getSignCertificate());  // номер сертификата
        createSignFormModel.setSignDateStart(ruTokenModel.getSignDateStart());   // дата начал действия сертификата
        createSignFormModel.setSignDateEnd(ruTokenModel.getSignDateEnd());   // дата окончания действия сертификата

        return createSignFormModel;
    }
}
