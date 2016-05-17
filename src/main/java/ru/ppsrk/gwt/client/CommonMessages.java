package ru.ppsrk.gwt.client;

import com.google.gwt.i18n.client.Messages;

public interface CommonMessages extends Messages {

    @DefaultMessage("Применить")
    public String apply();

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

    @DefaultMessage("Печать")
    public String print();

    @DefaultMessage("Обновить")
    public String refresh();

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

    @DefaultMessage("Поиск")
    public String search();

    @DefaultMessage("Не определено")
    public String undefined();

    @DefaultMessage("Сохранить")
    public String save();

    @DefaultMessage("Закрыть")
    public String close();

    @DefaultMessage("<Не задано>")
    public String notSet();
}
