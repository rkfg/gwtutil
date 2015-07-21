package ru.ppsrk.gwt.client;

import com.google.gwt.i18n.client.Messages;

public interface CommonMessages extends Messages {

    @DefaultMessage("Добавить")
    public String add();

    @DefaultMessage("Создать")
    public String create();

    @DefaultMessage("Изменить")
    public String change();

    @DefaultMessage("Удалить")
    public String delete();

    @DefaultMessage("ОК")
    public String ok();

    @DefaultMessage("Отмена")
    public String cancel();

    @DefaultMessage("Выход")
    public String logout();

    @DefaultMessage("Роль")
    public String role();

    @DefaultMessage("Пользователь")
    public String user();

    @DefaultMessage("Пароль")
    public String password();
    
    @DefaultMessage("Менеджер")
    public String manager();

    @DefaultMessage("Администратор")
    public String admin();

    @DefaultMessage("Загружается...")
    public String loading();

    @DefaultMessage("С даты")
    public String fromDate();

    @DefaultMessage("По дату")
    public String toDate();

    @DefaultMessage("Итого")
    public String total();
}
