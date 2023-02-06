package com.webapi.application.models.sign;

import lombok.Data;

/**
 * Модель результата подписи документа
 */
@Data
public class SignResultDownloadModel
{
    /**
     * Статус результат подписи файла
     */
    private FileProcessingResultStatus status;
    /**
     * Название файла
     */
    private String fileName;
    /**
     * Ссылка для скачивания готового документа
     */
    private String fileDownloadLink;
    /**
     * Ссылка на скачивание файла подписи документа
     */
    private String signFileDownloadLink;
    /**
     * Сообщение с текстом ошибки
     */
    private String errorMessage;
    /**
     * Наличие цифровой подписи
     */
    private boolean digitalSign;
}
