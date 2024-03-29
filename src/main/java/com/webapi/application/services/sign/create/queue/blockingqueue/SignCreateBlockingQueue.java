package com.webapi.application.services.sign.create.queue.blockingqueue;

import lombok.Getter;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Блокирующая очередь для задач создания подписи к файлам
 */
@Service
public class SignCreateBlockingQueue
{
    @Getter
    private Long lastTaskId = 0L;    // кол-во завершённых задач
    private ArrayList<SignCreateTask> tasksList = new ArrayList<>();    // список задач

    /** Возвращает верхнюю задачу из очереди. Если в очереди нет задач, ждёт добавления и возвращает её
     * @return Задача на формирование подписи
     * @see SignCreateTask
     */
    public synchronized SignCreateTask getNextTask()
    {
        while (tasksList.isEmpty())     // пока список задач пустой, ожидаем добавления задачи
        {
            try
            {
                wait();     // ожидаем задачу
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }

        }

        return tasksList.remove(0);     // берём верхнюю (первую) задачу из списка, удаляем её и возвращаем
    }

    /** Добавляет задачу в очередь
     * @param task задача на формирование подписи
     * @see SignCreateTask
     */
    public synchronized void addTask(SignCreateTask task)
    {
        tasksList.add(task);    // добавляем задачу в очередь
        lastTaskId += 1L;
        notify();   // оповещаем о добавлении
    }

    public SignCreateTask getTask(Long taskId)
    {
        return tasksList.stream()
                .filter(t -> taskId.equals(t.getTaskId()))
                .findFirst()
                .orElse(null);     // получаем задачу по ID задачи и пользователя
    }
}
