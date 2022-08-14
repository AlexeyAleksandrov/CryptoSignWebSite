package com.webapi.application.models.sign;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

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
}
