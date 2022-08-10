package com.webapi.application.models;

public class FileConvertParamsModel
{
    private String fileName;    // путь к файлу
    private String signOwner;   // поле владельца подписи
    private String signCertificate; // поле номер сертификата
    private String signDateStart;   // поле начала действия сертификата
    private String signDateEnd;     // поле окончания действия сертификата
    private boolean drawLogo;   // флаг отрисовки герба
    private boolean checkTransitionToNewPage;   // флаг проверки перехода на новую страницу
    private int insertType; // тип вставки (0 - классический, 1 - по координатам, 2 - по тэгу)

    public boolean isDrawLogo()
    {
        return drawLogo;
    }

    public void setDrawLogo(boolean drawLogo)
    {
        this.drawLogo = drawLogo;
    }

    public String getFileName()
    {
        return fileName;
    }

    public void setFileName(String fileName)
    {
        this.fileName = fileName;
    }

    public String getSignOwner()
    {
        return signOwner;
    }

    public void setSignOwner(String signOwner)
    {
        this.signOwner = signOwner;
    }

    public String getSignCertificate()
    {
        return signCertificate;
    }

    public void setSignCertificate(String signCertificate)
    {
        this.signCertificate = signCertificate;
    }

    public String getSignDateStart()
    {
        return signDateStart;
    }

    public void setSignDateStart(String signDateStart)
    {
        this.signDateStart = signDateStart;
    }

    public String getSignDateEnd()
    {
        return signDateEnd;
    }

    public void setSignDateEnd(String signDateEnd)
    {
        this.signDateEnd = signDateEnd;
    }

    public boolean isCheckTransitionToNewPage()
    {
        return checkTransitionToNewPage;
    }

    public void setCheckTransitionToNewPage(boolean checkTransitionToNewPage)
    {
        this.checkTransitionToNewPage = checkTransitionToNewPage;
    }

    public int getInsertType()
    {
        return insertType;
    }

    public void setInsertType(int insertType)
    {
        this.insertType = insertType;
    }

    @Override
    public String toString()
    {
        return "FileConvertParamsModel{" + "fileName='" + fileName + '\'' + ", signOwner='" + signOwner + '\'' + ", signCertificate='" + signCertificate + '\'' + ", signDateStart='" + signDateStart + '\'' + ", signDateEnd='" + signDateEnd + '\'' + ", drawLogo=" + drawLogo + ", checkTransitionToNewPage=" + checkTransitionToNewPage + '}';
    }
}
