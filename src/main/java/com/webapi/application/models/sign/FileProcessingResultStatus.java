package com.webapi.application.models.sign;

/**
 * Статус результата подписи документа
 */
public enum FileProcessingResultStatus   // TODO: Доделать статусы ошибок
{
    OK(false),
    ERROR_FILE_NOT_SUPPORTING,
    ERROR_FILE_NOT_SUPPORTING_FOR_TAG,
    ERROR_FILE_NOT_SAVED,
    ERROR_CRYPTO_PRO_EXCEPTION,
    ERROR_CERTIFICATE_NOT_FOUND;

    /**
     * Наличие ошибок
     */
    private boolean error = true;

    FileProcessingResultStatus(boolean error)
    {
        this.error = error;
    }

    FileProcessingResultStatus() {}

    public boolean isError()
    {
        return error;
    }
}
